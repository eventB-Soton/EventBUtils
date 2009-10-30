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

package ch.ethz.eventb.internal.decomposition.astyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.ISeesContext;
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
	enum DecomposedEventType {
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

	private final static IProgressMonitor monitor = new NullProgressMonitor();

	/**
	 * Utility method to decompose a model.
	 * 
	 * @param modelDecomp
	 *            a model decomposition.
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void decompose(final IModelDecomposition modelDecomp,
			final IProgressMonitor monitor) throws RodinDBException {
		ISubModel[] subModels = modelDecomp.getSubModels();

		// The number of works is the number of sub-models.
		monitor.beginTask(Messages.decomposition_description, subModels.length);
		for (ISubModel subModel : subModels) {
			// For each distribution, create the corresponding model.
			monitor.subTask(Messages.decomposition_submodel);
			createSubModel(subModel, modelDecomp.getContextDecomposition(), new SubProgressMonitor(monitor, 1));
		}
		monitor.done();
		return;
	}

	/**
	 * Utility method to create a sub-model.
	 * 
	 * @param subModel
	 *            a sub-model.
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private static void createSubModel(final ISubModel subModel, ContextDecomposition contextDecomp,
			final SubProgressMonitor monitor) throws RodinDBException {
		// TODO manage monitor cancellation
		
		// Monitor has 8 works.
		monitor.beginTask(Messages.decomposition_submodel, 8);
		IMachineRoot src = subModel.getMachineRoot();

		// 1: Create project
		monitor.subTask(Messages.decomposition_project);
		IEventBProject prj = EventBUtils.createProject(subModel
				.getProjectName(), monitor);
		monitor.worked(1);

		// 2: Create machine.
		monitor.subTask(Messages.decomposition_machine);
		IMachineRoot dest = EventBUtils.createMachine(prj,
				src.getElementName(), monitor);
		monitor.worked(1);

		// 3: Create variables.
		monitor.subTask(Messages.decomposition_variables);
		AStyleUtils.decomposeVariables(dest, subModel, new SubProgressMonitor(
				monitor, 1));

		// 4: Create invariants.
		monitor.subTask(Messages.decomposition_invariants);
		AStyleUtils.decomposeInvariants(dest, subModel, new SubProgressMonitor(
				monitor, 1));

		// 5: Create events.
		monitor.subTask(Messages.decomposition_external);
		AStyleUtils.decomposeEvents(dest, subModel, new SubProgressMonitor(
				monitor, 1));

		// 6: Copy or decompose contexts from the original project
		// 7: Make SEES clauses.
		final String flattenedContextName = Messages.label_decomposedContextName; // TODO better idea ?
		switch(contextDecomp) {
		case NO_DECOMPOSITION:
			monitor.subTask(Messages.decomposition_contexts);
			EventBUtils.copyContexts(src.getEventBProject(), prj, monitor);
			monitor.worked(1);

			monitor.subTask(Messages.decomposition_seesclauses);
			EventBUtils.copySeesClauses(src, dest, monitor);
			monitor.worked(1);
			break;
		case MINIMAL_FLATTENED_CONTEXT:
			monitor.subTask(Messages.decomposition_contexts);
			final IContextRoot ctx = EventBUtils.createContext(prj,
					flattenedContextName, monitor);
			decomposeContext(ctx, dest, subModel, new SubProgressMonitor(monitor, 1));
			if (!ctx.hasChildren()) {
				// empty context => remove it and do not create sees clause
				ctx.getRodinFile().delete(false, monitor);
				break;
			}
			ctx.getRodinFile().save(new SubProgressMonitor(monitor, 1), false);

			monitor.subTask(Messages.decomposition_seesclauses);
			final ISeesContext sees = dest.createChild(
					ISeesContext.ELEMENT_TYPE, null, monitor);
			sees.setSeenContextName(flattenedContextName, monitor);
			monitor.worked(1);
			break;
		default:
			assert false;
			break;
		}

		// 8: Save the resulting sub-model.
		dest.getRodinFile().save(new SubProgressMonitor(monitor, 1), false);
		monitor.done();
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
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void decomposeVariables(final IMachineRoot mch,
			final ISubModel subModel, final IProgressMonitor monitor)
			throws RodinDBException {
		Set<String> accessedVars = getAccessedVariables(subModel);
		IModelDecomposition modelDecomp = subModel.getModelDecomposition();
		Collection<String> sharedVars = getSharedVariables(modelDecomp);
		monitor
				.beginTask(Messages.decomposition_variables, accessedVars
						.size());
		for (String var : accessedVars) {
			monitor.subTask(Messages.decomposition_variable + var);
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
	 * Gets the set of variables shared between the sub-models of the
	 * decomposition (in {@link String}).
	 * 
	 * @param modelDecomp
	 *            the model decomposition
	 * @return the labels of the shared variables
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static Set<String> getSharedVariables(
			final IModelDecomposition modelDecomp) throws RodinDBException {
		return getSharedVariables(modelDecomp.getSubModels());
	}

	/**
	 * Gets the set of variables shared between the sub-models of the
	 * decomposition (in {@link String}).
	 * 
	 * @param subModels
	 *            the sub-models
	 * @return the labels of the shared variables
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static Set<String> getSharedVariables(final ISubModel[] subModels)
			throws RodinDBException {
		Set<String> sharedVars = new HashSet<String>();
		if (subModels.length != 0) {
			IMachineRoot nonDecomposedMachine = subModels[0].getMachineRoot();
			for (IVariable var : nonDecomposedMachine.getVariables()) {
				int occurrence = 0;
				String ident = var.getIdentifierString();
				for (ISubModel subModel : subModels) {
					if (getAccessedVariables(subModel).contains(ident)) {
						occurrence++;
					}
					if (occurrence > 1) {
						break;
					}
				}
				if (occurrence > 1) {
					sharedVars.add(ident);
				}
			}
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
			final String ident) throws RodinDBException {
		IVariable var = mch.createChild(IVariable.ELEMENT_TYPE, null, monitor);
		var.setIdentifierString(ident, monitor);
		var.setComment(Messages.decomposition_shared_comment, monitor);
		INatureElement elt = (INatureElement) var
				.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, monitor);
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
	 * @return the newly created private variable.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private static IVariable createPrivateVariable(final IMachineRoot mch,
			final String ident) throws RodinDBException {
		IVariable var = mch.createChild(IVariable.ELEMENT_TYPE, null, monitor);
		var.setIdentifierString(ident, monitor);
		var.setComment(Messages.decomposition_private_comment, monitor);
		INatureElement elt = (INatureElement) var
				.getAdapter(INatureElement.class);
		elt.setNature(Nature.PRIVATE, monitor);
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
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void decomposeEvents(final IMachineRoot dest,
			final ISubModel subModel, final IProgressMonitor monitor)
			throws RodinDBException {
		IMachineRoot src = subModel.getMachineRoot();
		for (IEvent evt : src.getEvents()) {
			DecomposedEventType type = getEventType(subModel, evt);
			if (type == DecomposedEventType.EXTERNAL) {
				createExternalEvent(dest, subModel, evt, monitor);
			} else if (type == DecomposedEventType.INTERNAL) {
				createInternalEvent(dest, evt, monitor);
			}
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
	private static DecomposedEventType getEventType(final ISubModel subModel,
			final IEvent evt) throws RodinDBException {
		if (evt.isInitialisation()) {
			return DecomposedEventType.EXTERNAL;
		}

		for (IRodinElement element : subModel.getElements()) {
			if (evt.getLabel().equals(((IEvent) element).getLabel())) {
				return DecomposedEventType.INTERNAL;
			}
		}

		Collection<String> idents = EventBUtils.getFreeIdentifiers(evt);
		for (String var : getAccessedVariables(subModel)) {
			if (idents.contains(var)) {
				return DecomposedEventType.EXTERNAL;
			}
		}

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
	 *            the progres monitor
	 * @return the newly created event
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	private static IEvent createExternalEvent(final IMachineRoot mch,
			final ISubModel subModel, final IEvent evt,
			final IProgressMonitor monitor) throws RodinDBException {
		// Flatten the original event.
		IEvent flattened = EventBUtils.flatten(evt);

		// Create the new event.
		IEvent newEvt = mch.createChild(IEvent.ELEMENT_TYPE, null, monitor);
		newEvt.setLabel(flattened.getLabel(), monitor);
		if (!evt.isInitialisation()) {
			newEvt.setComment(Messages.decomposition_external_comment, monitor);
			
			// Set the external attribute.
				IExternalElement elt = (IExternalElement) newEvt
						.getAdapter(IExternalElement.class);
				elt.setExternal(true, monitor);		
		}

		// Set the status.
		setEventStatus(evt, newEvt, monitor);

		// Copying the parameters from the source event.
		EventBUtils.copyParameters(newEvt, flattened);

		// Copying the guards from the source event.
		EventBUtils.copyGuards(newEvt, flattened);

		// Decomposing actions.
		Set<String> vars = getAccessedVariables(subModel);
		decomposeActions(newEvt, flattened, vars);

		// Creating missing parameters and guards.
		EventBUtils.createExtraParametersAndGuards(subModel.getMachineRoot(),
				newEvt, vars);

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
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private static void decomposeActions(final IEvent dest, final IEvent src,
			final Set<String> vars) throws RodinDBException {
		IAction[] acts = src.getActions();
		for (IAction act : acts) {
			String newAssignmentStr = decomposeAction(act, vars);
			if (newAssignmentStr == null) {
				continue;
			}
			IAction newAct = dest.createChild(IAction.ELEMENT_TYPE, null,
					monitor);
			newAct.setLabel(act.getLabel(), monitor);
			newAct.setAssignmentString(newAssignmentStr, monitor);
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
	 * @return the created action.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static String decomposeAction(final IAction act,
			final Set<String> vars) throws RodinDBException {

		// Parsing the assignment string and getting assigned variables.
		String assignmentStr = act.getAssignmentString();
		Assignment parseAssignment = Lib.parseAssignment(assignmentStr);
		FreeIdentifier[] assignedVars = parseAssignment
				.getAssignedIdentifiers();

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
					assignmentStr, v.toArray(new FreeIdentifier[v.size()]));
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
			String newAssignmentStr = vList + " :∣ ∃" + wPrimedList + "·" //$NON-NLS-1$ //$NON-NLS-2$
					+ strP;
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
	 *            the progress monitor
	 * @return the newly created event
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	private static IEvent createInternalEvent(final IMachineRoot mch,
			final IEvent evt, final IProgressMonitor monitor)
			throws RodinDBException {
		// Flatten the original event.
		IEvent flattened = EventBUtils.flatten(evt);

		// Create the new event.
		IEvent newEvt = mch.createChild(IEvent.ELEMENT_TYPE, null, monitor);
		newEvt.setLabel(flattened.getLabel(), monitor);

		// Set the external attribute.
		IExternalElement elt = (IExternalElement) newEvt
				.getAdapter(IExternalElement.class);
		elt.setExternal(false, monitor);

		// Set the status.
		setEventStatus(evt, newEvt, monitor);

		// Copy the parameters.
		IParameter[] params = flattened.getParameters();
		for (IParameter param : params) {
			IParameter newParam = newEvt.createChild(IParameter.ELEMENT_TYPE,
					null, monitor);
			newParam.setIdentifierString(param.getIdentifierString(), monitor);
		}

		// Copy the guards.
		IGuard[] grds = flattened.getGuards();
		for (IGuard grd : grds) {
			IGuard newGrd = newEvt.createChild(IGuard.ELEMENT_TYPE, null,
					monitor);
			newGrd.setLabel(grd.getLabel(), monitor);
			newGrd.setPredicateString(grd.getPredicateString(), monitor);
			newGrd.setTheorem(grd.isTheorem(), monitor);
		}

		// Copy the actions.
		IAction[] acts = flattened.getActions();
		for (IAction act : acts) {
			IAction newAct = newEvt.createChild(IAction.ELEMENT_TYPE, null,
					monitor);
			newAct.setLabel(act.getLabel(), monitor);
			newAct.setAssignmentString(act.getAssignmentString(), monitor);
		}

		return newEvt;
	}
}
