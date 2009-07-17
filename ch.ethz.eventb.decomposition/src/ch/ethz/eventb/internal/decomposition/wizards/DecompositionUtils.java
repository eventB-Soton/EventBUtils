/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     ETH Zurich - initial API and implementation
 ****************************************************************************/

package ch.ethz.eventb.internal.decomposition.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IGuard;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IRefinesMachine;
import org.eventb.core.IVariable;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.BecomesSuchThat;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.SourceLocation;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         Utility class contains method for decomposing models.
 *         </p>
 */
public class DecompositionUtils {

	/**
	 * @author htson
	 *         <p>
	 *         Enumerated event types:
	 *         <ul>
	 *         <li>{@value #EXTERNAL}: External events.</li>
	 *         <li>{@value #INTERNAL}: Internal events.</li>
	 *         <li>{@value #NONE}: Events does not accessed any variables of the
	 *         distribution.</li>
	 *         </ul>
	 *         </p> {@see DecompositionUtils#getEventType(IElementDistribution,
	 *         IEvent)}.
	 */
	enum DecomposedEventType {
		EXTERNAL(2), INTERNAL(1), NONE(0);

		// The code.
		private final int code;

		/**
		 * Constructor.
		 * 
		 * @param code
		 *            the internal code.
		 */
		DecomposedEventType(int code) {
			this.code = code;
		}

		/**
		 * Return the internal code.
		 * 
		 * @return the internal code.
		 */
		public int getCode() {
			return code;
		}
	}

	/**
	 * Utility method for decomposing a model, given a model distribution.
	 * 
	 * @param modelDist
	 *            a model distribution.
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if some errors occurred when creating any sub-models
	 *             according to the model distribution
	 *             {@link #createSubModel(IElementDistribution, SubProgressMonitor)}.
	 */
	public static void decomposeModel(IModelDistribution modelDist,
			IProgressMonitor monitor) throws RodinDBException {
		IElementDistribution[] distributions = modelDist
				.getElementDistributions();

		// The number of work is the number of distributions.
		monitor.beginTask("Generating sub-models", distributions.length);
		for (IElementDistribution dist : distributions) {
			// For each distribution, create the corresponding model.
			monitor.subTask("Create sub-model");
			createSubModel(dist, new SubProgressMonitor(monitor, 1));
		}
		monitor.done();
		return;
	}

	/**
	 * Utility method for creating a model, given an element distribution.
	 * 
	 * @param dist
	 *            an element distribution.
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>creating a new event project
	 *             {@link EventBUtils#createProject(String, IProgressMonitor)}.</li>
	 *             <li>copying all contexts from the original project to the
	 *             newly created project
	 *             {@link EventBUtils#copyContexts(IEventBProject, IEventBProject, IProgressMonitor)}.</li>
	 *             <li>creating a new machine
	 *             {@link EventBUtils#createMachine(IEventBProject, String, IProgressMonitor)}.</li>
	 *             <li>copying all SEES clauses from the decomposing model
	 *             {@link EventBUtils#copySeesClauses(IMachineRoot, IMachineRoot, IProgressMonitor)}.</li>
	 *             <li>decomposing variables
	 *             {@link DecompositionUtils#decomposeVariables(IMachineRoot, IElementDistribution, IProgressMonitor)}, invariants
	 *             {@link DecompositionUtils#decomposeInvariants(IMachineRoot, IElementDistribution, IProgressMonitor)}
	 *             and events
	 *             {@link DecompositionUtils#decomposeEvents(IMachineRoot, IElementDistribution, IProgressMonitor)}.</li>
	 *             <li>saving the newly created model
	 *             {@link IRodinFile#save(IProgressMonitor, boolean)}.</li>
	 *             </ul>
	 */
	private static void createSubModel(IElementDistribution dist,
			SubProgressMonitor monitor) throws RodinDBException {
		// Monitor has 8 works.
		monitor.beginTask("Create sub-model", 8);
		IMachineRoot src = dist.getMachineRoot();
		
		// 1: Create project
		monitor.subTask("Creating new projects");
		IEventBProject prj = EventBUtils.createProject(dist.getProjectName(),
				new NullProgressMonitor());
		monitor.worked(1);

		// 2: Copy contexts from the original project
		monitor.subTask("Copying contexts");
		EventBUtils.copyContexts(src.getEventBProject(), prj,
				new NullProgressMonitor());
		monitor.worked(1);

		// 3: Create machine.
		monitor.subTask("Create machine");
		IMachineRoot dest = EventBUtils.createMachine(prj, src.getElementName(),
				new NullProgressMonitor());
		monitor.worked(1);

		// 4: Copy SEES clauses.
		monitor.subTask("Copy SEES clauses");
		EventBUtils.copySeesClauses(src, dest, new NullProgressMonitor());
		monitor.worked(1);

		// 5: Create variables.
		monitor.subTask("Create variables");
		DecompositionUtils.decomposeVariables(dest, dist,
				new SubProgressMonitor(monitor, 1));

		// 6: Create invariants.
		monitor.subTask("Create invariants");
		DecompositionUtils.decomposeInvariants(dest, dist,
				new SubProgressMonitor(monitor, 1));

		// 7: Create events.
		monitor.subTask("Create external events");
		DecompositionUtils.decomposeEvents(dest, dist, new SubProgressMonitor(
				monitor, 1));

		// 8: Save the resulting sub-model.
		dest.getRodinFile().save(new SubProgressMonitor(monitor, 1), false);
		monitor.done();
	}

