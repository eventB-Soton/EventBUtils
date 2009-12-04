/*******************************************************************************
 * Copyright (c) 2009 University of Southampton and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    University of Southampton - Implementation
 *******************************************************************************/ 
package ch.ethz.eventb.internal.decomposition.bstyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IGuard;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
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
import ch.ethz.eventb.internal.decomposition.DecompositionUtils;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author renatosilva
 *
 */
public class BStyleUtils extends DecompositionUtils{

	
	/**
	 * Utility method to decompose a model.
	 * 
	 * @param modelDecomp
	 *            a model decomposition
	 * @param monitor
	 *            the progress monitor
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
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
						+ " (" + subModel.getComponentName() + ")");
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
	 *            the progress monitor
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
		String fileName = "";

		// 1: Create project if necessary
		IEventBProject prj = null;
		if(subModel.getModelDecomposition().createNewProjectDecomposition()){
			subMonitor.subTask(Messages.decomposition_project);
			prj = EventBUtils.createProject(subModel.getComponentName(), subMonitor.newChild(1));
			fileName = src.getElementName();
		}
		else {
			prj = src.getEventBProject();
			fileName = subModel.getComponentName();
		}
		checkCancellation(subMonitor);

		// 2: Create machine.
		subMonitor.subTask(Messages.decomposition_machine);
		final IMachineRoot dest = EventBUtils.createMachine(prj, fileName, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// 3: Create variables.
		subMonitor.subTask(Messages.decomposition_variables);
		BStyleUtils.decomposeVariables(dest, subModel, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// 4: Create invariants.
		subMonitor.subTask(Messages.decomposition_invariants);
		BStyleUtils.decomposeInvariants(dest, subModel, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// 5: Create events.
		subMonitor.subTask(Messages.decomposition_events);
		BStyleUtils.decomposeEvents(dest, subModel, subMonitor.newChild(1));
		checkCancellation(subMonitor);

		// 6: Copy or decompose contexts from the original project
		// 7: Make SEES clauses.
		switch (contextDecomp) {
		case NO_DECOMPOSITION:
			subMonitor.subTask(Messages.decomposition_contextsCopy);
			if(subModel.getModelDecomposition().createNewProjectDecomposition()){
				EventBUtils.copyContexts(src.getEventBProject(), prj, src, subMonitor.newChild(1));
				checkCancellation(subMonitor);
			}
			
			subMonitor.subTask(Messages.decomposition_seesclauses);
			EventBUtils.copySeesClauses(src, dest, subMonitor.newChild(1));
			checkCancellation(subMonitor);
			break;
		case MINIMAL_FLATTENED_CONTEXT:
			final String contextName = EventBUtils.makeLabel(Messages.label_decomposedContextName,fileName); // //Messages.label_decomposedContextName;
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
	
	/**
	 * Utility method to create variables in an input machine for a given
	 * sub-model. This is done by partition the variables to the respective machine.
	 * 
	 * @param mch
	 *            a machine
	 * @param subModel
	 *            the sub-model
	 * @param monitor
	 *            the progress monitor
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static void decomposeVariables(IMachineRoot mch, ISubModel subModel,IProgressMonitor monitor) throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 5);
		final IRodinElement[] elements = subModel.getElements();
		final IMachineRoot nonDecompMachine = subModel.getModelDecomposition().getMachineRoot();
		
		for (IRodinElement element : elements) {
			if (! (element instanceof IVariable)) {
				throw new IllegalArgumentException("Variable Decomposition: variable expected as sub-model element"); //$NON-NLS-1$
			}
			for (IVariable variable : nonDecompMachine.getVariables()) {
				if (variable.getIdentifierString().equals(((IVariable) element).getIdentifierString())) {
					createVariable(mch, variable.getIdentifierString(), subMonitor);
				}
			}
			subMonitor.worked(1);
			checkCancellation(subMonitor);
		}
	}
	
	/**
	 * Utility method to create a variable with the specified identifier
	 * in a machine.
	 * 
	 * @param mch
	 *            a machine
	 * @param ident
	 *            the identifier of the variable
	 * @return the newly created shared variable
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	private static IVariable createVariable(final IMachineRoot mch,final String ident, SubMonitor subMonitor) throws RodinDBException {
		subMonitor.setWorkRemaining(2);
		final IVariable var = mch.createChild(IVariable.ELEMENT_TYPE, null,subMonitor.newChild(1));
		var.setIdentifierString(ident, subMonitor.newChild(1));

		return var;
	}
	
	/**
	 * creates the minimal flattened context required by dest
	 * return false iff no context has been created
	 * 
	 * TODO: maybe pass this method to the {@link DecompositionUtils} since it is equal in both styles
	 */ 
	private static boolean createFlattenedContext(IMachineRoot dest,
			ISubModel subModel, IEventBProject prj, SubMonitor subMonitor,
			String contextName) throws RodinDBException {
		subMonitor.setWorkRemaining(10);
		final IContextRoot ctx = EventBUtils.createContext(dest.getEventBProject(), contextName, subMonitor.newChild(1));
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
	 * Utility method to check that the decomposition is authorized.
	 * 
	 * @return <tt>true</tt> if and only if the decomposition is authorized
	 * @param monitor
	 *            the progress monitor
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static boolean check(IModelDecomposition modelDecomp,IProgressMonitor monitor) throws RodinDBException {
//		// Checks that the INITIALISATION events of the sub-machines will not
//		// define an action modifying at the same time a private variable and a
//		// shared variable.
//		// An INITIALISATION event of a sub-machine will contain such an action
//		// if and only if the INITIALISATION event of the non-decomposed machine
//		// defines an action modifying a private variable and a shared variable
//		// which will be distributed in the same sub-machine (i.e. are both
//		// accessed in this sub-machine).
//		IEvent init = EventBUtils.getInitialisation(modelDecomp.getMachineRoot());
//		Set<String> sharedVariables = getSharedVariables(modelDecomp, monitor);
//		for (ISubModel subModel : modelDecomp.getSubModels()) {
//			Set<String> accessedVariables = getAccessedVariables(subModel,
//					monitor);
//			for (IAction action : init.getActions()) {
//				List<String> assignedIdentifiers = EventBUtils
//						.getAssignedIdentifiers(action);
//				accessedVariables.retainAll(assignedIdentifiers);
//				sharedVariables.retainAll(accessedVariables);
//				int nbVar = accessedVariables.size();
//				if ((nbVar > 1) && (nbVar > sharedVariables.size())) {
//					throw new IllegalArgumentException(Messages.bind(
//							Messages.scuser_ActionOnPrivateAndSharedError,
//							action.getLabel()));
//				}
//			}
//		}
		return true;
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
		final Set<String> vars = getVariablesSubModel(subModel, subMonitor.newChild(1));
		
		// Create the typing theorems.
		createTypingTheorems(mch, src, vars, subMonitor.newChild(1));
		checkCancellation(subMonitor);
		
		// Copy relevant invariants.
		EventBUtils.copyInvariants(mch, src, vars, subMonitor.newChild(1));
		checkCancellation(subMonitor);
	}
	
	/**
	 * Utility method to create the event in an input machine for a given
	 * sub-model.
	 * 
	 * @param dest
	 *            the destination machine
	 * @param subModel
	 *            a sub-model
	 * @param monitor
	 *            the progress monitor
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static void decomposeEvents(IMachineRoot dest, ISubModel subModel,
			IProgressMonitor monitor) throws RodinDBException {
		final IMachineRoot src = subModel.getMachineRoot();
		final IEvent[] events = src.getEvents();
		final SubMonitor subMonitor = SubMonitor.convert(monitor,2 * events.length);
		for (IEvent evt : events) {
			createEvent(dest,subModel,evt,subMonitor.newChild(1));
			checkCancellation(subMonitor);
		}

	}
	
	/**
	 * Utility method to check if an event belongs to a given a sub-model.
	 * Criteria: allocated variables are part of guard or action
	 * 
	 * @param subModel
	 *            a sub-model
	 * @param evt
	 *            an event
	 * @param monitor
	 *            the progress monitor
	 * @return the newly created event or null if no event is created
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	private static boolean checkEvent(ISubModel subModel, IEvent evt, IProgressMonitor monitor)
			throws RodinDBException {
		boolean addEvent = false;
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 5);
		IMachineRoot src = subModel.getModelDecomposition().getMachineRoot();
		
		final List<String> idents = EventBUtils.getFreeIdentifiers(evt, subMonitor.newChild(1));
		idents.removeAll(EventBUtils.getSeenCarrierSetsAndConstants(src));
//		for (IParameter param : evt.getParameters()) {
//			idents.remove(param.getIdentifierString());
//		}
		
//		final IGuard[] guards = evt.getGuards();
//		final IAction[] acts = evt.getActions();
		
		IRodinElement[] elements = subModel.getElements();
		for(IRodinElement element: elements){
			IVariable variable = (IVariable) element;
			
			if(idents.contains(variable.getIdentifierString())){
				addEvent = true;
				break;
			}
			
			//Check if variable is part of any of the guards or actions
//			for(IGuard guard: guards){
//				String predicateStr = guard.getPredicateString();
//				List<String> predicateFreeIdentifiers = EventBUtils.getPredicateFreeIdentifiers(predicateStr);
//				
//			}
//			
//			//Check if variable is part of any of the actions
//			for(IAction act: acts){
//				String assignmentStr = act.getAssignmentString();
//				List<String> assignmentFreeIdentifiers = EventBUtils.getAssignmentFreeIdentifiers(assignmentStr);
//				checkCancellation(subMonitor);
//			}
			
			
		}
		
		return addEvent;
		
	}
	
	

	
	/**
	 * Utility method to create an event in the input machine from an
	 * input event, given a sub-model.
	 * 
	 * @param mch
	 *            the destination machine
	 * @param subModel
	 *            a sub-model
	 * @param evt
	 *            an event
	 * @param monitor
	 *            the progress monitor
	 * @return the newly created event
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	private static IEvent createEvent(IMachineRoot mch,
			ISubModel subModel, IEvent evt, IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 20);
		// Flatten the original event.
		final IEvent flattened = EventBUtils.flatten(evt, subMonitor.newChild(2));
		
		final boolean createEvent = checkEvent(subModel, flattened, subMonitor.newChild(5));
		
		if(!createEvent)
			return null;

		// Create the new event.
		final IEvent newEvt = mch.createChild(IEvent.ELEMENT_TYPE, null,subMonitor.newChild(1));
		newEvt.setLabel(flattened.getLabel(), subMonitor.newChild(1));

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

		// Editing missing parameters and guards.
		editExtraParametersAndGuards(subModel,newEvt, vars, subMonitor.newChild(5));
		checkCancellation(subMonitor);

		return newEvt;
	}
	
	/**
	 * Utility method to edit parameters corresponding to variables
	 * that are used by the decomposed event but are not accessed by the
	 * sub-model. The additional guards are the typing theorems for those
	 * parameters. This is the last step when decomposing an event.
	 * 
	 * @param src
	 *            the source machine for getting the typing theorem.
	 * @param evt
	 *            the current decomposed event.
	 * @param vars
	 *            the set of accessed variables.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private static void editExtraParametersAndGuards(final ISubModel subModel,
			IEvent evt, Set<String> vars, IProgressMonitor monitor)
			throws RodinDBException {
		final IMachineRoot src = subModel.getMachineRoot();
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 10);
		
		final Set<String> idents = new HashSet<String>(EventBUtils.getFreeIdentifiers(evt, subMonitor.newChild(1)));
		Set<String> parameters = new HashSet<String>();
		idents.removeAll(EventBUtils.getSeenCarrierSetsAndConstants(src));
		idents.removeAll(vars);
		for (IParameter param : evt.getParameters()) {
			parameters.add(param.getIdentifierString());
		}
		subMonitor.setWorkRemaining(8 * idents.size());
		
		for(IGuard guard: evt.getGuards()){
			if(EventBUtils.isRelevant(guard, idents, EventBUtils.getSeenCarrierSetsAndConstants(src))){
				guard.delete(true, subMonitor.newChild(1));
			}
			else if(!idents.isEmpty()){
				idents.addAll(parameters);
				if(EventBUtils.isRelevant(guard, idents, EventBUtils.getSeenCarrierSetsAndConstants(src))){
					
					IGuard fstGrd = null;
					final IGuard[] grds = evt.getGuards();
					if (grds.length != 0) {
						fstGrd = grds[0];
					}
					
					//If a guard containing a parameter is deleted, we add a typing theorem guard
					for (String ident : parameters) {
						final String typThm = EventBUtils.getTypingTheorem(src, evt, ident);
						final IGuard newGrd = evt.createChild(IGuard.ELEMENT_TYPE, fstGrd,subMonitor.newChild(1));
						newGrd.setLabel(EventBUtils.makeTypingLabel(ident), subMonitor.newChild(1));
						newGrd.setPredicateString(typThm, subMonitor.newChild(1));
						newGrd.setTheorem(true, subMonitor.newChild(1));
					}
					guard.delete(true, subMonitor.newChild(1));
					idents.removeAll(parameters);
				}
			}
		}
	}
	
	/**
	 * Utility method to create actions in a destination event from a source
	 * event and a set of accessed variables.
	 * 
	 * @param dest
	 *            the destination event
	 * @param src
	 *            the source event
	 * @param vars
	 *            the accessed variables
	 * @param monitor
	 *            the progress monitor
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	private static void decomposeActions(IEvent dest, IEvent src,
			Set<String> vars, IProgressMonitor monitor) throws RodinDBException {
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
	 *            an input action
	 * @param vars
	 *            a set of variables (in {@link String}
	 * @param monitor
	 *            the progress monitor
	 * @return the created action
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static String decomposeAction(final IAction act,
			final Set<String> vars, IProgressMonitor monitor)
			throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 2);

		// Parsing the assignment string and getting assigned variables.
		String assignmentStr = act.getAssignmentString();
		Assignment parseAssignment = Lib.parseAssignment(assignmentStr);
		FreeIdentifier[] assignedVars = parseAssignment.getAssignedIdentifiers();
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
			String newAssignmentStr = EventBUtils.identsToCSVString(v.toArray(new FreeIdentifier[v.size()]));
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
			String wPrimedList = EventBUtils.identsToPrimedCSVString(w
					.toArray(new FreeIdentifier[w.size()]));
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




}
