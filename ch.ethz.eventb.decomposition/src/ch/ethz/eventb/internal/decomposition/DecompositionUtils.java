/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - implemented context decomposition
 *     Systerel - implemented progress reporting and cancellation support
 ****************************************************************************/

package ch.ethz.eventb.internal.decomposition;

import static ch.ethz.eventb.internal.decomposition.utils.EventBUtils.getConstantAndSetSymbols;
import static ch.ethz.eventb.internal.decomposition.utils.EventBUtils.getFreeIdentifiers;
import static ch.ethz.eventb.internal.decomposition.utils.EventBUtils.getSeenContexts;
import static ch.ethz.eventb.internal.decomposition.utils.EventBUtils.makeLabel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eventb.core.IAxiom;
import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBRoot;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.IInvariant;
import org.eventb.core.ILabeledElement;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IConvergenceElement.Convergence;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.location.IInternalLocation;

import ch.ethz.eventb.decomposition.ISubModel;
import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;
import ch.ethz.eventb.internal.decomposition.utils.Messages;
import ch.ethz.eventb.internal.decomposition.utils.symbols.ContextSymbolGatherer;
import ch.ethz.eventb.internal.decomposition.utils.symbols.MachineSymbolGatherer;
import ch.ethz.eventb.internal.decomposition.utils.symbols.ReferenceTable;
import ch.ethz.eventb.internal.decomposition.utils.symbols.Symbol;
import ch.ethz.eventb.internal.decomposition.utils.symbols.SymbolTable;

/**
 * @author htson
 *         <p>
 *         Class containing useful methods to perform decomposition (A-style or
 *         B-style).
 *         </p>
 */
public class DecompositionUtils {

