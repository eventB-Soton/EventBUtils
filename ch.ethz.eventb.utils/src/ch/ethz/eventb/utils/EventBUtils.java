/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - added context related methods, used symbol tables
 *     Systerel - implemented progress reporting and cancellation support
 *     Extracted from ch.ethz.eventb.decomposition plugin.
 *******************************************************************************/
package ch.ethz.eventb.utils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IAxiom;
import org.eventb.core.IConfigurationElement;
import org.eventb.core.IContextRoot;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IExtendsContext;
import org.eventb.core.IGuard;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IRefinesMachine;
import org.eventb.core.ISeesContext;
import org.eventb.core.IVariable;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.utils.Messages;


/**
 * @author htson
 *         <p>
 *         Utility class containing some useful methods to handle Event-B
 *         elements (<i>e.g.</i> Event-B projects, machines, contexts).
 *         </p>
 */
public final class EventBUtils {

	private final static IRodinDB rodinDB = RodinCore.getRodinDB();

	private EventBUtils() {
		// Utility classes shall not have a public or default constructor.
	}

	// =========================================================================
	// Projects
	// =========================================================================

	/**
	 * Returns the Event-B project with the given name. This is a handle-only
	 * method. The project may or may not exist.
	 * 
	 * @param name
	 *            the name of the Event-B project.
	 * @return The Event-B project with the given name.
	 */
	public static IEventBProject getEventBProject(String name) {
		IRodinProject rodinProject = rodinDB.getRodinProject(name);
		return (IEventBProject) rodinProject.getAdapter(IEventBProject.class);
	}

	// =========================================================================
	// Machines / Contexts
	// =========================================================================

