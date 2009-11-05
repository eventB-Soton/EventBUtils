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

package ch.ethz.eventb.internal.decomposition.astyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.BecomesSuchThat;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.SourceLocation;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.decomposition.ISubModel;
import ch.ethz.eventb.decomposition.IModelDecomposition.ContextDecomposition;
import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement.Nature;
import ch.ethz.eventb.internal.decomposition.DecompositionUtils;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         Class containing useful methods to perform A-style decomposition.
 *         </p>
 */
public final class AStyleUtils extends DecompositionUtils {

	/**
	 * @author htson
	 *         <p>
	 *         Enumerated event types:
	 *         <ul>
	 *         <li>{@value #EXTERNAL}: External events.</li>
	 *         <li>{@value #INTERNAL}: Internal events.</li>
	 *         <li>{@value #NONE}: Events that do not access any variables of
	 *         the decomposition.</li>
	 *         </ul>
	 *         </p> {@see AStyleUtils#getEventType(ISubModel, IEvent)}.
	 */
	public enum DecomposedEventType {
		/** The external type. */
		EXTERNAL(2),

		/** The internal type. */
		INTERNAL(1),

		/** No type. */
		NONE(0);

		/** The code of the type. */
		private final int code;

		/**
		 * Constructor.
		 * 
		 * @param code
		 *            the internal code.
		 */
		DecomposedEventType(final int code) {
			this.code = code;
		}

		/**
		 * Returns the internal code.
		 * 
		 * @return the internal code.
		 */
		public int getCode() {
			return code;
		}
	}