	/**
	 * Returns the set of variables accessed by a sub-model.
	 * 
	 * @param subModel
	 *            the sub-model to be considered
	 *            @param monitor
	 * @return the labels of the accessed variables.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static Set<String> getAccessedVariables(final ISubModel subModel,
			IProgressMonitor monitor) throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		final Set<String> vars = getFreeIdentifiersFromEvents(subModel, subMonitor.newChild(1));
		// Removes the constants and sets.
		final IMachineRoot mch = subModel.getMachineRoot();
		vars.removeAll(EventBUtils.getSeenCarrierSetsAndConstants(mch));
		checkCancellation(subMonitor);
		subMonitor.worked(1);
		return vars;
	}
	
	private static Set<String> getFreeIdentifiersFromEvents(ISubModel subModel,
			IProgressMonitor monitor)
			throws RodinDBException {
		final IMachineRoot mch = subModel.getMachineRoot();
		final Set<String> identifiers = new HashSet<String>();
		// Adds the free identifiers from the events.
		final IRodinElement[] elements = subModel.getElements();
		final SubMonitor subMonitor = SubMonitor.convert(monitor, elements.length);
		for (IRodinElement element : elements) {
			if (! (element instanceof IEvent)) {
				throw new IllegalArgumentException("Event Decomposition: event expected as sub-model element"); //$NON-NLS-1$
			}
			for (IEvent event : mch.getEvents()) {
				if (event.getLabel().equals(((IEvent) element).getLabel())) {
					identifiers.addAll(EventBUtils.getFreeIdentifiers(event, subMonitor.newChild(0)));
				}
			}
			subMonitor.worked(1);
			checkCancellation(subMonitor);
		}
		return identifiers;
	}

	public static Set<String> getAccessedIdentifiers(ISubModel subModel,
			IProgressMonitor monitor) throws RodinDBException {
		final IMachineRoot mch = subModel.getMachineRoot();
		final Set<String> identifiers = getFreeIdentifiersFromEvents(subModel,
				monitor);
		for(IInvariant inv: mch.getInvariants()) {
			final List<String> freeIdents = getFreeIdentifiers(inv);
			identifiers.addAll(freeIdents);
			checkCancellation(monitor);
		}
		return identifiers;
	}

	/**
	 * Utility method to create invariants in an input machine for a given
	 * sub-model. This is done by first creating the typing theorems for the
	 * accessed variables, and then copying the "relevant" invariants from the
	 * source model (recursively).
	 * 
	 * @param mch
	 *            a machine.
	 * @param subModel
	 *            a sub-model.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void decomposeInvariants(IMachineRoot mch,
			ISubModel subModel, IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 3);
		final IMachineRoot src = subModel.getMachineRoot();
		final Set<String> vars = getAccessedVariables(subModel, subMonitor.newChild(1));
		
		// Create the typing theorems.
		createTypingTheorems(mch, src, vars, subMonitor.newChild(1));
		checkCancellation(subMonitor);
		
		// Copy relevant invariants.
		EventBUtils.copyInvariants(mch, src, vars, subMonitor.newChild(1));
		checkCancellation(subMonitor);
	}

	/**
	 * Utility method to create typing theorems in an input machine, given the
	 * set of variables and the source machine containing these variables.
	 * 
	 * @param mch
	 *            a machine.
	 * @param src
	 *            the source machine containing the variables.
	 * @param vars
	 *            the set of variables (in {@link String}).
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private static void createTypingTheorems(IMachineRoot mch,
			IMachineRoot src, Set<String> vars, IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.decomposition_typingtheorems, 4 * vars.size());
		for (String var : vars) {
			final String typingTheorem = EventBUtils.getTypingTheorem(src, var);
			if (typingTheorem == null) {
				continue;
			}
			final IInvariant newInv = mch.createChild(IInvariant.ELEMENT_TYPE,
					null, subMonitor.newChild(1));
			newInv.setLabel(EventBUtils.makeTypingLabel(var), subMonitor
					.newChild(1));
			newInv.setTheorem(true, subMonitor.newChild(1));
			newInv.setPredicateString(typingTheorem, subMonitor.newChild(1));
		}
	}

	/**
	 * Sets the status of an event in a sub-machine from the status of the
	 * associated event in the non-decomposed machine:
	 * <ul>
	 * <li>An <i>internal</i> event of a sub-machine is tagged as
	 * <i>ordinary</i> if and only if this event was declared <i>ordinary</i> or
	 * <i>convergent</i> in the non-decomposed machine.
	 * <li>An <i>internal</i> event of a sub-machine is tagged as
	 * <i>anticipated</i> if and only if this event was declared
	 * <i>anticipated</i> in the non-decomposed machine.
	 * <li>An <i>internal</i> event of a sub-machine is never tagged as
	 * <i>convergent</i>.
	 * <li>An <i>external</i> event of a sub-machine is always tagged as
	 * <i>ordinary</i>.
	 * <li>An event of a sub-machine, <i>external</i> or <i>internal</i>, is
	 * always tagged as <i>non-extended</i>.
	 * <li>An event tagged as <i>external</i> in the non-decomposed machine
	 * (<i>i.e.</i>resulting from a previous decomposition) remains
	 * <i>external</i> in the sub-machine.
	 * </ul>
	 * 
	 * @param srcEvt
	 *            the source event in the non-decomposed machine
	 * @param destEvt
	 *            the destination event in a sub-machine
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static void setEventStatus(IEvent srcEvt, IEvent destEvt,
			IProgressMonitor monitor) throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 3);
		// Gets the external status of the source event
		IExternalElement srcElt = (IExternalElement) srcEvt
				.getAdapter(IExternalElement.class);
		// Gets the external status of the destination event
		IExternalElement destElt = (IExternalElement) destEvt
				.getAdapter(IExternalElement.class);

		// Sets the convergence
		if (destElt.isExternal()) {
			destEvt.setConvergence(Convergence.ORDINARY, subMonitor.newChild(1));
		} else {
			final Convergence convergence = srcEvt.getConvergence();
			if (convergence.equals(Convergence.ORDINARY)
					|| convergence.equals(Convergence.CONVERGENT)) {
				destEvt.setConvergence(Convergence.ORDINARY, subMonitor
						.newChild(1));
			} else if (convergence.equals(Convergence.ANTICIPATED)) {
				destEvt.setConvergence(Convergence.ANTICIPATED, subMonitor
						.newChild(1));
			}
		}
		subMonitor.setWorkRemaining(2);

		// Sets the extended status
		destEvt.setExtended(false, subMonitor.newChild(1));

		// Sets the external status
		if (srcElt.isExternal()) {
			destElt.setExternal(true, subMonitor.newChild(1));
		}
		subMonitor.setWorkRemaining(0);
	}

	/**
	 * Populates the given context with elements required by a decomposed
	 * machine for a given sub model.
	 * 
	 * @param dest
	 *            the target context
	 * @param mchRoot
	 *            the decomposed machine
	 * 
	 * @param subModel
	 *            the sub model corresponding to the decomposed machine
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database
	 */
	public static void decomposeContext(IContextRoot dest,
			IMachineRoot mchRoot, ISubModel subModel, IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.decomposition_decomposeContexts, 10);

		final Set<IContextRoot> seenContexts = getSeenContexts(subModel
				.getMachineRoot());
		if (seenContexts.isEmpty()) {
			return;
		}
		checkCancellation(subMonitor);
		subMonitor.worked(1);

		// get all seen constants and carrier sets
		final SymbolTable cstSetNonDecompSymbols = getConstantAndSetSymbols(seenContexts);
		checkCancellation(subMonitor);
		subMonitor.worked(1);

		// get per axiom references of all seen constants and carrier sets
		final ReferenceTable cstSetNonDecompCtxReferences = populateReferenceTable(
				seenContexts, cstSetNonDecompSymbols);
		checkCancellation(subMonitor);
		subMonitor.worked(1);

		// get references of constants and carrier sets in decomposed machine
		final ReferenceTable cstSetDecompMchReferences = new ReferenceTable();
		final MachineSymbolGatherer mchSymbGth = new MachineSymbolGatherer(
				mchRoot);
		mchSymbGth.addReferencedSymbols(cstSetNonDecompSymbols,
				cstSetDecompMchReferences);
		checkCancellation(subMonitor);
		subMonitor.worked(1);