	/**
	 * Utility method to create a new context (*.buc) within an existing
	 * project. The name of the new context is chosen with the specified prefix
	 * so that there is no existing component with the same bare-name. The
	 * default configuration is associated with the new context.
	 * 
	 * @param prj
	 *            The Event-B project.
	 * @param prefix
	 *            Prefix for the new context name.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the handle to newly created context.
	 * @throws RodinDBException
	 *             if there are problems accessing the database.
	 */
	public static IContextRoot createContext(IEventBProject prj, String prefix,
			IProgressMonitor monitor) throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.createContext, 3);

		String name = getFreeComponentName(prj, prefix, subMonitor.newChild(1));

		IRodinFile context = prj.getContextFile(name);
		Assert.isTrue(!context.exists(), Messages.bind(
				Messages.error_existingcontext, context));

		context.create(false, subMonitor.newChild(1));
		IContextRoot root = (IContextRoot) context.getRoot();

		root.setConfiguration(IConfigurationElement.DEFAULT_CONFIGURATION,
				subMonitor.newChild(1));
		return root;
	}

	/**
	 * Utility method to create a new machine (*.bum) within an existing
	 * project. The name of the new machine is chosen with the specified prefix
	 * so that there is no existing component with the same bare-name. The
	 * default configuration is associated with the new machine.
	 * 
	 * @param prj
	 *            The Event-B project.
	 * @param prefix
	 *            Prefix for the new machine name.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the handle to newly created machine.
	 * @throws RodinDBException
	 *             if there are problems accessing the database.
	 */
	public static IMachineRoot createMachine(IEventBProject prj, String prefix,
			IProgressMonitor monitor) throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.createContext, 3);

		String name = getFreeComponentName(prj, prefix, subMonitor.newChild(1));

		IRodinFile machine = prj.getMachineFile(name);
		Assert.isTrue(!machine.exists(), Messages.bind(
				Messages.error_existingmachine, machine));

		machine.create(false, subMonitor.newChild(1));
		IMachineRoot root = (IMachineRoot) machine.getRoot();

		root.setConfiguration(IConfigurationElement.DEFAULT_CONFIGURATION,
				subMonitor.newChild(1));
		return root;
	}

	/*
	 * Get a free component name within a project. A component name is free when
	 * there are no machine or context with the same name already existed.
	 * 
	 * @param prj an Event-B project.
	 * 
	 * @param monitor a progress monitor.
	 * 
	 * @return a free component name with a project.
	 */
	private static String getFreeComponentName(IEventBProject prj,
			String prefix, IProgressMonitor monitor) {
		int index = 0;
		String name = prefix;
		while (checkFreeComponentName(prj, name, monitor)) {
			name = prefix + "_" + index;
			index++;
		}
		return name;
	}

	/*
	 * Check if a component name is a free to be used within a project.
	 * 
	 * @param prj an Event-B project
	 * 
	 * @param name a given component name.
	 * 
	 * @param monitor a progress monitor.
	 * 
	 * @return <code>true</code> if the component name is NOT free,
	 * <code>false</code> otherwise.
	 */
	private static boolean checkFreeComponentName(IEventBProject prj,
			String name, IProgressMonitor monitor) {
		IRodinFile contextFile = prj.getContextFile(name);
		if (contextFile.exists())
			return true;

		IRodinFile machineFile = prj.getMachineFile(name);
		if (machineFile.exists())
			return true;

		return false;
	}

	/**
	 * Creates a new Extends Context clause with the given context name, in the
	 * given context root.
	 * 
	 * @param ctx
	 *            a context root
	 * @param absCtxName
	 *            a bare context name
	 * @param nextSibling
	 *            sibling before which the sees clause should be created, or
	 *            <code>null</code> to create it at the last position
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database
	 */
	public static IExtendsContext createExtendsClause(IContextRoot ctx,
			String absCtxName, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		final IExtendsContext extendCtx = ctx.createChild(
				IExtendsContext.ELEMENT_TYPE, nextSibling, subMonitor
						.newChild(1));
		extendCtx.setAbstractContextName(absCtxName, subMonitor.newChild(1));
		return extendCtx;
	}



	/**
	 * Creates an axiom in the given context with the provided information:
	 * label, predicate string, is Theorem.
	 * 
	 * @param ctx
	 *            a context root.
	 * @param label
	 *            the label of the axiom.
	 * @param predicate
	 *            the predicate string of the axiom.
	 * @param isTheorem
	 *            <code>true</code> if this should be a theorem, otherwise
	 *            <code>false</code>.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the newly created axiom.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IAxiom createAxiom(IContextRoot ctx, String label,
			String predicate, boolean isTheorem, IProgressMonitor monitor)
			throws RodinDBException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 4);
		IAxiom axm = ctx.createChild(IAxiom.ELEMENT_TYPE, null, subMonitor
				.newChild(1));
		axm.setLabel(label, subMonitor.newChild(1));
		axm.setPredicateString(predicate, subMonitor.newChild(1));
		axm.setTheorem(isTheorem, subMonitor.newChild(1));
		return axm;
	}

	/**
	 * Creates a REFINES clause in the given machine with the provided
	 * information: the abstract machine name.
	 * 
	 * @param mch
	 *            a machine root.
	 * @param name
	 *            the abstract machine name of the SEES clause.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the newly created REFINES clause.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IRefinesMachine createRefinesClause(IMachineRoot mch,
			String name, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		IRefinesMachine refinesMch = mch.createChild(
				IRefinesMachine.ELEMENT_TYPE, nextSibling, subMonitor
						.newChild(1));
		refinesMch.setAbstractMachineName(name, subMonitor.newChild(1));
		return refinesMch;
	}

	/**
	 * Creates a SEES clause in the given machine with the provided information:
	 * the seen context name.
	 * 
	 * @param mch
	 *            a machine root.
	 * @param name
	 *            the seen context name of the SEES clause.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the newly created SEES clause.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static ISeesContext createSeesClause(IMachineRoot mch, String name,
			IInternalElement nextSibling, IProgressMonitor monitor)
			throws RodinDBException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		ISeesContext seesCtx = mch.createChild(ISeesContext.ELEMENT_TYPE,
				nextSibling, subMonitor.newChild(1));
		seesCtx.setSeenContextName(name, subMonitor.newChild(1));
		return seesCtx;
	}

	/**
	 * Creates a new variable in the given machine with the provided
	 * information: the identifier string.
	 * 
	 * @param mch
	 *            a machine root.
	 * @param identifier
	 *            the identifier of the new variable.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the newly created variable.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IVariable createVariable(IMachineRoot mch, String identifier,
			IProgressMonitor monitor) throws RodinDBException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		IVariable var = mch.createChild(IVariable.ELEMENT_TYPE, null,
				subMonitor.newChild(1));
		var.setIdentifierString(identifier, subMonitor.newChild(1));
		return var;
	}

	/**
	 * Creates a new invariant in the given machine with the provided
	 * information: the label, the predicate string and boolean flag isThm.
	 * 
	 * @param mch
	 *            a machine root.
	 * @param label
	 *            the label of the new invariant.
	 * @param predicate
	 *            the predicate string of the new invariant.
	 * @param thm
	 *            <code>true</code> if the new invariant should be a theorem,
	 *            otherwise <code>false</code>.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the newly created invariant.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IInvariant createInvariant(IMachineRoot mch, String label,
			String predicate, boolean thm, IProgressMonitor monitor)
			throws RodinDBException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 4);
		IInvariant inv = mch.createChild(IInvariant.ELEMENT_TYPE, null,
				subMonitor.newChild(1));
		inv.setLabel(label, subMonitor.newChild(1));
		inv.setPredicateString(predicate, subMonitor.newChild(1));
		inv.setTheorem(thm, subMonitor.newChild(1));
		return inv;
	}

	/**
	 * Creates a new event in the given machine with the provided information:
	 * the label, convergence, extended flag.
	 * 
	 * @param mch
	 *            a machine root.
	 * @param label
	 *            the label of the new event.
	 * @param convergence
	 *            the convergence value of the new event.
	 * @param extended
	 *            the extended flag of the new event.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the newly created event.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IEvent createEvent(IMachineRoot mch, String label,
			Convergence convergence, boolean extended, IProgressMonitor monitor)
			throws RodinDBException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 4);
		IEvent evt = mch.createChild(IEvent.ELEMENT_TYPE, null, subMonitor
				.newChild(1));
		evt.setLabel(label, subMonitor.newChild(1));
		evt.setConvergence(convergence, subMonitor.newChild(1));
		evt.setExtended(extended, subMonitor.newChild(1));
		return evt;
	}

	/**
	 * Creates a new parameter in the given event with the provided information:
	 * the identifier string.
	 * 
	 * @param evt
	 *            an event.
	 * @param identifier
	 *            the identifier of the new parameter.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the newly created parameter.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IParameter createParameter(IEvent evt, String identifier,
			IProgressMonitor monitor) throws RodinDBException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		IParameter par = evt.createChild(IParameter.ELEMENT_TYPE, null,
				subMonitor.newChild(1));
		par.setIdentifierString(identifier, subMonitor.newChild(1));
		return par;
	}

	/**
	 * Creates a new guard in the given event with the provided information: the
	 * label, the predicate string, the isThm flag.
	 * 
	 * @param evt
	 *            an event.
	 * @param label
	 *            the label of the new guard.
	 * @param predicate
	 *            the predicate string of the new guard
	 * @param thm
	 *            <code>true</code> if the guard is a theorem, otherwise
	 *            <code>false</code>.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the newly created guard.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IGuard createGuard(IEvent evt, String label,
			String predicate, boolean thm, IProgressMonitor monitor)
			throws RodinDBException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 4);
		IGuard grd = evt.createChild(IGuard.ELEMENT_TYPE, null, subMonitor
				.newChild(1));
		grd.setLabel(label, subMonitor.newChild(1));
		grd.setPredicateString(predicate, subMonitor.newChild(1));
		grd.setTheorem(thm, subMonitor.newChild(1));
		return grd;
	}

	/**
	 * Creates a new action in the given event with the provided information: the
	 * label, the assignment string.
	 * 
	 * @param evt an event.
	 * @param label the label of the new action.
	 * @param assignment the assignment string of the new action.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the newly created action.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IAction createAction(IEvent evt, String label,
			String assignment, IProgressMonitor monitor)
			throws RodinDBException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 3);
		IAction act = evt.createChild(IAction.ELEMENT_TYPE, null, subMonitor
				.newChild(1));
		act.setLabel(label, subMonitor.newChild(1));
		act.setAssignmentString(assignment, subMonitor.newChild(1));
		return act;
	}

	public static IEvent getEvent(IMachineRoot mchRoot, String evtLabel)
			throws RodinDBException {
		if (mchRoot == null)
			return null;
		IEvent[] evts = mchRoot.getEvents();
		for (IEvent evt : evts) {
			if (evt.getLabel().equals(evtLabel))
				return evt;
		}
		return null;
	}

}