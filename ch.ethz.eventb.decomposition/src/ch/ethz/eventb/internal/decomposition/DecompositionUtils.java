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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
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
	 * @return the labels of the accessed variables.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static Set<String> getAccessedVariables(final ISubModel subModel)
			throws RodinDBException {
		final Set<String> vars = getFreeIdentifiersFromEvents(subModel);
		// Removes the constants and sets.
		final IMachineRoot mch = subModel.getMachineRoot();
		vars.removeAll(EventBUtils.getSeenCarrierSetsAndConstants(mch));
		return vars;
	}
	
	private static Set<String> getFreeIdentifiersFromEvents(
			final ISubModel subModel) throws RodinDBException {
		final IMachineRoot mch = subModel.getMachineRoot();
		final Set<String> identifiers = new HashSet<String>();
		// Adds the free identifiers from the events.
		for (IRodinElement element : subModel.getElements()) {
			if (! (element instanceof IEvent)) {
				throw new IllegalArgumentException("Event Decomposition: event expected as sub-model element"); //$NON-NLS-1$
			}
			for (IEvent event : mch.getEvents()) {
				if (event.getLabel().equals(((IEvent) element).getLabel())) {
					identifiers.addAll(EventBUtils.getFreeIdentifiers(event));
				}
			}
		}
		return identifiers;
	}

	public static Set<String> getAccessedIdentifiers(ISubModel subModel)
			throws RodinDBException {
		final IMachineRoot mch = subModel.getMachineRoot();
		final Set<String> identifiers = getFreeIdentifiersFromEvents(subModel);
		for(IInvariant inv: mch.getInvariants()) {
			final List<String> freeIdents = getFreeIdentifiers(inv);
			identifiers.addAll(freeIdents);
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
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void decomposeInvariants(final IMachineRoot mch,
			final ISubModel subModel, final IProgressMonitor monitor)
			throws RodinDBException {
		IMachineRoot src = subModel.getMachineRoot();
		Set<String> vars = getAccessedVariables(subModel);
		
		// Create the typing theorems.
		createTypingTheorems(mch, src, vars, new SubProgressMonitor(monitor, 1));

		// Copy relevant invariants.
		EventBUtils.copyInvariants(mch, src, vars, new SubProgressMonitor(
				monitor, 1));

		monitor.done();
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
	 *            the progress monitor.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private static void createTypingTheorems(final IMachineRoot mch,
			final IMachineRoot src, final Set<String> vars,
			final IProgressMonitor monitor) throws RodinDBException {
		monitor.beginTask(Messages.decomposition_typingtheorems, vars.size());
		for (String var : vars) {
			final String typingTheorem = EventBUtils.getTypingTheorem(src, var);
			if (typingTheorem == null) {
				continue;
			}
			final IInvariant newInv = mch.createChild(IInvariant.ELEMENT_TYPE,
					null, monitor);
			newInv.setLabel(EventBUtils.makeTypingLabel(var), monitor);
			newInv.setTheorem(true, monitor);
			newInv.setPredicateString(typingTheorem, monitor);
			monitor.worked(1);
		}
		monitor.done();
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
	 *            the progress monitor
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static void setEventStatus(final IEvent srcEvt,
			final IEvent destEvt, final IProgressMonitor monitor)
			throws RodinDBException {
		// Gets the external status of the source event
		IExternalElement srcElt = (IExternalElement) srcEvt
				.getAdapter(IExternalElement.class);
		// Gets the external status of the destination event
		IExternalElement destElt = (IExternalElement) destEvt
				.getAdapter(IExternalElement.class);

		// Sets the convergence
		if (destElt.isExternal()) {
			destEvt.setConvergence(Convergence.ORDINARY, monitor);
		} else {
			Convergence convergence = srcEvt.getConvergence();
			if (convergence.equals(Convergence.ORDINARY)
					|| convergence.equals(Convergence.CONVERGENT)) {
				destEvt.setConvergence(Convergence.ORDINARY, monitor);
			} else if (convergence.equals(Convergence.ANTICIPATED)) {
				destEvt.setConvergence(Convergence.ANTICIPATED, monitor);
			}
		}

		// Sets the extended status
		destEvt.setExtended(false, monitor);

		// Sets the external status
		if (srcElt.isExternal()) {
			destElt.setExternal(true, monitor);
		}
	}
	
	/**
	 * @param dest
	 * @param mchRoot
	 * @param subModel
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @throws RodinDBException
	 */
	public static void decomposeContext(IContextRoot dest,
			IMachineRoot mchRoot, ISubModel subModel, IProgressMonitor monitor) throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.decomposition_decomposeContexts, 10);
		
		final Set<IContextRoot> seenContexts = getSeenContexts(subModel.getMachineRoot());
		if (seenContexts.isEmpty()) {
			return;
		}
		if (subMonitor.isCanceled()) return;
		subMonitor.worked(1);
		
		// get all seen constants and carrier sets
		final SymbolTable cstSetNonDecompSymbols = getConstantAndSetSymbols(seenContexts);
		if (subMonitor.isCanceled()) return;
		subMonitor.worked(1);
		
		// get per axiom references of all seen constants and carrier sets
		final ReferenceTable cstSetNonDecompCtxReferences = populateReferenceTable(
				seenContexts, cstSetNonDecompSymbols);
		if (subMonitor.isCanceled()) return;
		subMonitor.worked(1);
		
		// get references of constants and carrier sets in decomposed machine
		final ReferenceTable cstSetDecompMchReferences = new ReferenceTable();
		final MachineSymbolGatherer mchSymbGth = new MachineSymbolGatherer(mchRoot);
		mchSymbGth.addReferencedSymbols(cstSetNonDecompSymbols, cstSetDecompMchReferences);
		if (subMonitor.isCanceled()) return;
		subMonitor.worked(1);
		
		// get referenced symbols
		final Set<Symbol> neededSymbols = cstSetDecompMchReferences.getSymbols();
		if (subMonitor.isCanceled()) return;
		subMonitor.worked(1);

		// get typing theorems
		final Map<String, String> typingTheorems = getTypingTheorems(neededSymbols);
		if (subMonitor.isCanceled()) return;
		subMonitor.worked(1);
		
		// get axioms corresponding to referenced symbols
		final List<IAxiom> neededAxioms = getNeededAxioms(cstSetNonDecompCtxReferences, neededSymbols);
		if (subMonitor.isCanceled()) return;
		subMonitor.worked(1);
		
		// populate decomposed context
		createIdentifierElements(dest, neededSymbols, subMonitor.newChild(1));
		if (subMonitor.isCanceled()) return;

		createTypingTheorems(dest, typingTheorems, subMonitor.newChild(1));
		if (subMonitor.isCanceled()) return;

		createNeededAxioms(dest, neededAxioms, subMonitor.newChild(1));
	}

	private static ReferenceTable populateReferenceTable(
			final Set<IContextRoot> contexts, final SymbolTable symbolTable)
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
			final List<IAxiom> neededAxioms, IProgressMonitor monitor)
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
}