	/**
	 * Utility method to decompose a model.
	 * 
	 * @param modelDecomp
	 *            a model decomposition.
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 * @throws DecompositionCanceledException
	 *             if the monitor is canceled
	 */
	public static void decompose(IModelDecomposition modelDecomp,
			IProgressMonitor monitor) throws RodinDBException {
		final ISubModel[] subModels = modelDecomp.getSubModels();
		try {
			// The number of works is the number of sub-models.
			final SubMonitor subMonitor = SubMonitor.convert(monitor,
					Messages.decomposition_description, subModels.length);

			for (ISubModel subModel : subModels) {
				subMonitor.setTaskName(Messages.decomposition_description
						+ " (" + subModel.getProjectName() + ")");
				// For each distribution, create the corresponding model.
				subMonitor.subTask(Messages.decomposition_submodel);
				createSubModel(subModel, modelDecomp.getContextDecomposition(),
						subMonitor.newChild(1));
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Utility method to create a sub-model.
	 * 
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
	 * @throws DecompositionCanceledException
	 *             if the monitor is canceled
	 */
	private static void createSubModel(ISubModel subModel,
			ContextDecomposition contextDecomp, SubMonitor subMonitor)
			throws RodinDBException {
		// Monitor has 8 works.
		subMonitor.setWorkRemaining(8);
		final IMachineRoot src = subModel.getMachineRoot();

		// 1: Create project
		subMonitor.subTask(Messages.decomposition_project);
		final IEventBProject prj = EventBUtils.createProject(subModel
				.getProjectName(), subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// 2: Create machine.
		subMonitor.subTask(Messages.decomposition_machine);
		final IMachineRoot dest = EventBUtils.createMachine(prj,
				src.getElementName(), subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// 3: Create variables.
		subMonitor.subTask(Messages.decomposition_variables);
		AStyleUtils.decomposeVariables(dest, subModel, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// 4: Create invariants.
		subMonitor.subTask(Messages.decomposition_invariants);
		AStyleUtils.decomposeInvariants(dest, subModel, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// 5: Create events.
		subMonitor.subTask(Messages.decomposition_external);
		AStyleUtils.decomposeEvents(dest, subModel, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// 6: Copy or decompose contexts from the original project
		// 7: Make SEES clauses.
		switch (contextDecomp) {
		case NO_DECOMPOSITION:
			subMonitor.subTask(Messages.decomposition_contextsCopy);
			EventBUtils.copyContexts(src.getEventBProject(), prj, subMonitor
					.newChild(1));
			checkCancellation(subMonitor);
			subMonitor.subTask(Messages.decomposition_seesclauses);
			EventBUtils.copySeesClauses(src, dest, subMonitor.newChild(1));
			checkCancellation(subMonitor);
			break;
		case MINIMAL_FLATTENED_CONTEXT:
			final String contextName = Messages.label_decomposedContextName;
			subMonitor.subTask(Messages.decomposition_contextsDecompose);
			final boolean contextCreated = createFlattenedContext(dest,
					subModel, prj, subMonitor.newChild(1), contextName);
			checkCancellation(subMonitor);
			subMonitor.subTask(Messages.decomposition_seesclauses);
			if (contextCreated) {
				EventBUtils.createSeesClause(dest, contextName, null,
						subMonitor.newChild(1));
			}
			subMonitor.setWorkRemaining(1);
			checkCancellation(subMonitor);
			break;
		default:
			throw new IllegalStateException(
					Messages.decomposition_error_contextDecompositionKind);
		}

		// 8: Save the resulting sub-model.
		subMonitor.subTask(Messages.decomposition_saving);
		dest.getRodinFile().save(subMonitor.newChild(1), false);
		checkCancellation(subMonitor);
	}

	// creates the minimal flattened context required by dest
	// return false iff no context has been created
	private static boolean createFlattenedContext(IMachineRoot dest,
			ISubModel subModel, IEventBProject prj, SubMonitor subMonitor,
			String contextName) throws RodinDBException {
		subMonitor.setWorkRemaining(10);
		final IContextRoot ctx = EventBUtils.createContext(dest
				.getEventBProject(), contextName, subMonitor
				.newChild(1));
		decomposeContext(ctx, dest, subModel, subMonitor.newChild(8));
		checkCancellation(subMonitor);
		if (!ctx.hasChildren()) {
			// empty context => remove it and do not create sees clause
			ctx.getRodinFile().delete(false, subMonitor.newChild(1));
			return false;
		}
		ctx.getRodinFile().save(subMonitor.newChild(1), false);
		return true;
	}

	/**
	 * Utility method to create variables in an input machine for a given
	 * sub-model. This is done by copying the accessed variables to the machine
	 * as well as the shared variables (
	 * {@link #createSharedVariable(IMachineRoot, String)}) or as private
	 * variables ({@link #createPrivateVariable(IMachineRoot, String)}).
	 * 
	 * @param mch
	 *            a machine.
	 * @param subModel
	 *            the sub-model.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void decomposeVariables(IMachineRoot mch, ISubModel subModel,
			IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 5);
		Set<String> accessedVars = getAccessedVariables(subModel, subMonitor.newChild(1));
		IModelDecomposition modelDecomp = subModel.getModelDecomposition();
		Collection<String> sharedVars = getSharedVariables(modelDecomp, subMonitor.newChild(1));
		subMonitor.setWorkRemaining(accessedVars.size());
		for (String var : accessedVars) {
			subMonitor.subTask(Messages.decomposition_variable + var);
			if (sharedVars.contains(var)) {
				createSharedVariable(mch, var, subMonitor.newChild(1));
			} else {
				createPrivateVariable(mch, var, subMonitor.newChild(1));
			}
			checkCancellation(subMonitor);
		}
	}

	/**
	 * Gets the set of variables shared between the sub-models of the
	 * decomposition (in {@link String}).
	 * 
	 * @param modelDecomp
	 *            the model decomposition
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @return the labels of the shared variables
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static Set<String> getSharedVariables(
			final IModelDecomposition modelDecomp, IProgressMonitor monitor)
			throws RodinDBException {
		return getSharedVariables(modelDecomp.getSubModels(), monitor);
	}

	/**
	 * Gets the set of variables shared between the sub-models of the
	 * decomposition (in {@link String}).
	 * 
	 * @param subModels
	 *            the sub-models
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @return the labels of the shared variables
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static Set<String> getSharedVariables(ISubModel[] subModels,
			IProgressMonitor monitor) throws RodinDBException {
		if (subModels.length == 0) {
			return Collections.emptySet();
		}
		Set<String> sharedVars = new HashSet<String>();
		IMachineRoot nonDecomposedMachine = subModels[0].getMachineRoot();
		final IVariable[] variables = nonDecomposedMachine.getVariables();
		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				variables.length * subModels.length);
		for (IVariable var : variables) {
			int occurrence = 0;
			String ident = var.getIdentifierString();
			for (ISubModel subModel : subModels) {
				final Set<String> accessedVariables = getAccessedVariables(subModel, subMonitor.newChild(1));
				if (accessedVariables.contains(ident)) {
					occurrence++;
				}
				if (occurrence > 1) {
					break;
				}
			}
			if (occurrence > 1) {
				sharedVars.add(ident);
			}
			checkCancellation(subMonitor);
		}
		return sharedVars;
	}

	/**
	 * Utility method to create a shared variable with the specified identifier
	 * in a machine.
	 * 
	 * @param mch
	 *            a machine.
	 * @param ident
	 *            the identifier of the variable.
	 * @return the newly created shared variable.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private static IVariable createSharedVariable(final IMachineRoot mch,
			final String ident, SubMonitor subMonitor)
			throws RodinDBException {
		subMonitor.setWorkRemaining(4);
		final IVariable var = mch.createChild(IVariable.ELEMENT_TYPE, null,
				subMonitor.newChild(1));
		var.setIdentifierString(ident, subMonitor.newChild(1));
		var.setComment(Messages.decomposition_shared_comment, subMonitor
				.newChild(1));
		final INatureElement elt = (INatureElement) var
				.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, subMonitor.newChild(1));
		return var;
	}

	/**
	 * Utility method to create a private variable with the specified identifier
	 * in a machine.
	 * 
	 * @param mch
	 *            a machine.
	 * @param ident
	 *            the identifier of the variable.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @return the newly created private variable.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private static IVariable createPrivateVariable(IMachineRoot mch,
			String ident, IProgressMonitor monitor) throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 4);
		final IVariable var = mch.createChild(IVariable.ELEMENT_TYPE, null,
				subMonitor.newChild(1));
		var.setIdentifierString(ident, subMonitor.newChild(1));
		var.setComment(Messages.decomposition_private_comment, subMonitor
				.newChild(1));
		final INatureElement elt = (INatureElement) var
				.getAdapter(INatureElement.class);
		elt.setNature(Nature.PRIVATE, subMonitor.newChild(1));
		return var;
	}

	/**
	 * Utility method to create the event in an input machine for a given
	 * sub-model.
	 * 
	 * @param dest
	 *            the destination machine.
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
	public static void decomposeEvents(IMachineRoot dest,
			ISubModel subModel, IProgressMonitor monitor)
			throws RodinDBException {
		final IMachineRoot src = subModel.getMachineRoot();
		final IEvent[] events = src.getEvents();
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 2*events.length);
		for (IEvent evt : events) {
			DecomposedEventType type = getEventType(subModel, evt, subMonitor.newChild(1));
			if (type == DecomposedEventType.EXTERNAL) {
				createExternalEvent(dest, subModel, evt, subMonitor.newChild(1));
			} else if (type == DecomposedEventType.INTERNAL) {
				createInternalEvent(dest, evt, subMonitor.newChild(1));
			}
			checkCancellation(subMonitor);
		}

	}

	/**
	 * Utility method to get the type of an event for a given sub-model.
	 * <ul>
	 * <li>An event is {@link DecomposedEventType#INTERNAL} if it belongs to the
	 * sub-model.</li>
	 * <li>An event is {@link DecomposedEventType#EXTERNAL} if it does not
	 * belong to the sub-model, but accesses some variables of a sub-model.</li>
	 * <li>Otherwise (i.e. the event does not access any variable of the
	 * sub-model), the type of the event is {@link DecomposedEventType#NONE} and
	 * will be ignore when creating the decomposed model.</li>
	 * </ul>
	 * Note: The initialization event is an external event.
	 * 
	 * @param subModel
	 *            a subModel.
	 * @param evt
	 *            an event
	 * @return the type of the input event according to the element
	 *         distribution.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static DecomposedEventType getEventType(ISubModel subModel,
			IEvent evt, IProgressMonitor monitor) throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 4);
		// Initialization event
		if (evt.isInitialisation()) {
			return DecomposedEventType.EXTERNAL;
		}

		// Internal event
		for (IRodinElement element : subModel.getElements()) {
			if (evt.getLabel().equals(((IEvent) element).getLabel())) {
				return DecomposedEventType.INTERNAL;
			}
		}

		// External event
		final List<String> modifiedVariables = EventBUtils
				.getAssignedIdentifiers(evt, subMonitor.newChild(1));
		final Set<String> sharedVariables = getSharedVariables(subModel
				.getModelDecomposition(), subMonitor.newChild(1));
		final Set<String> accessedVariables = getAccessedVariables(subModel,
				subMonitor.newChild(1));
		for (String sharedVariable : sharedVariables) {
			// If the event modifies a shared variable
			if (modifiedVariables.contains(sharedVariable)) {
				// which is accessed in the sub-model
				if (accessedVariables.contains(sharedVariable)) {
					// then the event is tagged as external
					subMonitor.setWorkRemaining(0);
					return DecomposedEventType.EXTERNAL;
				}

			}
		}
		subMonitor.setWorkRemaining(0);
		return DecomposedEventType.NONE;
	}

	/**
	 * Utility method to create an external event in the input machine from an
	 * input event, given a sub-model.
	 * 
	 * @param mch
	 *            the destination machine
	 * @param subModel
	 *            a sub-model
	 * @param evt
	 *            an event
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @return the newly created event
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	private static IEvent createExternalEvent(IMachineRoot mch,
			ISubModel subModel, IEvent evt, IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 20);
		// Flatten the original event.
		final IEvent flattened = EventBUtils.flatten(evt, subMonitor.newChild(2));

		// Create the new event.
		final IEvent newEvt = mch.createChild(IEvent.ELEMENT_TYPE, null, subMonitor.newChild(1));
		newEvt.setLabel(flattened.getLabel(), subMonitor.newChild(1));
		if (!evt.isInitialisation()) {
			newEvt.setComment(Messages.decomposition_external_comment, subMonitor.newChild(1));

			// Set the external attribute.
			IExternalElement elt = (IExternalElement) newEvt
					.getAdapter(IExternalElement.class);
			elt.setExternal(true, subMonitor.newChild(1));
		}
		subMonitor.setWorkRemaining(14);
		checkCancellation(subMonitor);

		// Set the status.
		setEventStatus(evt, newEvt, subMonitor.newChild(1));
		checkCancellation(subMonitor);
		
		// Copying the parameters from the source event.
		EventBUtils.copyParameters(newEvt, flattened, null, subMonitor.newChild(1));
		checkCancellation(subMonitor);
		
		// Copying the guards from the source event.
		EventBUtils.copyGuards(newEvt, flattened, null, subMonitor.newChild(1));
		checkCancellation(subMonitor);
		
		// Decomposing actions.
		Set<String> vars = getAccessedVariables(subModel, subMonitor.newChild(1));
		decomposeActions(newEvt, flattened, vars, subMonitor.newChild(5));

		// Creating missing parameters and guards.
		EventBUtils.createExtraParametersAndGuards(subModel.getMachineRoot(),
				newEvt, vars, subMonitor.newChild(5));
		checkCancellation(subMonitor);

		return newEvt;
	}

	/**
	 * Utility method to create actions in a destination event from a source
	 * event and a set of accessed variables.
	 * 
	 * @param dest
	 *            the destination event.
	 * @param src
	 *            the source event.
	 * @param vars
	 *            the accessed variables.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private static void decomposeActions(IEvent dest, IEvent src,
			Set<String> vars, IProgressMonitor monitor)
			throws RodinDBException  {
		final IAction[] acts = src.getActions();
		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				4 * acts.length);
		for (IAction act : acts) {
			final String newAssignmentStr = decomposeAction(act, vars,
					subMonitor.newChild(1));
			if (newAssignmentStr == null) {
				continue;
			}
			final IAction newAct = dest.createChild(IAction.ELEMENT_TYPE, null,
					subMonitor.newChild(1));
			newAct.setLabel(act.getLabel(), subMonitor.newChild(1));
			newAct.setAssignmentString(newAssignmentStr, subMonitor.newChild(1));
			checkCancellation(subMonitor);
		}
	}

	/**
	 * Utility method to decompose an action according to a set of given
	 * accessed variables.
	 * 
	 * @param act
	 *            an input action.
	 * @param vars
	 *            a set of variables (in {@link String}.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @return the created action.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static String decomposeAction(final IAction act,
			final Set<String> vars, IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 2);

		// Parsing the assignment string and getting assigned variables.
		String assignmentStr = act.getAssignmentString();
		Assignment parseAssignment = Lib.parseAssignment(assignmentStr);
		FreeIdentifier[] assignedVars = parseAssignment
				.getAssignedIdentifiers();
		checkCancellation(subMonitor);
		
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
			checkCancellation(subMonitor);
		}
		subMonitor.worked(1);
		
		// Return nothing if it does not modify any accessed variables.
		// This covers the cases for
		// w :: E(v, w)
		// w :| P(v, w, w')
		// w := E(v, w)
		// w(E) := F
		if (v.isEmpty()) {
			return null;
		}

		// Do nothing if all assigned variables are accessed variables.
		// This covers the cases for
		// v :: E(v, w)
		// v :| P(v, w, w')
		// v := E(v, w)
		// v(E) := F
		if (w.isEmpty()) {
			return act.getAssignmentString();
		}

		// v, w := E(v,w), F(v, w) => v := E(v, w)
		if (parseAssignment instanceof BecomesEqualTo) {
			BecomesEqualTo bcmeq = (BecomesEqualTo) parseAssignment;
			Expression[] exps = bcmeq.getExpressions();
			assert exps.length == assignedVars.length;
			String newAssignmentStr = EventBUtils.identsToCSVString(
					v.toArray(new FreeIdentifier[v.size()]));
			newAssignmentStr += " ≔ "; //$NON-NLS-1$
			boolean fst = true;
			for (int i = 0; i < exps.length; i++) {
				FreeIdentifier ident = assignedVars[i];
				if (v.contains(ident)) {
					if (fst) {
						fst = false;
					} else {
						newAssignmentStr += ", "; //$NON-NLS-1$
					}
					newAssignmentStr += exps[i];
				}
				checkCancellation(subMonitor);
			}
			subMonitor.worked(1);
			return newAssignmentStr;
		}

		// v, w :| P(v',w') ==> v :| #w'.P(v',w')
		else {
			assert parseAssignment instanceof BecomesSuchThat;
			BecomesSuchThat bcmsuch = (BecomesSuchThat) parseAssignment;
			Predicate P = bcmsuch.getCondition();
			String vList = EventBUtils.identsToCSVString(v
					.toArray(new FreeIdentifier[v.size()]));
			String wPrimedList = EventBUtils.identsToPrimedCSVString(
					w.toArray(new FreeIdentifier[w.size()]));
			checkCancellation(subMonitor);
			
			SourceLocation srcLoc = P.getSourceLocation();
			String strP = assignmentStr.substring(srcLoc.getStart(), srcLoc
					.getEnd() + 1);
			String newAssignmentStr = vList + " :∣ ∃" + wPrimedList + "·" //$NON-NLS-1$ //$NON-NLS-2$
					+ strP;
			subMonitor.worked(1);
			return newAssignmentStr;
		}
	}

	/**
	 * Utility method to create an internal event in a destination machine from
	 * a source event.
	 * 
	 * @param mch
	 *            the destination machine
	 * @param evt
	 *            the source event
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @return the newly created event
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	private static IEvent createInternalEvent(IMachineRoot mch, IEvent evt,
			IProgressMonitor monitor) throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 4);
		// Flatten the original event.
		final IEvent flattened = EventBUtils.flatten(evt, subMonitor
				.newChild(5));
		checkCancellation(subMonitor);

		// Create the new event.
		final IEvent newEvt = mch.createChild(IEvent.ELEMENT_TYPE, null,
				subMonitor.newChild(1));
		newEvt.setLabel(flattened.getLabel(), subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// Set the external attribute.
		final IExternalElement elt = (IExternalElement) newEvt
				.getAdapter(IExternalElement.class);
		elt.setExternal(false, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// Set the status.
		setEventStatus(evt, newEvt, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// Copy the parameters.
		
		EventBUtils.copyParameters(newEvt, flattened, null, subMonitor
				.newChild(1));

		EventBUtils.copyGuards(newEvt, flattened, null, subMonitor.newChild(1));

		EventBUtils.copyActions(newEvt, flattened, null, subMonitor.newChild(1));

		return newEvt;
	}
}
