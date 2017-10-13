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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IAxiom;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IConfigurationElement;
import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IExtendsContext;
import org.eventb.core.IGuard;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IRefinesEvent;
import org.eventb.core.IRefinesMachine;
import org.eventb.core.ISeesContext;
import org.eventb.core.IVariable;
import org.eventb.core.IWitness;
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
 *         unchecked elements (<i>e.g.</i> Event-B projects, machines,
 *         contexts).
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

	/**
	 * Utility method to create an Event-B project with given name.
	 * 
	 * @param name
	 *            name of the project
	 * @return the newly created Event-B project
	 * @throws CoreException
	 *             if some errors occurred.
	 */
	public static IEventBProject createEventBProject(String name,
			IProgressMonitor monitor) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(name);
		
		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateEventBProject, 3);

		
		// 1. Create a new project
		subMonitor.subTask(Messages.progress_CreateProject);
		project.create(subMonitor.newChild(1));
		
		// 2. Open the newly created project.
		subMonitor.subTask(Messages.progress_OpenProject);
		project.open(subMonitor.newChild(1));
		
		// 3. Set the project nature.
		IProjectDescription pDescription = project.getDescription();
		pDescription.setNatureIds(new String[] { RodinCore.NATURE_ID });
		subMonitor.subTask(Messages.progress_SetRodinProjectNature);
		project.setDescription(pDescription, subMonitor.newChild(1));

		IRodinProject rodinPrj = RodinCore.valueOf(project);
		return (IEventBProject) rodinPrj.getAdapter(IEventBProject.class);
	}

	// =========================================================================
	// Machines / Contexts
	// =========================================================================

	/**
	 * Utility method to create a new context (*.buc) within an existing
	 * project. The name of the new context is chosen with the specified
	 * bare-name by adding some suffix so that there is no existing component
	 * with the same bare-name. The default configuration is associated with the
	 * new context.
	 * 
	 * @param prj
	 *            The Event-B project.
	 * @param barename
	 *            Intended bare-name for the new context.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * @return the handle to the newly created context.
	 * @throws RodinDBException
	 *             if there are problems accessing the database.
	 */
	public static IContextRoot createContext(IEventBProject prj,
			String barename, IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(prj, Messages.error_NullProject);
		Assert.isTrue(prj.getRodinProject().exists(),
				Messages.bind(Messages.error_NonExistingProject, prj));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateContext, 3);

		// 1. Get a free component name by appending some suffix.
		subMonitor.subTask(Messages.progress_GetFreeComponentName);
		String name = getFreeComponentName(prj, barename,
				subMonitor.newChild(1));
		IRodinFile context = prj.getContextFile(name);
		Assert.isNotNull(context, Messages.error_NullContext);
		Assert.isTrue(
				!context.exists(),
				Messages.bind(Messages.error_ExistingContext,
						context.getBareName()));

		// 2. Create the context.
		subMonitor.subTask(Messages.progress_CreateContextFile);
		context.create(false, subMonitor.newChild(1));
		IContextRoot root = (IContextRoot) context.getRoot();

		// 3. Set default configuration.
		subMonitor.subTask(Messages.progress_SetDefaultConfiguration);
		root.setConfiguration(IConfigurationElement.DEFAULT_CONFIGURATION,
				subMonitor.newChild(1));

		return root;
	}

	/**
	 * Utility method to create a new machine (*.bum) within an existing
	 * project. The name of the new machine is chosen with the specified
	 * bare-name by appending some suffix so that there is no existing component
	 * with the same bare-name. The default configuration is associated with the
	 * new machine.
	 * 
	 * @param prj
	 *            The Event-B project.
	 * @param barename
	 *            The intended bare-name for the new machine.
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
	public static IMachineRoot createMachine(IEventBProject prj,
			String barename, IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(prj, Messages.error_NullProject);
		Assert.isTrue(prj.getRodinProject().exists(),
				Messages.bind(Messages.error_NonExistingProject, prj));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateMachine, 3);

		// 1. Get a free component name by appending some suffix.
		subMonitor.subTask(Messages.progress_GetFreeComponentName);
		String name = getFreeComponentName(prj, barename,
				subMonitor.newChild(1));
		IRodinFile machine = prj.getMachineFile(name);
		Assert.isNotNull(machine, Messages.error_NullMachine);
		Assert.isTrue(!machine.exists(),
				Messages.bind(Messages.error_ExistingMachine, machine));

		// 2. Create the machine.
		subMonitor.subTask(Messages.progress_CreateMachineFile);
		machine.create(false, subMonitor.newChild(1));
		IMachineRoot root = (IMachineRoot) machine.getRoot();

		// 3. Set default configuration.
		subMonitor.subTask(Messages.progress_SetDefaultConfiguration);
		root.setConfiguration(IConfigurationElement.DEFAULT_CONFIGURATION,
				subMonitor.newChild(1));
		return root;
	}

	/**
	 * Get a free component name within a project. A component name is free when
	 * there are no existing machine or context with the same name.
	 * 
	 * @param prj
	 *            an Event-B project.
	 * 
	 * @param monitor
	 *            a progress monitor or <code>null</code> indicating no need for
	 *            progress reporting.
	 * 
	 * @return a free component name with a project.
	 */
	private static String getFreeComponentName(IEventBProject prj,
			String prefix, IProgressMonitor monitor) {
		if (monitor != null)
			monitor.beginTask(Messages.progress_GetFreeComponentName, 1);
		int index = 0;
		String name = prefix;
		while (!checkFreeComponentName(prj, name, monitor)) {
			name = prefix + "_" + index; //$NON-NLS-1$
			index++;
		}
		if (monitor != null) {
			monitor.worked(1);
			monitor.done();
		}
		return name;
	}

	/**
	 * Check if a component name is a free to be used within a project.
	 * 
	 * @param prj
	 *            an Event-B project
	 * 
	 * @param name
	 *            a given component name.
	 * 
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that no
	 *            progress should be reported and that the operation cannot be
	 *            cancelled.
	 * 
	 * @return <code>true</code> if the component name is free,
	 *         <code>false</code> otherwise.
	 */
	private static boolean checkFreeComponentName(IEventBProject prj,
			String name, IProgressMonitor monitor) {

		if (monitor != null)
			monitor.beginTask(Messages.progress_GetFreeComponentName, 1);
		boolean result = true;
		IRodinFile contextFile = prj.getContextFile(name);
		if (contextFile.exists())
			result = false;
		else {
			IRodinFile machineFile = prj.getMachineFile(name);
			if (machineFile.exists())
				result = false;
		}
		if (monitor != null) {
			monitor.worked(1);
			monitor.done();
		}
		return result;
	}

	/**
	 * Creates a new EXTENDS clause with the given abstract context name, in an
	 * EXISTING context root.
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
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database
	 */
	public static IExtendsContext createExtendsContextClause(IContextRoot ctx,
			String absCtxName, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(ctx, Messages.error_NullContext);
		Assert.isTrue(ctx.exists(), Messages.bind(
				Messages.error_NonExistingContext, ctx.getRodinFile()
						.getBareName()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateExtendsContextClause, 2);

		// 1. Create the extends clause.
		subMonitor.subTask(Messages.progress_CreateExtendsContextElement);
		IExtendsContext extendCtx = ctx.createChild(
				IExtendsContext.ELEMENT_TYPE, nextSibling,
				subMonitor.newChild(1));

		// 2. Set the abstract context name.
		subMonitor.subTask(Messages.progress_SetAbstractContextName);
		extendCtx.setAbstractContextName(absCtxName, subMonitor.newChild(1));

		return extendCtx;
	}

	/**
	 * Creates a new carrier set with the given identifier string, in an
	 * EXISTING context root.
	 * 
	 * @param ctx
	 *            a context.
	 * @param identifierString
	 *            the identifier string.
	 * @param nextSibling
	 *            sibling before which the sees clause should be created, or
	 *            <code>null</code> to create it at the last position
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created carrier set.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database
	 */
	public static ICarrierSet createCarrierSet(IContextRoot ctx,
			String identifierString, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(ctx, Messages.error_NullContext);
		Assert.isTrue(ctx.exists(), Messages.bind(
				Messages.error_NonExistingContext, ctx.getRodinFile()
						.getBareName()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateCarrierSet, 2);

		// 1. Create the carrier set.
		subMonitor.subTask(Messages.progress_CreateCarrierSetElement);
		ICarrierSet set = ctx.createChild(ICarrierSet.ELEMENT_TYPE,
				nextSibling, subMonitor.newChild(1));

		// 2. Set the identifier string.
		subMonitor.subTask(Messages.progress_SetCarrierSetIdentifierString);
		set.setIdentifierString(identifierString, subMonitor.newChild(1));

		return set;
	}

	/**
	 * Creates a new constant with the given identifier string, in an EXISTING
	 * context root.
	 * 
	 * @param ctx
	 *            a context.
	 * @param identifierString
	 *            the identifier string.
	 * @param nextSibling
	 *            sibling before which the sees clause should be created, or
	 *            <code>null</code> to create it at the last position
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created constant.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database
	 */
	public static IConstant createConstant(IContextRoot ctx,
			String identifierString, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(ctx, Messages.error_NullContext);
		Assert.isTrue(ctx.exists(), Messages.bind(
				Messages.error_NonExistingContext, ctx.getRodinFile()
						.getBareName()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateConstant, 2);

		// 1. Create the carrier set.
		subMonitor.subTask(Messages.progress_CreateConstantElement);
		IConstant cst = ctx.createChild(IConstant.ELEMENT_TYPE, nextSibling,
				subMonitor.newChild(1));

		// 2. Set the identifier string.
		subMonitor.subTask(Messages.progress_SetConstantIdentifierString);
		cst.setIdentifierString(identifierString, subMonitor.newChild(1));

		return cst;
	}

	/**
	 * Creates an axiom in an EXISTING context with the provided information:
	 * label, predicate string, is Theorem.
	 * 
	 * @param ctx
	 *            an EXISTING context root.
	 * @param label
	 *            the label of the axiom.
	 * @param predicate
	 *            the predicate string of the axiom.
	 * @param isTheorem
	 *            <code>true</code> if this should be a theorem, otherwise
	 *            <code>false</code>.
	 * @param nextSibling
	 *            sibling before which the sees clause should be created, or
	 *            <code>null</code> to create it at the last position
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created axiom.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IAxiom createAxiom(IContextRoot ctx, String label,
			String predicate, boolean isTheorem, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(ctx, Messages.error_NullContext);
		Assert.isTrue(ctx.exists(), Messages.bind(
				Messages.error_NonExistingContext, ctx.getRodinFile()
						.getBareName()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateAxiom, 4);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateAxiomElement);
		IAxiom axm = ctx.createChild(IAxiom.ELEMENT_TYPE, nextSibling,
				subMonitor.newChild(1));

		// 2. Set the label.
		subMonitor.subTask(Messages.progress_SetAxiomLabel);
		axm.setLabel(label, subMonitor.newChild(1));

		// 3. Set predicate string.
		subMonitor.subTask(Messages.progress_SetAxiomPredicateString);
		axm.setPredicateString(predicate, subMonitor.newChild(1));

		// 4. Set isTheorem attribute.
		subMonitor.subTask(Messages.progress_SetAxiomIsTheorem);
		axm.setTheorem(isTheorem, subMonitor.newChild(1));

		return axm;
	}

	/**
	 * Creates a REFINES clause in an EXISTING machine with the provided
	 * information: the abstract machine name.
	 * 
	 * @param mch
	 *            an EXISTING machine root.
	 * @param name
	 *            the abstract machine name of the REFINES clause.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created REFINES clause.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IRefinesMachine createRefinesMachineClause(IMachineRoot mch,
			String name, IInternalElement nextSibling, IProgressMonitor monitor)
			throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(mch, Messages.error_NullMachine);
		Assert.isTrue(mch.exists(), Messages.bind(
				Messages.error_NonExistingMachine, mch.getRodinFile()
						.getBareName()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateRefinesMachineClause, 2);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateRefinesMachineElement);
		IRefinesMachine refinesMch = mch.createChild(
				IRefinesMachine.ELEMENT_TYPE, nextSibling,
				subMonitor.newChild(1));

		// 2. Set the abstract machine name.
		subMonitor
				.subTask(Messages.progress_SetRefinesMachineAbstractMachineName);
		refinesMch.setAbstractMachineName(name, subMonitor.newChild(1));

		return refinesMch;
	}

	/**
	 * Creates a SEES clause in an EXISTING machine with the provided
	 * information: the seen context name.
	 * 
	 * @param mch
	 *            an EXISTING machine root.
	 * @param name
	 *            the seen context name of the SEES clause.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created SEES clause.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static ISeesContext createSeesContextClause(IMachineRoot mch,
			String name, IInternalElement nextSibling, IProgressMonitor monitor)
			throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(mch, Messages.error_NullMachine);
		Assert.isTrue(mch.exists(), Messages.bind(
				Messages.error_NonExistingMachine, mch.getRodinFile()
						.getBareName()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateSeesContextClause, 2);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateSeesContextElement);
		ISeesContext seesCtx = mch.createChild(ISeesContext.ELEMENT_TYPE,
				nextSibling, subMonitor.newChild(1));

		// 2. Set seen context name.
		subMonitor.subTask(Messages.progress_SetSeenContextName);
		seesCtx.setSeenContextName(name, subMonitor.newChild(1));

		return seesCtx;
	}

	/**
	 * Creates a new variable in an EXISTING machine with the provided
	 * information: the identifier string.
	 * 
	 * @param mch
	 *            an EXISTING machine root.
	 * @param identifier
	 *            the identifier of the new variable.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created variable.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IVariable createVariable(IMachineRoot mch, String identifier,
			IInternalElement nextSibling, IProgressMonitor monitor)
			throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(mch, Messages.error_NullMachine);
		Assert.isTrue(mch.exists(), Messages.bind(
				Messages.error_NonExistingMachine, mch.getRodinFile()
						.getBareName()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateVariable, 2);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateVariableElement);
		IVariable var = mch.createChild(IVariable.ELEMENT_TYPE, nextSibling,
				subMonitor.newChild(1));

		// 2. Set the identifier string.
		subMonitor.subTask(Messages.progress_SetVariableIdentifierString);
		var.setIdentifierString(identifier, subMonitor.newChild(1));

		return var;
	}

	/**
	 * Creates a new invariant in an EXISTING machine with the provided
	 * information: the label, the predicate string and boolean flag isThm.
	 * 
	 * @param mch
	 *            an EXISTING machine root.
	 * @param label
	 *            the label of the new invariant.
	 * @param predicate
	 *            the predicate string of the new invariant.
	 * @param thm
	 *            <code>true</code> if the new invariant should be a theorem,
	 *            otherwise <code>false</code>.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created invariant.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IInvariant createInvariant(IMachineRoot mch, String label,
			String predicate, boolean thm, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(mch, Messages.error_NullMachine);
		Assert.isTrue(mch.exists(), Messages.bind(
				Messages.error_NonExistingMachine, mch.getRodinFile()
						.getBareName()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateInvariant, 4);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateInvariantElement);
		IInvariant inv = mch.createChild(IInvariant.ELEMENT_TYPE, nextSibling,
				subMonitor.newChild(1));

		// 2. Set invariant label.
		subMonitor.subTask(Messages.progress_SetInvariantLabel);
		inv.setLabel(label, subMonitor.newChild(1));

		// 3. Set invariant predicate string.
		subMonitor.subTask(Messages.progress_SetInvariantPredicateString);
		inv.setPredicateString(predicate, subMonitor.newChild(1));

		// 4. Set invariant isTheorem attribute
		subMonitor.subTask(Messages.progress_SetInvariantIsTheorem);
		inv.setTheorem(thm, subMonitor.newChild(1));

		return inv;
	}

	/**
	 * Creates a new event in an EXISTING machine with the provided information:
	 * the label, convergence, extended flag.
	 * 
	 * @param mch
	 *            an EXISTING machine root.
	 * @param label
	 *            the label of the new event.
	 * @param convergence
	 *            the convergence value of the new event.
	 * @param extended
	 *            the extended flag of the new event.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created event.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IEvent createEvent(IMachineRoot mch, String label,
			Convergence convergence, boolean extended,
			IInternalElement nextSibling, IProgressMonitor monitor)
			throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(mch, Messages.error_NullMachine);
		Assert.isTrue(mch.exists(), Messages.bind(
				Messages.error_NonExistingMachine, mch.getRodinFile()
						.getBareName()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateEvent, 4);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateEventElement);
		IEvent evt = mch.createChild(IEvent.ELEMENT_TYPE, nextSibling,
				subMonitor.newChild(1));

		// 2. Set event label.
		subMonitor.subTask(Messages.progress_SetEventLabel);
		evt.setLabel(label, subMonitor.newChild(1));

		// 3. Set event convergence attribute.
		subMonitor.subTask(Messages.progress_SetEventConvergence);
		evt.setConvergence(convergence, subMonitor.newChild(1));

		// 4. Set event extended attribute.
		subMonitor.subTask(Messages.progress_SetEventExtended);
		evt.setExtended(extended, subMonitor.newChild(1));

		return evt;
	}

	/**
	 * Creates a new refine event clause in an EXISTING event with the abstract
	 * event label.
	 * 
	 * @param evt
	 *            an event.
	 * @param absEvtLabel
	 *            the abstract event label.
	 * @return the newly created refines event clause.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	public static IRefinesEvent createRefinesEventClause(IEvent evt,
			String absEvtLabel, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(evt, Messages.error_NullEvent);
		Assert.isTrue(evt.exists(),
				Messages.bind(Messages.error_NonExistingEvent, evt.getLabel()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateRefinesEventClause, 2);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateRefinesEventElement);
		IRefinesEvent refEvt = evt.createChild(IRefinesEvent.ELEMENT_TYPE,
				nextSibling, subMonitor.newChild(1));

		// 2. Set abstract event label.
		subMonitor.subTask(Messages.progress_SetAbstractEventLabel);
		refEvt.setAbstractEventLabel(absEvtLabel, subMonitor.newChild(1));
		return refEvt;
	}

	/**
	 * Creates a new parameter in an EXISTING event with the provided
	 * information: the identifier string.
	 * 
	 * @param evt
	 *            an EXISTING event.
	 * @param identifier
	 *            the identifier of the new parameter.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created parameter.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IParameter createParameter(IEvent evt, String identifier,
			IInternalElement nextSibling, IProgressMonitor monitor)
			throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(evt, Messages.error_NullEvent);
		Assert.isTrue(evt.exists(),
				Messages.bind(Messages.error_NonExistingEvent, evt.getLabel()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateParameter, 2);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateParameterElement);
		IParameter par = evt.createChild(IParameter.ELEMENT_TYPE, nextSibling,
				subMonitor.newChild(1));

		// 2. Set parameter identifier string.
		subMonitor.subTask(Messages.progress_SetParameterIdentifierString);
		par.setIdentifierString(identifier, subMonitor.newChild(1));

		return par;
	}

	/**
	 * Creates a new guard in an EXISTING event with the provided information:
	 * the label, the predicate string, the isThm flag.
	 * 
	 * @param evt
	 *            an EXISTING event.
	 * @param label
	 *            the label of the new guard.
	 * @param predicate
	 *            the predicate string of the new guard
	 * @param thm
	 *            <code>true</code> if the guard is a theorem, otherwise
	 *            <code>false</code>.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created guard.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IGuard createGuard(IEvent evt, String label,
			String predicate, boolean thm, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(evt, Messages.error_NullEvent);
		Assert.isTrue(evt.exists(),
				Messages.bind(Messages.error_NonExistingEvent, evt.getLabel()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateGuard, 4);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateGuardElement);
		IGuard grd = evt.createChild(IGuard.ELEMENT_TYPE, null,
				subMonitor.newChild(1));

		// 2. Set guard label.
		subMonitor.subTask(Messages.progress_SetGuardLabel);
		grd.setLabel(label, subMonitor.newChild(1));

		// 3. Set guard predicate string.
		subMonitor.subTask(Messages.progress_SetGuardPredicateString);
		grd.setPredicateString(predicate, subMonitor.newChild(1));

		// 4. Set guard isTheorem attribute.
		subMonitor.subTask(Messages.progress_SetGuardIsTheorem);
		grd.setTheorem(thm, subMonitor.newChild(1));

		return grd;
	}

	/**
	 * Creates a new witness in an EXISTING event with the provided information:
	 * the label, the predicate string.
	 * 
	 * @param evt
	 *            an event.
	 * @param label
	 *            the label of the witness.
	 * @param predicateString
	 *            the predicate string of the witness.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created witness.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IWitness createWitness(IEvent evt, String label,
			String predicateString, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(evt, Messages.error_NullEvent);
		Assert.isTrue(evt.exists(),
				Messages.bind(Messages.error_NonExistingEvent, evt.getLabel()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateWitness, 3);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateWitnessElement);
		IWitness wit = evt.createChild(IWitness.ELEMENT_TYPE, nextSibling,
				subMonitor.newChild(1));

		// 2. Set witness label.
		subMonitor.subTask(Messages.progress_SetWitnessLabel);
		wit.setLabel(label, subMonitor.newChild(1));

		// 3. Set guard predicate string.
		subMonitor.subTask(Messages.progress_SetWitnessPredicateString);
		wit.setPredicateString(predicateString, subMonitor.newChild(1));

		return wit;
	}

	/**
	 * Creates a new action in an EXISTING event with the provided information:
	 * the label, the assignment string.
	 * 
	 * @param evt
	 *            an EXISTING event.
	 * @param label
	 *            the label of the new action.
	 * @param assignment
	 *            the assignment string of the new action.
	 * @param nextSibling
	 *            sibling before which the child should be created (must have
	 *            this element as parent), or <code>null</code> to create the
	 *            child in the last position.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. Accepts <code>null</code>, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @return the newly created action.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IAction createAction(IEvent evt, String label,
			String assignment, IInternalElement nextSibling,
			IProgressMonitor monitor) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(evt, Messages.error_NullEvent);
		Assert.isTrue(evt.exists(),
				Messages.bind(Messages.error_NonExistingEvent, evt.getLabel()));

		// Split the progress monitor.
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				Messages.progress_CreateAction, 3);

		// 1. Create the element.
		subMonitor.subTask(Messages.progress_CreateActionElement);
		IAction act = evt.createChild(IAction.ELEMENT_TYPE, nextSibling,
				subMonitor.newChild(1));

		// 2. Set action label.
		subMonitor.subTask(Messages.progress_SetActionLabel);
		act.setLabel(label, subMonitor.newChild(1));

		// 3. Set action assignment string.
		subMonitor.subTask(Messages.progress_SetActionAssignmentString);
		act.setAssignmentString(assignment, subMonitor.newChild(1));

		return act;
	}

	/**
	 * Gets an event with a given event label within an EXISTING machine.
	 * 
	 * @param mch
	 *            an EXISTING machine root.
	 * @param evtLabel
	 *            the event label.
	 * @return The first event with a given input label or <code>null</code> if
	 *         there are no events with the given input label. The order of the
	 *         events are the order returned by {@link IMachineRoot#getEvents()}
	 * @see IMachineRoot#getEvents()
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static IEvent getEvent(IMachineRoot mch, String evtLabel)
			throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(mch, Messages.error_NullMachine);
		Assert.isTrue(mch.exists(), Messages.bind(
				Messages.error_NonExistingMachine, mch.getRodinFile()
						.getBareName()));

		// Get the list of events and check their labels.
		IEvent[] evts = mch.getEvents();
		for (IEvent evt : evts) {
			// Return the event if its label is the same as the input label.
			if (evt.getLabel().equals(evtLabel))
				return evt;
		}

		// Return <code>null</code> in the case where no matching events are
		// found.
		return null;
	}

}