	/**
	 * Utility method for creating variables in an input machine given an
	 * element distribution. This is done by copying the accessed variables of
	 * the distribution to the machine as either shared variables (
	 * {@link #createSharedVariable(IMachineRoot, String)}) or as private
	 * variables ({@link #createPrivateVariable(IMachineRoot, String)}).
	 * 
	 * @param mch
	 *            a machine.
	 * @param dist
	 *            an element distribution.
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the shared variables of the input model
	 *             distribution {@link IModelDistribution#getSharedVariables()}.
	 *             </li>
	 *             <li>creating the shared variables
	 *             {@link #createSharedVariable(IMachineRoot, String)}.</li>
	 *             <li>creating the private variables
	 *             {@link #createPrivateVariable(IMachineRoot, String)}.</li>
	 *             </ul>
	 */
	public static void decomposeVariables(IMachineRoot mch,
			IElementDistribution dist, IProgressMonitor monitor)
			throws RodinDBException {
		Set<String> accessedVars = dist.getAccessedVariables();
		IModelDistribution modelDist = dist.getModelDistribution();

		Collection<String> sharedVars = modelDist.getSharedVariables();
		monitor.beginTask("Create variables", accessedVars.size());
		for (String var : accessedVars) {
			monitor.subTask("Create variable " + var);
			if (sharedVars.contains(var)) {
				createSharedVariable(mch, var);
			} else {
				createPrivateVariable(mch, var);
			}
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * Utility method for creating a shared variable in a machine with a given
	 * identifier.
	 * 
	 * @param mch
	 *            a machine
	 * @param ident
	 *            the identifier of the variable
	 * @return the newly created shared variable.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>creating the new variable in the input machine
	 *             {@link IMachineRoot#createChild(org.rodinp.core.IInternalElementType, org.rodinp.core.IInternalElement, IProgressMonitor)}
	 *             .</li>
	 *             <li>setting the identifier string of the created variable
	 *             {@link IVariable#setIdentifierString(String, IProgressMonitor)}
	 *             .</li>
	 *             <li>setting the comment of the created variable
	 *             {@link IVariable#setComment(String, IProgressMonitor)}.</li>
	 *             </ul>
	 *             TODO: Changed when the shared/private attribute is defined.
	 */
	private static IVariable createSharedVariable(IMachineRoot mch, String ident)
			throws RodinDBException {
		IVariable var = mch.createChild(IVariable.ELEMENT_TYPE, null,
				new NullProgressMonitor());
		var.setIdentifierString(ident, new NullProgressMonitor());
		var.setComment("Shared variable, DO NOT REFINE",
				new NullProgressMonitor());
		return var;
	}

	/**
	 * Utility method for creating a private variable in a machine with a given
	 * identifier.
	 * 
	 * @param mch
	 *            a machine
	 * @param ident
	 *            the identifier of the variable
	 * @return the newly created private variable.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>creating the new variable in the input machine
	 *             {@link IMachineRoot#createChild(org.rodinp.core.IInternalElementType, org.rodinp.core.IInternalElement, IProgressMonitor)}
	 *             .</li>
	 *             <li>setting the identifier string of the created variable
	 *             {@link IVariable#setIdentifierString(String, IProgressMonitor)}
	 *             .</li>
	 *             <li>setting the comment of the created variable
	 *             {@link IVariable#setComment(String, IProgressMonitor)}.</li>
	 *             </ul>
	 *             TODO: Changed when the shared/private attribute is defined.
	 */
	private static IVariable createPrivateVariable(IMachineRoot mch,
			String ident) throws RodinDBException {
		IVariable var = mch.createChild(IVariable.ELEMENT_TYPE, null,
				new NullProgressMonitor());
		var.setIdentifierString(ident, new NullProgressMonitor());
		var.setComment("Private variable", new NullProgressMonitor());
		return var;
	}

	/**
	 * Utility method for creating invariants in an input machine given an
	 * element distribution. This is done by first creating the typing theorems
	 * for the accessed variables then copying the "relevant" invariants from
	 * the source model (recursively).
	 * 
	 * @param mch
	 *            a machine.
	 * @param dist
	 *            an element distribution.
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>creating the typing theorems for the accessed variables
	 *             of the input element distribution
	 *             {@link #createTypingTheorems(IMachineRoot, IMachineRoot, Set)}
	 *             .</li>
	 *             <li>copy invariants from the source machine associated with
	 *             the input element distribution to the input machine
	 *             {@link #copyInvariants(IMachineRoot, IMachineRoot, Set)}.</li>
	 *             </ul>
	 */
	public static void decomposeInvariants(IMachineRoot mch,
			IElementDistribution dist, IProgressMonitor monitor)
			throws RodinDBException {
		IMachineRoot src = dist.getMachineRoot();
		Set<String> vars = dist.getAccessedVariables();
		monitor.beginTask("Create invariants", 2);

		// Create the typing theorems.
		createTypingTheorems(mch, src, vars, new SubProgressMonitor(monitor, 1));
		
		// Copy relevant invariants.
		copyInvariants(mch, src, vars, new SubProgressMonitor(monitor, 1));
		
		monitor.done();
	}

	/**
	 * Utility method for creating typing theorems in an input machine, given
	 * the set of variables and the source machine contains these variables.
	 * 
	 * @param mch
	 *            a machine
	 * @param src
	 *            the source machine contains the variables.
	 * @param vars
	 *            the set of variables (in {@link String}).
	 * @throws RodinDBException
	 *             if some errors occurred when creating tying theorem for any
	 *             input variable
	 *             {@link #createTypingTheorem(IMachineRoot, IMachineRoot, String)}
	 *             .</li>
	 */
	private static void createTypingTheorems(IMachineRoot mch,
			IMachineRoot src, Set<String> vars, IProgressMonitor monitor)
			throws RodinDBException {
		monitor.beginTask("Create typing theorems", vars.size());
		for (String var : vars) {
			IInvariant newInv = mch.createChild(IInvariant.ELEMENT_TYPE, null,
					new NullProgressMonitor());
			newInv.setLabel("typing_" + var, new NullProgressMonitor());
			newInv.setTheorem(true, new NullProgressMonitor());
			newInv.setPredicateString(EventBUtils.getTypingTheorem(src, var),
					new NullProgressMonitor());
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * Utility method for copying the relevant invariants to a set of variables
	 * from a source machine to a destination machine (recursively).
	 * 
	 * @param mch
	 *            the destination machine.
	 * @param src
	 *            the source machine.
	 * @param vars
	 *            the set of variables (in {@link String}).
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting REFINES clauses of the source machine
	 *             {@link IMachineRoot#getRefinesClauses()}.</li>
	 *             <li>getting the abstract machine of any REFINES clause
	 *             {@link IRefinesMachine#getAbstractMachine()}.</li>
	 *             <li>getting the invariants of the source machine
	 *             {@link IMachineRoot#getInvariants()}.</li>
	 *             <li>checking the relevant of the invariant against the input
	 *             set of variables {@link #isRelevant(IInvariant, Set)}.</li>
	 *             <li>creating the new invariant in the destination machine
	 *             {@link IMachineRoot#createChild(org.rodinp.core.IInternalElementType, org.rodinp.core.IInternalElement, IProgressMonitor)}
	 *             .</li>
	 *             <li>setting the label of the created invariant
	 *             {@link IInvariant#setLabel(String, IProgressMonitor)}.</li>
	 *             <li>setting the predicate string of the created invariant
	 *             {@link IInvariant#setPredicateString(String, IProgressMonitor)}
	 *             .</li>
	 *             <li>setting the theorem attributed of the created invariant
	 *             {@link IInvariant#setTheorem(boolean, IProgressMonitor)}.</li>
	 *             </ul>
	 */
	private static void copyInvariants(IMachineRoot mch, IMachineRoot src,
			Set<String> vars, IProgressMonitor monitor) throws RodinDBException {
		// Recursively copy from the abstract machine.
		IRefinesMachine[] refinesClauses = src.getRefinesClauses();
		if (refinesClauses.length != 0) {
			copyInvariants(mch, (IMachineRoot) refinesClauses[0]
					.getAbstractMachine().getRoot(), vars, monitor);
		}

		// Check local invariants
		IInvariant[] invs = src.getInvariants();
		for (IInvariant inv : invs) {
			if (isRelevant(inv, vars)) {
				IInvariant newInv = mch.createChild(IInvariant.ELEMENT_TYPE,
						null, new NullProgressMonitor());
				newInv.setLabel(src.getComponentName() + "_" + inv.getLabel(),
						new NullProgressMonitor());
				newInv.setPredicateString(inv.getPredicateString(),
						new NullProgressMonitor());
				newInv.setTheorem(inv.isTheorem(), new NullProgressMonitor());
			}
		}

	}

	/**
	 * Utility method for checking if an invariant is relevant or to a set of
	 * variables or not. Currently, these are the invariants whose variables are
	 * contained by the input set of variables.
	 * 
	 * @param inv
	 *            an invariant
	 * @param vars
	 *            a set of variables (in {@link String}).
	 * @return return <code>true</code> if the invariant is relevant, otherwise
	 *         return <code>false</code>.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the free identifiers of the input invariant
	 *             {@link EventBUtils#getFreeIdentifiers(IInvariant)}.</li>
	 *             <li>getting the seen carrier sets and constants of the
	 *             machine contains the input invariant
	 *             {@link EventBUtils#getSeenCarrierSetsAndConstants(IMachineRoot)}
	 *             .</li>
	 *             </ul>
	 */
	private static boolean isRelevant(IInvariant inv, Set<String> vars)
			throws RodinDBException {
		Collection<String> idents = EventBUtils.getFreeIdentifiers(inv);

		// Remove the seen carrier sets and constants.
		IMachineRoot mch = (IMachineRoot) inv.getRoot();
		idents.removeAll(EventBUtils.getSeenCarrierSetsAndConstants(mch));
		return vars.containsAll(idents);
	}

	/**
	 * Utility method for creating the event in an input machine given an
	 * element distribution.
	 * 
	 * @param dest
	 *            the destination machine.
	 * @param dist
	 *            the element distribution
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>creating the initialisation
	 *             {@link #createInitialisation(IMachineRoot, IElementDistribution, IProgressMonitor)}.</li>
	 *             <li>getting the the events of the source machine associated
	 *             with the input element distribution
	 *             {@link IMachineRoot#getEvents()}.</li>
	 *             <li>getting the event type of any input event
	 *             {@link #getEventType(IElementDistribution, IEvent)}.</li>
	 *             <li>creating external event
	 *             {@link #createExternalEvent(IMachineRoot, IElementDistribution, IEvent)}.</li>
	 *             <li>creating internal event
	 *             {@link #createInternalEvent(IMachineRoot, IElementDistribution, IEvent)}.</li>
	 *             </ul>
	 */
	public static void decomposeEvents(IMachineRoot dest,
			IElementDistribution dist, IProgressMonitor monitor)
			throws RodinDBException {
		IMachineRoot src = dist.getMachineRoot();

		// Create other events.
		IEvent[] evts = src.getEvents();
		for (IEvent evt : evts) {
			DecomposedEventType type = getEventType(dist, evt);
			if (type == DecomposedEventType.EXTERNAL) {
				createExternalEvent(dest, dist, evt);
			} else if (type == DecomposedEventType.INTERNAL) {
				createInternalEvent(dest, evt);
			}
		}
	}

	/**
	 * Utility method for getting the type of an input event, given an element
	 * distribution.
	 * <ul>
	 * <li>An event is {@link DecomposedEventType#INTERNAL} if it belongs to the
	 * distribution.</li>
	 * <li>An event is {@link DecomposedEventType#EXTERNAL} if it does not
	 * belong to the distribution, but accesses some variables belong to the
	 * distribution.</li>
	 * <li>Otherwise (i.e. the event does not access any variable belong to the
	 * distribution), the type of the event is {@link DecomposedEventType#NONE}
	 * and will be ignore when creating the decomposed model.</li>
	 * </ul>
	 * Note: The INITIALISATION is an always external event.
	 * 
	 * @param dist
	 *            an element distribution
	 * @param evt
	 *            an event
	 * @return the type of the input event according to the element
	 *         distribution.
	 * @throws RodinDBException
	 *             if some errors occurred. TODO: List the errors.
	 */
	private static DecomposedEventType getEventType(IElementDistribution dist,
			IEvent evt) throws RodinDBException {
		if (evt.isInitialisation())
			return DecomposedEventType.EXTERNAL;
		
		String[] evtLabels = dist.getEventLabels();
		for (String evtLabel : evtLabels) {
			if (evt.getLabel().equals(evtLabel))
				return DecomposedEventType.INTERNAL;
		}

		Collection<String> vars = dist.getAccessedVariables();
		Collection<String> idents = EventBUtils.getFreeIdentifiers(evt);
		for (String var : vars) {
			if (idents.contains(var)) {
				return DecomposedEventType.EXTERNAL;
			}
		}

		return DecomposedEventType.NONE;
	}

	/**
	 * Utility method for creating an external event in the input machine
	 * corresponding to the input event, given an element distribution.
	 * 
	 * @param mch
	 *            the destination machine.
	 * @param dist
	 *            an element distribution.
	 * @param evt
	 *            an event.
	 * @throws RodinDBException
	 *             if some errors occurred. 
	 *             TODO: List the possible errors.
	 */
	private static void createExternalEvent(IMachineRoot mch,
			IElementDistribution dist, IEvent evt) throws RodinDBException {
		// Flatten the original event.
		evt = EventBUtils.flatten(evt);

		// Create the new event.
		IEvent newEvt = mch.createChild(IEvent.ELEMENT_TYPE, null,
				new NullProgressMonitor());

		// Set event signature.
		newEvt.setLabel(evt.getLabel(), new NullProgressMonitor());
		newEvt.setConvergence(Convergence.ORDINARY, new NullProgressMonitor());
		newEvt.setExtended(false, new NullProgressMonitor());
		newEvt.setComment("External event, DO NOT REFINE",
				new NullProgressMonitor());

		// Copying the parameters from the source event.
		copyParameters(newEvt, evt);
		
		// Copying the guards from the source event.
		copyGuards(newEvt, evt);
		
		
		Set<String> vars = dist.getAccessedVariables();

		// Decomposing actions.
		decomposeActions(newEvt, evt, vars);

		creatingExtraParametersAndGuards(dist.getMachineRoot(), newEvt, vars);
	}

	/**
	 * Utility method for copying all parameters of a source event to a
	 * destination event.
	 * 
	 * @param dest
	 *            the destination event.
	 * @param src
	 *            the source event.
	 * @throws RodinDBException
	 *             if some errors occurred. TODO List the possible errors.
	 */
	private static void copyParameters(IEvent dest, IEvent src)
			throws RodinDBException {
		IParameter[] params = src.getParameters();
		for (IParameter param : params) {
			IParameter newParam = dest.createChild(IParameter.ELEMENT_TYPE,
					null, new NullProgressMonitor());
			newParam.setIdentifierString(param.getIdentifierString(),
					new NullProgressMonitor());
		}
	}

	/**
	 * Utility method for copying all guards of a source event to a destination
	 * event.
	 * 
	 * @param dest
	 *            the destination event.
	 * @param src
	 *            the source event.
	 * @throws RodinDBException
	 */
	private static void copyGuards(IEvent dest, IEvent src)
			throws RodinDBException {
		// Copy guards from the source event.
		IGuard[] grds = src.getGuards();
		for (IGuard grd : grds) {
			IGuard newGrd = dest.createChild(IGuard.ELEMENT_TYPE, null,
					new NullProgressMonitor());
			newGrd.setLabel(grd.getLabel(), new NullProgressMonitor());
			newGrd.setPredicateString(grd.getPredicateString(),
					new NullProgressMonitor());
			newGrd.setTheorem(grd.isTheorem(), new NullProgressMonitor());
		}
	}

	/**
	 * Utility method for creating extra parameters corresponding to variables
	 * that are not accessed by the distribution and but used by the decomposed
	 * event. The extra guards are the typing theorems of the extra parameters.
	 * This is the last step in decomposing event.
	 * 
	 * @param src
	 *            the source machine for getting the typing theorem.
	 * @param evt
	 *            the current decomposed event.
	 * @param vars
	 *            the set of accessed variables
	 * @throws RodinDBException
	 *             if some errors occurred. TODO List possible errors.
	 */
	private static void creatingExtraParametersAndGuards(IMachineRoot src,
			IEvent evt, Set<String> vars) throws RodinDBException {
		List<String> idents = EventBUtils.getFreeIdentifiers(evt);
		idents.removeAll(EventBUtils.getSeenCarrierSetsAndConstants(src));
		idents.removeAll(vars);
		IParameter[] params = evt.getParameters();
		for (IParameter param : params) {
			idents.remove(param.getIdentifierString());
		}
		
		for (String ident : idents) {
			IParameter newParam = evt.createChild(IParameter.ELEMENT_TYPE,
					null, new NullProgressMonitor());
			newParam.setIdentifierString(ident, new NullProgressMonitor());
		}
		
		IGuard fstGrd = null;
		IGuard[] grds = evt.getGuards();
		if (grds.length != 0)
			fstGrd = grds[0];
		
		for (String ident : idents) {
			String typThm = EventBUtils.getTypingTheorem(src, ident);
			IGuard newGrd = evt.createChild(IGuard.ELEMENT_TYPE, fstGrd,
					new NullProgressMonitor());
			newGrd.setLabel("typing_" + ident, new NullProgressMonitor());
			newGrd.setPredicateString(typThm, new NullProgressMonitor());
			newGrd.setTheorem(true, new NullProgressMonitor());
		}
	}

	/**
	 * Utility method for creating actions in a destination event given the
	 * source event and a set of accessed variables by an element distribution.
	 * 
	 * @param dest
	 *            the destination event.
	 * @param src
	 *            the source event.
	 * @param vars
	 *            the accessed variables of an element distribution.
	 * @throws RodinDBException
	 *             if some errors occurred. TODO List of the possible errors.
	 */
	private static void decomposeActions(IEvent dest, IEvent src,
			Set<String> vars) throws RodinDBException {
		IAction[] acts = src.getActions();
		for (IAction act : acts) {
			String newAssignmentStr = decomposeAction(act, vars);
			if (newAssignmentStr == null)
				continue;
			IAction newAct = dest.createChild(IAction.ELEMENT_TYPE, null,
					new NullProgressMonitor());
			newAct.setLabel(act.getLabel(), new NullProgressMonitor());
			newAct.setAssignmentString(newAssignmentStr,
					new NullProgressMonitor());
		}
	}

	/**
	 * Utility method for decomposing an action according to a set of given
	 * accessed variables.
	 * 
	 * @param act
	 *            an action.
	 * @param vars
	 *            a set of variables (in {@link String}.
	 * @return the resulting decomposed action.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>normalising the action {@link #normalise(IAction)}.</li>
	 *             <li>getting the assignment string of the action
	 *             {@link IAction#getAssignmentString()}.</li>
	 *             <li>setting the assignment string of the action
	 *             {@link IAction#setAssignmentString(String, IProgressMonitor)}
	 *             .</li>
	 *             </ul>
	 */
	public static String decomposeAction(IAction act, Set<String> vars)
			throws RodinDBException {
	
		// Parsing the assignment string and getting assigned variables.
		String assignmentStr = act.getAssignmentString();
		Assignment parseAssignment = Lib.parseAssignment(assignmentStr);
		FreeIdentifier[] assignedVars = parseAssignment.getAssignedIdentifiers();
		
		// Getting the set of assigned variables which are also accessed
		// variables (v) and the set of assigned variables which are not
		// accessed variables (w).
		List<FreeIdentifier> v = new ArrayList<FreeIdentifier>();
		List<FreeIdentifier> w = new ArrayList<FreeIdentifier>();
		for (FreeIdentifier ident : assignedVars) {
			if (vars.contains(ident.getName())) {
				v.add(ident);
			} else {
				w.add(ident);
			}
		}
	
	
		// Return nothing if it does not modify any accessed variables. 
		// This covers the cases for
		//    w :: E(v, w)
		//    w :| P(v, w, w')
		//    w := E(v, w)
		//    w(E) := F
		if (v.isEmpty()) {
			return null;
		}
	
		// Do nothing if all assigned variables are accessed variables.
		// This covers the cases for
		//    v :: E(v, w)
		//    v :| P(v, w, w')
		//    v := E(v, w)
		//    v(E) := F
		if (w.isEmpty()) {
			return act.getAssignmentString();
		}
	
		// v, w := E(v,w), F(v, w) => v := E(v, w)
		if (parseAssignment instanceof BecomesEqualTo) {
			BecomesEqualTo bcmeq = (BecomesEqualTo) parseAssignment;
			Expression[] exps = bcmeq.getExpressions();
			assert exps.length == assignedVars.length;
			String newAssignmentStr = EventBUtils.identsToCSVString(
					assignmentStr, v.toArray(new FreeIdentifier[v.size()]));
			newAssignmentStr += " ≔ ";
			boolean fst = true;
			for (int i = 0; i < exps.length; i++) {
				FreeIdentifier ident = assignedVars[i];
				if (v.contains(ident)) {
					if (fst) {
						fst = false;
					} else {
						newAssignmentStr += ", ";
					}
					newAssignmentStr += exps[i];
				} else {
					continue;
				}
			}
			return newAssignmentStr;
		}
		
		// v, w :| P(v',w') ==> v :| #w'.P(v',w')
		else {
			assert parseAssignment instanceof BecomesSuchThat;
			BecomesSuchThat bcmsuch = (BecomesSuchThat) parseAssignment;
			Predicate P = bcmsuch.getCondition();
			String vList = EventBUtils.identsToCSVString(assignmentStr, v
					.toArray(new FreeIdentifier[v.size()]));
			String wPrimedList = EventBUtils.identsToPrimedCSVString(
					assignmentStr, w.toArray(new FreeIdentifier[w.size()]));
	
			SourceLocation srcLoc = P.getSourceLocation();
			String strP = assignmentStr.substring(srcLoc.getStart(), srcLoc
					.getEnd() + 1);
			String newAssignmentStr = vList + " :∣ ∃" + wPrimedList + "·"
					+ strP;
			return newAssignmentStr;
		}
	}

	/**
	 * Utility method for creating an internal event in a destination machine
	 * given the source event.
	 * 
	 * @param mch
	 *            the destination machine.
	 * @param evt
	 *            the source event.
	 * @throws RodinDBException
	 *             if some errors occurred. TODO List of the possible errors.
	 *             TODO This is just copying the source event into the
	 *             destination machine.
	 */
	private static void createInternalEvent(IMachineRoot mch, IEvent evt)
			throws RodinDBException {
		// Flatten the original event.
		evt = EventBUtils.flatten(evt);

		// Create the new event.
		IEvent newEvt = mch.createChild(IEvent.ELEMENT_TYPE, null,
				new NullProgressMonitor());

		// Set event signature.
		newEvt.setLabel(evt.getLabel(), new NullProgressMonitor());
		newEvt.setConvergence(Convergence.ORDINARY, new NullProgressMonitor());
		newEvt.setExtended(false, new NullProgressMonitor());

		// Copy the parameters.
		IParameter[] params = evt.getParameters();
		for (IParameter param : params) {
			IParameter newParam = newEvt.createChild(IParameter.ELEMENT_TYPE,
					null, new NullProgressMonitor());
			newParam.setIdentifierString(param.getIdentifierString(),
					new NullProgressMonitor());
		}

		// Copy the guards.
		IGuard[] grds = evt.getGuards();
		for (IGuard grd : grds) {
			IGuard newGrd = newEvt.createChild(IGuard.ELEMENT_TYPE, null,
					new NullProgressMonitor());
			newGrd.setLabel(grd.getLabel(), new NullProgressMonitor());
			newGrd.setPredicateString(grd.getPredicateString(),
					new NullProgressMonitor());
			newGrd.setTheorem(grd.isTheorem(), new NullProgressMonitor());
		}

		// Copy the actions.
		IAction[] acts = evt.getActions();
		for (IAction act : acts) {
			IAction newAct = newEvt.createChild(IAction.ELEMENT_TYPE, null,
					new NullProgressMonitor());
			newAct.setLabel(act.getLabel(), new NullProgressMonitor());
			newAct.setAssignmentString(act.getAssignmentString(),
					new NullProgressMonitor());
		}
	}

	/**
	 * Utility method for cleaning up the decomposition process. Make the
	 * decomposing machine consistent and all the contexts in the same project
	 * as the decomposing machine consistent.
	 * 
	 * @param modelDist
	 *            a model distribution.
	 * @param monitor
	 *            a progress monitor.
	 */
	public static void cleanUp(IModelDistribution modelDist,
			IProgressMonitor monitor) {
		monitor.subTask("Cleanup");

		// Make the machine consistent.
		IMachineRoot mch = modelDist.getMachineRoot();
		try {
			IRodinFile rodinFile = mch.getRodinFile();
			if (rodinFile.hasUnsavedChanges())
				rodinFile.makeConsistent(monitor);
		} catch (RodinDBException e) {
			e.printStackTrace();
		}
		
		// Make all the contexts consistent.
		IRodinProject prj = mch.getRodinProject();
		IContextRoot[] ctxs;
		try {
			ctxs = prj
					.getRootElementsOfType(IContextRoot.ELEMENT_TYPE);
			for (IContextRoot ctx : ctxs) {
				IRodinFile rodinFile = ctx.getRodinFile();
				if (rodinFile.hasUnsavedChanges())
					rodinFile.makeConsistent(monitor);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
		}
		
		monitor.worked(1);
		monitor.done();
	}

}