		// get referenced symbols
		final Set<Symbol> neededSymbols = cstSetDecompMchReferences
				.getSymbols();
		checkCancellation(subMonitor);
		subMonitor.worked(1);

		// get typing theorems
		final Map<String, String> typingTheorems = getTypingTheorems(neededSymbols);
		checkCancellation(subMonitor);
		subMonitor.worked(1);

		// get axioms corresponding to referenced symbols
		final List<IAxiom> neededAxioms = getNeededAxioms(
				cstSetNonDecompCtxReferences, neededSymbols);
		checkCancellation(subMonitor);
		subMonitor.worked(1);

		// populate decomposed context
		createIdentifierElements(dest, neededSymbols, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		createTypingTheorems(dest, typingTheorems, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		createNeededAxioms(dest, neededAxioms, subMonitor.newChild(1));
	}

	private static ReferenceTable populateReferenceTable(
			Set<IContextRoot> contexts, SymbolTable symbolTable)
			throws RodinDBException {
		final ReferenceTable constantSetCtxReferences = new ReferenceTable();
		for (IContextRoot ctx : contexts) {
			final ContextSymbolGatherer ctxSymbGth = new ContextSymbolGatherer(ctx);
			ctxSymbGth.addReferencedSymbols(symbolTable, constantSetCtxReferences);
		}
		return constantSetCtxReferences;
	}

	private static Map<String, String> getTypingTheorems(Set<Symbol> symbols)
			throws RodinDBException {
		final Map<String, String> typingTheorems = new LinkedHashMap<String, String>();
		for (Symbol symbol : symbols) {
			final IInternalElementType<?> elementType = symbol.getElementType();
			if (elementType != IConstant.ELEMENT_TYPE) {
				continue;
			}
			final String typingThm = symbol.getTypingTheorem();
			if (typingThm != null) {
				typingTheorems.put(symbol.getName(), typingThm);
			}
		}
		return typingTheorems;
	}

	private static List<IAxiom> getNeededAxioms(
			ReferenceTable constantSetCtxReferences, Set<Symbol> neededSymbols) {
		final List<IAxiom> axioms = new ArrayList<IAxiom>();
		for (Entry<IInternalLocation, Set<Symbol>> entry : constantSetCtxReferences
				.entrySet()) {
			final Set<Symbol> axiomReferences = entry.getValue();
			if (neededSymbols.containsAll(axiomReferences)) {
				final IInternalLocation location = entry.getKey();
				final IInternalElement element = location.getElement();
				if (element.getElementType() != IAxiom.ELEMENT_TYPE) {
					throw new IllegalArgumentException(
							"Context decomposition: axiom references expected"); //$NON-NLS-1$
				}
				axioms.add((IAxiom) element);
			}
		}
		return axioms;
	}

	private static void createIdentifierElements(IInternalElement parent,
			Set<Symbol> neededSymbols, IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				2 * neededSymbols.size());
		for (Symbol symbol : neededSymbols) {
			final IInternalElementType<?> elementType = symbol.getElementType();
			final IInternalElement child = parent.createChild(elementType,
					null, subMonitor.newChild(1));
			if (!(child instanceof IIdentifierElement)) {
				throw new IllegalArgumentException(
						"Context Decomposition: identifier expected"); //$NON-NLS-1$
			}
			((IIdentifierElement) child).setIdentifierString(symbol.getName(),
					subMonitor.newChild(1));
		}
	}

	private static void createTypingTheorems(IContextRoot dest,
			Map<String, String> typingTheorems, IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				4 * typingTheorems.size());
		int typingIndex = 1;
		for (Entry<String, String> entry : typingTheorems.entrySet()) {
			final IAxiom axm = dest.createChild(IAxiom.ELEMENT_TYPE, null,
					subMonitor.newChild(1));
			axm.setLabel(EventBUtils.makeTypingLabel(entry.getKey()), subMonitor
					.newChild(1));
			typingIndex++;
			axm.setPredicateString(entry.getValue(), subMonitor.newChild(1));
			axm.setTheorem(true, subMonitor.newChild(1));
		}
	}

	private static void createNeededAxioms(IContextRoot dest,
			List<IAxiom> neededAxioms, IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				4 * neededAxioms.size());
		for (IAxiom axm : neededAxioms) {
			final IAxiom destAxm = dest.createChild(IAxiom.ELEMENT_TYPE, null,
					subMonitor.newChild(1));
			destAxm.setLabel(makePredicateLabel(axm),
					subMonitor.newChild(1));
			destAxm.setPredicateString(axm.getPredicateString(), subMonitor
					.newChild(1));
			destAxm.setTheorem(axm.isTheorem(), subMonitor.newChild(1));
		}
	}

	private static String makePredicateLabel(ILabeledElement element) throws RodinDBException {
		final IEventBRoot root = (IEventBRoot) element.getRoot();
		return makeLabel(root.getComponentName(), element.getLabel());
	}

	public static void checkCancellation(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled()) {
			monitor.done();
			throw new OperationCanceledException();
		}
	}
}
