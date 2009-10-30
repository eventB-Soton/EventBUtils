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
 *******************************************************************************/
package ch.ethz.eventb.internal.decomposition.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eventb.core.IAction;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IConfigurationElement;
import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IEventBRoot;
import org.eventb.core.IExtendsContext;
import org.eventb.core.IGuard;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.IInvariant;
import org.eventb.core.ILabeledElement;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IRefinesEvent;
import org.eventb.core.IRefinesMachine;
import org.eventb.core.ISCContextRoot;
import org.eventb.core.ISCIdentifierElement;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ISeesContext;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.SourceLocation;
import org.eventb.core.ast.Type;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.DecompositionPlugin;
import ch.ethz.eventb.decomposition.IDecomposedElement;
import ch.ethz.eventb.internal.decomposition.utils.symbols.ContextSymbolGatherer;
import ch.ethz.eventb.internal.decomposition.utils.symbols.SymbolTable;

/**
 * @author htson
 *         <p>
 *         Utility class containing some useful methods to handle Event-B
 *         elements (<i>e.g.</i> Event-B projects, machines, contexts).
 *         </p>
 */
public final class EventBUtils {

	private static final FormulaFactory FORMULA_FACTORY = FormulaFactory
			.getDefault();

	/**
	 * Configuration used by the static checker.
	 */
	public static final String DECOMPOSITION_CONFIG_SC = DecompositionPlugin.PLUGIN_ID
			+ ".mchBase"; //$NON-NLS-1$

	/**
	 * Configuration used by the proof obligation generator.
	 */
	public static final String DECOMPOSITION_CONFIG_POG = DecompositionPlugin.PLUGIN_ID
			+ ".pogConfig"; //$NON-NLS-1$

	private EventBUtils() {
		// Utility classes shall not have a public or default constructor.
	}

	// =========================================================================
	// Projects
	// =========================================================================
	/**
	 * Utility method to create a new Event-B project with a given name. If a
	 * project with the given name exists, then it is returned. Otherwise, a new
	 * Event-B project is created and returned.
	 * 
	 * @param projectName
	 *            The name of the project.
	 * @param monitor
	 *            The progress monitor.
	 * @return A newly created Event-B project or <code>null</code>.
	 * @throws RodinDBException
	 *             if some errors occurred in the RodinCore runnable to create
	 *             the project
	 *             {@link RodinCore#run(IWorkspaceRunnable, IProgressMonitor)}.
	 */
	public static IEventBProject createProject(final String projectName,
			final IProgressMonitor monitor) throws RodinDBException {
		final IRodinDB rodinDB = RodinCore.getRodinDB();
		final IRodinProject rodinProject = rodinDB.getRodinProject(projectName);
		if (!rodinProject.exists()) {
			RodinCore.run(new IWorkspaceRunnable() {

				public void run(final IProgressMonitor pMonitor)
						throws CoreException {
					IProject project = rodinProject.getProject();
					Assert.isTrue(!project.exists(),
							Messages.decomposition_error_existingproject);
					project.create(null);
					project.open(null);
					IProjectDescription description = project.getDescription();
					description
							.setNatureIds(new String[] { RodinCore.NATURE_ID });
					project.setDescription(description, null);
				}

			}, monitor);
		}

		return (IEventBProject) rodinProject.getAdapter(IEventBProject.class);
	}

	// =========================================================================
	// Machines / Contexts
	// =========================================================================
	/**
	 * Utility method to create a new sub-machine (*.bum) with the given name
	 * within an existing project. There must be no existing construct with the
	 * same bare-name. The machine is tagged as generated.
	 * 
	 * @param project
	 *            The Event-B project.
	 * @param fileName
	 *            the full name with of the new machine.
	 * @param monitor
	 *            the progress monitor used to create this machine.
	 * @return the handle to newly created machine.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static IMachineRoot createMachine(final IEventBProject project,
			final String fileName, final IProgressMonitor monitor)
			throws RodinDBException {
		monitor.beginTask(Messages.decomposition_machine, 1);
		IRodinFile machine = project.getMachineFile(fileName);
		Assert.isTrue(!machine.exists(),
				Messages.decomposition_error_existingmachine);
		machine.create(false, new NullProgressMonitor());
		IMachineRoot root = (IMachineRoot) machine.getRoot();

		setDecomposed(root, monitor);

		monitor.worked(1);
		monitor.done();
		return (IMachineRoot) root;
	}

	/**
	 * Utility method to create a new context (*.buc) with the given name within
	 * an existing project. There must be no existing construct with the same
	 * bare-name. The context is tagged as generated.
	 * 
	 * @param project
	 *            The Event-B project
	 * @param fileName
	 *            the full name with of the new context
	 * @param monitor
	 *            the progress monitor used to create this context
	 * @return the handle to newly created context
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static IContextRoot createContext(final IEventBProject project,
			final String fileName, final IProgressMonitor monitor)
			throws RodinDBException {
		monitor.beginTask(Messages.decomposition_contexts, 1);
		IRodinFile context = project.getContextFile(fileName);
		Assert.isTrue(!context.exists(),
				Messages.decomposition_error_existingmachine);
		context.create(false, new NullProgressMonitor());
		IContextRoot root = (IContextRoot) context.getRoot();

		setDecomposed(root, monitor);

		monitor.worked(1);
		monitor.done();
		return root;
	}

	private static void setDecomposed(IEventBRoot root,
			final IProgressMonitor monitor) throws RodinDBException {
		// Tag the root as decomposed and generated
		IDecomposedElement elt = (IDecomposedElement) root
				.getAdapter(IDecomposedElement.class);
		elt.setDecomposed(monitor);

		// Set the configuration
		((IConfigurationElement) root).setConfiguration(
				DECOMPOSITION_CONFIG_SC, monitor);
	}

	/**
	 * Utility method to copy all contexts from a project to another one.
	 * WARNING: This method will overwrite any existing contexts in the
	 * destination with the same name.
	 * 
	 * @param from
	 *            the source project
	 * @param to
	 *            the destination project
	 * @param monitor
	 *            the progress monitor
	 * @throws RodinDBException
	 *             when some errors occur when
	 *             <ul>
	 *             <li>getting the contexts in the from project
	 *             {@link IRodinProject#getRootElementsOfType(org.rodinp.core.IInternalElementType)}
	 *             .</li>
	 *             <li>copying any context to the destination project
	 *             {@link IRodinFile#copy(IRodinElement, IRodinElement, String, boolean, IProgressMonitor)}
	 *             .</li>
	 *             </ul>
	 */
	public static void copyContexts(final IEventBProject from,
			final IEventBProject to, final IProgressMonitor monitor)
			throws RodinDBException {
		IRodinProject fromPrj = from.getRodinProject();
		IContextRoot[] contexts = fromPrj
				.getRootElementsOfType(IContextRoot.ELEMENT_TYPE);
		for (IContextRoot context : contexts) {
			IRodinFile ctxFile = context.getRodinFile();
			ctxFile.copy(to.getRodinProject(), null, null, true, monitor);
		}

		// Tag the contexts as decomposed and generated and set the
		// configuration
		for (IContextRoot context : contexts) {
			IContextRoot copiedContext = to.getContextRoot(context
					.getElementName());
			IDecomposedElement elt = (IDecomposedElement) copiedContext
					.getAdapter(IDecomposedElement.class);
			elt.setDecomposed(monitor);

			((IConfigurationElement) copiedContext).setConfiguration(
					DECOMPOSITION_CONFIG_SC, monitor);
		}
	}

	/**
	 * Utility method to copy the SEES clauses from a source machine to a
	 * destination machine.
	 * 
	 * @param src
	 *            the source machine.
	 * @param dest
	 *            the destination machine.
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the SEES clause of the source machine
	 *             {@link IMachineRoot#getSeesClauses()}.</li>
	 *             <li>creating a new SEES clause in the destination machine
	 *             {@link IMachineRoot#createChild(org.rodinp.core.IInternalElementType, IInternalElement, IProgressMonitor)}
	 *             .</li>
	 *             <li>setting the seen context name of the created SEES clause
	 *             {@link ISeesContext#setSeenContextName(String, IProgressMonitor)}
	 *             .</li>
	 *             </ul>
	 */
	public static void copySeesClauses(final IMachineRoot src,
			final IMachineRoot dest, final IProgressMonitor monitor)
			throws RodinDBException {
		ISeesContext[] seesClauses = src.getSeesClauses();
		monitor.beginTask(Messages.decomposition_seesclauses,
				seesClauses.length);
		for (ISeesContext seesClause : seesClauses) {
			ISeesContext newSeesClause = dest.createChild(
					ISeesContext.ELEMENT_TYPE, null, new NullProgressMonitor());
			newSeesClause.setSeenContextName(seesClause.getSeenContextName(),
					new NullProgressMonitor());
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * Utility method to get the set of seen carrier sets and constants of a
	 * machine. This is the set of carrier sets and constants from all the seen
	 * contexts. No assumption should be made concerning the order of returned
	 * elements.
	 * 
	 * @param mch
	 *            a machine.
	 * @return the set of seen carrier sets and constants (as {@link String}.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the SEES clauses of the input machine
	 *             {@link IMachineRoot#getSeesClauses()}.</li>
	 *             <li>getting the seen context name of any SEES clause of the
	 *             input machine {@link ISeesContext#getSeenContextName()}.</li>
	 *             <li>getting the carrier sets and constants of any seen
	 *             contexts {@link #getCarrierSetsAndConstants(IContextRoot)}.</li>
	 *             </ul>
	 */
	public static List<String> getSeenCarrierSetsAndConstants(
			final IMachineRoot mch) throws RodinDBException {
		final Set<IContextRoot> seenContexts = getSeenContexts(mch);
		final SymbolTable symbolTable = getConstantAndSetSymbols(seenContexts);
		return new ArrayList<String>(symbolTable.getNames());
	}

	/**
	 * Utility method to get the set of carrier sets and constants of a context
	 * (include ones from the abstract contexts). This is done by first
	 * flattening the context and getting the carrier sets and constants. The
	 * carrier sets are returned before the constants.
	 * 
	 * @param ctx
	 *            a context.
	 * @return the set of carrier sets and constants contain within the input
	 *         context.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the carrier sets/constants of the input context
	 *             {@link IContextRoot#getCarrierSets()}.</li>
	 *             <li>getting the identifier string of any carrier set
	 *             {@link ICarrierSet#getIdentifierString()} or any constant
	 *             {@link IConstant#getIdentifierString()} of the input context.
	 *             </li>
	 *             </ul>
	 */
	public static List<String> getCarrierSetsAndConstants(IContextRoot ctx)
			throws RodinDBException {
		// TODO not used ? => can be removed
		final Set<IContextRoot> contexts = new LinkedHashSet<IContextRoot>();
		addExtendedContexts(ctx, contexts);
		contexts.add(ctx);
		final SymbolTable symbolTable = getConstantAndSetSymbols(contexts);
		return new ArrayList<String>(symbolTable.getNames());
	}

	/**
	 * Returns a set of all contexts seen (directly or not) by the given
	 * machine.
	 * 
	 * @param mch
	 *            a machine root
	 * @return a (possibly empty) set of context roots
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	public static Set<IContextRoot> getSeenContexts(IMachineRoot mch)
			throws RodinDBException {
		final Set<IContextRoot> seenContexts = new LinkedHashSet<IContextRoot>();
		final ISeesContext[] seesClauses = mch.getSeesClauses();
		for (ISeesContext seesContext : seesClauses) {
			final IContextRoot ctx = seesContext.getSeenContextRoot();
			addExtendedContexts(ctx, seenContexts);
			seenContexts.add(ctx);
		}
		return seenContexts;
	}

	private static void addExtendedContexts(IContextRoot ctx,
			Set<IContextRoot> contexts) throws RodinDBException {
		final IExtendsContext[] extendsClauses = ctx.getExtendsClauses();
		for (IExtendsContext extendsContext : extendsClauses) {
			final IContextRoot absCtx = extendsContext.getAbstractContextRoot();
			addExtendedContexts(absCtx, contexts);
			contexts.add(absCtx);
		}
	}

	/**
	 * Returns a symbol table containing all sets and constants declared in the
	 * given contexts.
	 * 
	 * @param contexts
	 *            a set of contexts
	 * @return a symbol table
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	public static SymbolTable getConstantAndSetSymbols(
			Set<IContextRoot> contexts) throws RodinDBException {
		final SymbolTable constantSetSymbols = new SymbolTable();
		for (IContextRoot ctx : contexts) {
			final ContextSymbolGatherer ctxSymbGth = new ContextSymbolGatherer(
					ctx);
			ctxSymbGth.addDeclaredSymbols(constantSetSymbols);
		}
		return constantSetSymbols;
	}

	/**
	 * Makes a machine consistent and all the contexts in the same project as
	 * this machine consistent.
	 * 
	 * @param machine
	 *            a machine.
	 * @param monitor
	 *            a progress monitor.
	 */
	public static void cleanUp(final IMachineRoot machine,
			final IProgressMonitor monitor) {
		// Make the machine consistent.
		try {
			IRodinFile rodinFile = machine.getRodinFile();
			if (rodinFile.hasUnsavedChanges()) {
				rodinFile.makeConsistent(monitor);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
		}

		// Make all the contexts consistent.
		IRodinProject prj = machine.getRodinProject();
		IContextRoot[] ctxs;
		try {
			ctxs = prj.getRootElementsOfType(IContextRoot.ELEMENT_TYPE);
			for (IContextRoot ctx : ctxs) {
				IRodinFile rodinFile = ctx.getRodinFile();
				if (rodinFile.hasUnsavedChanges()) {
					rodinFile.makeConsistent(monitor);
				}
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
		}
	}

	// =========================================================================
	// Variables / Constants
	// =========================================================================
	/**
	 * Gets the predicate string corresponding to a variable identifier given an
	 * input machine. This is done using the statically checked version of the
	 * machine. Returns <code>null</code> if and only if the source machine or
	 * its statically checked version do not exists, or if there is no
	 * statically checked variable with the specified identifier.
	 * 
	 * @param src
	 *            a source machine.
	 * @param var
	 *            a variable identifier.
	 * @return the typing predicate string for the input variable identifier.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static String getTypingTheorem(final IMachineRoot src,
			final String var) throws RodinDBException {
		return new VariableTypingTheoremMaker(src).getTypingTheorem(var);
	}

	public static String getTypingTheorem(IContextRoot src, String cst)
			throws RodinDBException {
		return new ConstantTypingTheoremMaker(src).getTypingTheorem(cst);
	}

	public static Expression getTypeExpression(IContextRoot src, String cst)
			throws RodinDBException {
		return new ConstantTypingTheoremMaker(src).getTypingExpression(cst);
	}

	public static Expression getTypeExpression(IMachineRoot src, String var)
			throws RodinDBException {
		return new VariableTypingTheoremMaker(src).getTypingExpression(var);
	}

	public static String makeTypingTheorem(String ident,
			Expression typeExpression) {
		return ident + " ∈ " //$NON-NLS-1$
				+ typeExpression;
	}

	private static abstract class TypingTheoremMaker<T extends IEventBRoot> {

		protected final T root;

		public TypingTheoremMaker(T root) {
			this.root = root;
		}

		public Expression getTypingExpression(String ident)
				throws RodinDBException {
			if (!root.exists()) {
				return null;
			}
			final ISCIdentifierElement[] scIdents = getSCIdents();
			if (scIdents == null) {
				return null;
			}
			final ISCIdentifierElement scIdent = findSCIdent(ident, scIdents);
			if (scIdent == null) {
				return null;
			}
			final Type type = scIdent.getType(FORMULA_FACTORY);
			final Expression typeExpression = type
					.toExpression(FORMULA_FACTORY);
			return typeExpression;

		}

		public String getTypingTheorem(String ident) throws RodinDBException {
			final Expression typeExpression = getTypingExpression(ident);

			if (typeExpression == null) {
				return null;
			}

			return makeTypingTheorem(ident, typeExpression);
		}

		protected abstract ISCIdentifierElement[] getSCIdents()
				throws RodinDBException;

		private static ISCIdentifierElement findSCIdent(String ident,
				ISCIdentifierElement[] idents) throws RodinDBException {
			for (ISCIdentifierElement cstSC : idents) {
				if (cstSC.getIdentifierString().equals(ident)) {
					return cstSC;
				}
			}
			return null;
		}

	}

	private static class ConstantTypingTheoremMaker extends
			TypingTheoremMaker<IContextRoot> {

		public ConstantTypingTheoremMaker(IContextRoot root) {
			super(root);
		}

		@Override
		protected ISCIdentifierElement[] getSCIdents() throws RodinDBException {
			ISCContextRoot ctxSC = root.getSCContextRoot();
			if (!ctxSC.exists()) {
				return null;
			}
			return ctxSC.getSCConstants();
		}
	}

	private static class VariableTypingTheoremMaker extends
			TypingTheoremMaker<IMachineRoot> {

		public VariableTypingTheoremMaker(IMachineRoot root) {
			super(root);
		}

		@Override
		protected ISCIdentifierElement[] getSCIdents() throws RodinDBException {
			ISCMachineRoot mchSC = root.getSCMachineRoot();
			if (!mchSC.exists()) {
				return null;
			}
			return mchSC.getSCVariables();
		}
	}

	// =========================================================================
	// Invariants
	// =========================================================================
	/**
	 * Utility method to get the set of free identifiers of an invariant. The
	 * order of the free identifiers is determined syntactically.
	 * 
	 * @param inv
	 *            an invariant.
	 * @return set of free identifiers of the input invariant.
	 * @throws RodinDBException
	 *             if some errors occurred when getting the predicate string of
	 *             the input invariant {@link IInvariant#getPredicateString()}.
	 */
	public static List<String> getFreeIdentifiers(final IInvariant inv)
			throws RodinDBException {
		return getPredicateFreeIdentifiers(inv.getPredicateString());
	}

	/**
	 * Copies the relevant invariants for a set of variables, from a source
	 * machine to a destination machine (recursively).
	 * 
	 * @param mch
	 *            the destination machine
	 * @param src
	 *            the source machine
	 * @param vars
	 *            the set of variables (in {@link String})
	 * @param monitor
	 *            the progress monitor
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static void copyInvariants(final IMachineRoot mch,
			final IMachineRoot src, final Set<String> vars,
			final IProgressMonitor monitor) throws RodinDBException {
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
				newInv.setLabel(src.getComponentName() + "_" + inv.getLabel(), //$NON-NLS-1$
						new NullProgressMonitor());
				newInv.setPredicateString(inv.getPredicateString(),
						new NullProgressMonitor());
				newInv.setTheorem(inv.isTheorem(), new NullProgressMonitor());
			}
		}

	}

	/**
	 * Checks if an invariant is relevant for a set of variables or not,
	 * <i>i.e.</i> if the referenced variables are contained in the input set of
	 * variables.
	 * 
	 * @param inv
	 *            an invariant.
	 * @param vars
	 *            a set of variables (in {@link String}).
	 * @return return <code>true</code> if and only if the invariant is
	 *         relevant.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static boolean isRelevant(final IInvariant inv,
			final Set<String> vars) throws RodinDBException {
		Collection<String> idents = getFreeIdentifiers(inv);

		// Remove the seen carrier sets and constants.
		IMachineRoot mch = (IMachineRoot) inv.getRoot();
		idents.removeAll(getSeenCarrierSetsAndConstants(mch));
		return vars.containsAll(idents);
	}

	// =========================================================================
	// Events
	// =========================================================================
	/**
	 * Utility method to get the set of assigned identifiers of an input event.
	 * 
	 * @param evt
	 *            an event.
	 * @return the set of assigned identifiers.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static List<String> getAssignedIdentifiers(IEvent evt)
			throws RodinDBException {
		// First flatten the event.
		evt = flatten(evt);

		// Initialize the list of assigned identifiers.
		List<String> idents = new ArrayList<String>();

		// Copy the assigned identifiers from all actions.
		IAction[] acts = evt.getActions();
		for (IAction act : acts) {
			List<String> actIdents = getAssignedIdentifiers(act);
			for (String actIdent : actIdents) {
				if (!idents.contains(actIdent)) {
					idents.add(actIdent);
				}
			}
		}

		return idents;
	}

	/**
	 * Utility method to get the set of free identifiers of an input event. The
	 * order of the free identifiers is determined syntactically.
	 * 
	 * @param evt
	 *            an event.
	 * @return the set of free identifiers.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static List<String> getFreeIdentifiers(IEvent evt)
			throws RodinDBException {
		// First flatten the event.
		evt = flatten(evt);

		// Initialize the list of free identifiers.
		List<String> idents = new ArrayList<String>();

		// Copy the free identifiers from all guards.
		IGuard[] grds = evt.getGuards();
		for (IGuard grd : grds) {
			List<String> grdIdents = getFreeIdentifiers(grd);
			for (String grdIdent : grdIdents) {
				if (!idents.contains(grdIdent)) {
					idents.add(grdIdent);
				}
			}
		}

		// Copy the free identifiers from all actions.
		IAction[] acts = evt.getActions();
		for (IAction act : acts) {
			List<String> actIdents = getFreeIdentifiers(act);
			for (String actIdent : actIdents) {
				if (!idents.contains(actIdent)) {
					idents.add(actIdent);
				}
			}
		}

		// Remove the parameters.
		IParameter[] params = evt.getParameters();
		for (IParameter param : params) {
			idents.remove(param.getIdentifierString());
		}

		return idents;
	}

	/**
	 * Utility method to flatten an Event-B event:
	 * <ul>
	 * <li>If the input event is not extended then return the event.</li>
	 * <li>If the input event is extended then it is merged
	 * {@link #merge(IEvent, IEvent)} with the abstract event.</li>
	 * </ul>
	 * 
	 * @param evt
	 *            an event.
	 * @return the flatten event.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static IEvent flatten(final IEvent evt) throws RodinDBException {
		if (evt.isExtended()) {
			IEvent absEvt = getAbstract(evt);
			return merge(evt, flatten(absEvt));
		}
		return evt;
	}

	/**
	 * Utility method to get the abstract event of an input event. Returns
	 * <code>null</code> if there is no abstract event (<i>e.g.</i> there is no
	 * REFINES event clause or the abstract machine does not exist).
	 * 
	 * @param event
	 *            an event.
	 * @return the abstract event of the input event or <code>null</code> if
	 *         there is none.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static IEvent getAbstract(final IEvent event)
			throws RodinDBException {
		String absEvtLabel;
		if (event.isInitialisation()) {
			absEvtLabel = IEvent.INITIALISATION;
		} else {
			IRefinesEvent[] evtRefinesClauses = event.getRefinesClauses();
			if (evtRefinesClauses.length != 1) {
				return null;
			}
			absEvtLabel = evtRefinesClauses[0].getAbstractEventLabel();
		}

		IMachineRoot mch = (IMachineRoot) event.getRoot();

		IRefinesMachine[] refinesClauses = mch.getRefinesClauses();
		if (refinesClauses.length != 1) {
			return null;
		}

		IRodinFile abstractMachine = refinesClauses[0].getAbstractMachine();
		if (!abstractMachine.exists()) {
			return null;
		}
		IMachineRoot absMch = (IMachineRoot) abstractMachine.getRoot();
		IEvent[] absEvts = absMch.getEvents();
		for (IEvent absEvt : absEvts) {
			if (absEvt.getLabel().equals(absEvtLabel)) {
				return absEvt;
			}
		}
		return null;
	}

	/**
	 * Utility method to merge two events by copying the content of the source
	 * event to the destination event.
	 * 
	 * @param dest
	 *            the destination event.
	 * @param src
	 *            the source event.
	 * @return the merged event.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static IEvent merge(final IEvent dest, final IEvent src)
			throws RodinDBException {

		// Get the current first parameter.
		IParameter[] currParams = dest.getParameters();
		IParameter fstParam = null;
		if (currParams.length != 0) {
			fstParam = currParams[0];
		}

		// Copy the parameters from abstract event to the beginning of the list.
		IParameter[] params = src.getParameters();
		for (IParameter param : params) {
			IParameter newParam = dest.createChild(IParameter.ELEMENT_TYPE,
					fstParam, new NullProgressMonitor());
			newParam.setIdentifierString(param.getIdentifierString(),
					new NullProgressMonitor());
		}

		// Get the current first guard.
		IGuard[] currGuards = dest.getGuards();
		IGuard fstGrd = null;
		if (currGuards.length != 0) {
			fstGrd = currGuards[0];
		}

		// Copy the guards from the abstract event to the beginning of the list.
		IGuard[] grds = src.getGuards();
		for (IGuard grd : grds) {
			IGuard newGrd = dest.createChild(IGuard.ELEMENT_TYPE, fstGrd,
					new NullProgressMonitor());
			newGrd.setLabel(grd.getLabel(), new NullProgressMonitor());
			newGrd.setPredicateString(grd.getPredicateString(),
					new NullProgressMonitor());
		}

		// Get the current first action.
		IAction[] currActs = dest.getActions();
		IAction fstAct = null;
		if (currActs.length != 0) {
			fstAct = currActs[0];
		}

		// Copy the actions from the abstract event to the beginning of the
		// list.
		IAction[] acts = src.getActions();
		for (IAction act : acts) {
			IAction newAct = dest.createChild(IAction.ELEMENT_TYPE, fstAct,
					new NullProgressMonitor());
			newAct.setLabel(act.getLabel(), new NullProgressMonitor());
			newAct.setAssignmentString(act.getAssignmentString(),
					new NullProgressMonitor());
		}

		// Set the extended attribute to false.
		dest.setExtended(false, new NullProgressMonitor());
		return dest;
	}

	/**
	 * Utility method to get the initialization event of a machine.
	 * 
	 * @param mch
	 *            a machine
	 * @return the initialization event for the input machine if any, or
	 *         <code>null</code>.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static IEvent getInitialisation(final IMachineRoot mch)
			throws RodinDBException {
		return getEventWithLabel(mch, IEvent.INITIALISATION);
	}

	/**
	 * Utility method to get the event of a machine with a given label.
	 * 
	 * @param mch
	 *            a machine
	 * @param label
	 *            an event label
	 * @return the event with the given label of the input machine or
	 *         <code>null</code> if there is no event with the given label.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static IEvent getEventWithLabel(final IMachineRoot mch,
			final String label) throws RodinDBException {
		IEvent[] evts = mch.getEvents();
		for (IEvent evt : evts) {
			if (evt.getLabel().equals(label)) {
				return evt;
			}
		}
		return null;
	}

	// =========================================================================
	// Parameters
	// =========================================================================
	/**
	 * Copies all parameters from a source event to a destination event.
	 * 
	 * @param dest
	 *            the destination event.
	 * @param src
	 *            the source event.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void copyParameters(final IEvent dest, final IEvent src)
			throws RodinDBException {
		for (IParameter param : src.getParameters()) {
			IParameter newParam = dest.createChild(IParameter.ELEMENT_TYPE,
					null, new NullProgressMonitor());
			newParam.setIdentifierString(param.getIdentifierString(),
					new NullProgressMonitor());
		}
	}

	// =========================================================================
	// Guards
	// =========================================================================
	/**
	 * Utility method to get free identifiers of a guard. The order of the free
	 * identifiers is determined syntactically.
	 * 
	 * @param grd
	 *            a guard.
	 * @return the set of free identifiers appearing in the guard.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static List<String> getFreeIdentifiers(final IGuard grd)
			throws RodinDBException {
		return getPredicateFreeIdentifiers(grd.getPredicateString());
	}

	/**
	 * Copies all guards from a source event to a destination event.
	 * 
	 * @param dest
	 *            the destination event.
	 * @param src
	 *            the source event.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void copyGuards(final IEvent dest, final IEvent src)
			throws RodinDBException {
		// Copy guards from the source event.
		for (IGuard grd : src.getGuards()) {
			IGuard newGrd = dest.createChild(IGuard.ELEMENT_TYPE, null,
					new NullProgressMonitor());
			newGrd.setLabel(grd.getLabel(), new NullProgressMonitor());
			newGrd.setPredicateString(grd.getPredicateString(),
					new NullProgressMonitor());
			newGrd.setTheorem(grd.isTheorem(), new NullProgressMonitor());
		}
	}

	/**
	 * Utility method to create additional parameters corresponding to variables
	 * that are used by the decomposed event but are not accessed by the
	 * sub-model. The additional guards are the typing theorems for the added
	 * parameters. This is the last step when decomposing an event.
	 * 
	 * @param src
	 *            the source machine for getting the typing theorem.
	 * @param evt
	 *            the current decomposed event.
	 * @param vars
	 *            the set of accessed variables.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void createExtraParametersAndGuards(final IMachineRoot src,
			final IEvent evt, final Set<String> vars) throws RodinDBException {
		List<String> idents = getFreeIdentifiers(evt);
		idents.removeAll(getSeenCarrierSetsAndConstants(src));
		idents.removeAll(vars);
		for (IParameter param : evt.getParameters()) {
			idents.remove(param.getIdentifierString());
		}

		for (String ident : idents) {
			IParameter newParam = evt.createChild(IParameter.ELEMENT_TYPE,
					null, new NullProgressMonitor());
			newParam.setIdentifierString(ident, new NullProgressMonitor());
		}

		IGuard fstGrd = null;
		IGuard[] grds = evt.getGuards();
		if (grds.length != 0) {
			fstGrd = grds[0];
		}

		for (String ident : idents) {
			String typThm = getTypingTheorem(src, ident);
			IGuard newGrd = evt.createChild(IGuard.ELEMENT_TYPE, fstGrd,
					new NullProgressMonitor());
			newGrd.setLabel(makeTypingLabel(ident), new NullProgressMonitor());
			newGrd.setPredicateString(typThm, new NullProgressMonitor());
			newGrd.setTheorem(true, new NullProgressMonitor());
		}
	}

	// =========================================================================
	// Actions
	// =========================================================================
	/**
	 * Utility method to get assigned identifiers of an action. The order of the
	 * free identifiers is determined syntactically.
	 * 
	 * @param act
	 *            an action.
	 * @return the set of free identifiers appearing in an action.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static List<String> getAssignedIdentifiers(final IAction act)
			throws RodinDBException {
		Assignment parseAssignment = Lib.parseAssignment(act
				.getAssignmentString());
		return toStringList(parseAssignment.getAssignedIdentifiers());
	}

	/**
	 * Utility method to get free identifiers of an action. The order of the
	 * free identifiers is determined syntactically.
	 * 
	 * @param act
	 *            an action.
	 * @return the set of free identifiers appearing in an action.
	 * @throws RodinDBException
	 *             if there are problems accessing the database
	 */
	public static List<String> getFreeIdentifiers(final IAction act)
			throws RodinDBException {
		return getAssignmentFreeIdentifiers(act.getAssignmentString());
	}

	// =========================================================================
	// Other useful methods
	// =========================================================================

	/**
	 * Utility method to get the displayed text of an Event-B element which is a
	 * sub-type of {@link IRodinElement}.
	 * 
	 * <ul>
	 * <li>if the element is a guard {@link IGuard} then return the predicate
	 * string.</li>
	 * 
	 * <li>if the element is a guard {@link IAction} then return the assignment
	 * string.</li>
	 * 
	 * <li>if the element is a invariant {@link IInvariant} then return the
	 * predicate string.</li>
	 * 
	 * <li>if the element is a labeled element {@link ILabeledElement} then
	 * return the label.</li>
	 * 
	 * <li>if the element is an identifier element {@link IIdentifierElement}
	 * then return the identifier string.</li>
	 * 
	 * <li>otherwise (for {@link IRodinElement}) return the element name.</li>
	 * </ul>
	 * 
	 * @param element
	 *            an Event-B element.
	 * @return the displayed text corresponding to the input element.
	 */
	public static String getDisplayedText(final IRodinElement element) {

		// If the element is a guard element then return the predicate of the
		// element.
		if (element instanceof IGuard) {
			try {
				return ((IGuard) element).getPredicateString();
			} catch (RodinDBException e) {
				return ""; //$NON-NLS-1$
			}
		}

		// If the element is an action element then return the assignment of the
		// element.
		if (element instanceof IAction) {
			try {
				return ((IAction) element).getAssignmentString();
			} catch (RodinDBException e) {
				return ""; //$NON-NLS-1$
			}
		}

		// If the element is an invariant element then return the predicate of
		// the element.
		if (element instanceof IInvariant) {
			try {
				return ((IInvariant) element).getPredicateString();
			} catch (RodinDBException e) {
				return ""; //$NON-NLS-1$
			}
		}

		// If the element has label then return the label.
		if (element instanceof ILabeledElement) {
			try {
				return ((ILabeledElement) element).getLabel();
			} catch (RodinDBException e) {
				return ""; //$NON-NLS-1$
			}
		}

		// If the element has identifier string then return it.
		if (element instanceof IIdentifierElement) {
			try {
				return ((IIdentifierElement) element).getIdentifierString();
			} catch (RodinDBException e) {
				return ""; //$NON-NLS-1$
			}
		}

		// Otherwise return the element name of the element.
		return element.getElementName();
	}

	/**
	 * Utility method to get free identifiers of a predicate. The order of the
	 * free identifiers is determined syntactically
	 * {@link Predicate#getSyntacticallyFreeIdentifiers()}.
	 * 
	 * @param predicateString
	 *            a predicate string.
	 * @return the set of free identifiers appearing in a predicate string.
	 */
	public static List<String> getPredicateFreeIdentifiers(
			final String predicateString) {
		Predicate parsePredicate = Lib.parsePredicate(predicateString);
		return toStringList(parsePredicate.getSyntacticallyFreeIdentifiers());
	}

	/**
	 * Utility method to get free identifiers of an assignment. The order of the
	 * free identifiers is determined syntactically
	 * {@link Assignment#getSyntacticallyFreeIdentifiers()}.
	 * 
	 * @param assignmentString
	 *            an assignment.
	 * @return the set of free identifiers appearing in an assignment string.
	 */
	public static List<String> getAssignmentFreeIdentifiers(
			final String assignmentString) {
		Assignment parseAssignment = Lib.parseAssignment(assignmentString);
		return toStringList(parseAssignment.getSyntacticallyFreeIdentifiers());
	}

	/**
	 * Utility method to convert an array of free identifiers to a set of
	 * strings.
	 * 
	 * @param freeIdents
	 *            an array of free identifiers.
	 * @return a set of strings corresponding to the input array.
	 */
	public static List<String> toStringList(final FreeIdentifier[] freeIdents) {
		List<String> result = new ArrayList<String>();

		for (FreeIdentifier freeIdentifier : freeIdents) {
			String name = freeIdentifier.getName();
			if (!result.contains(name)) {
				result.add(name);
			}
		}
		return result;
	}

	/**
	 * Utility method to convert an array of free identifiers to comma separated
	 * values.
	 * 
	 * @param srcStr
	 *            a source string of the free identifiers.
	 * @param idents
	 *            an array of free identifiers.
	 * @return a CSV string corresponding the input free identifiers.
	 */
	public static String identsToCSVString(final String srcStr,
			final FreeIdentifier[] idents) {
		String result = ""; //$NON-NLS-1$
		for (int i = 0; i < idents.length; i++) {
			if (i != 0) {
				result += ", "; //$NON-NLS-1$
			}
			SourceLocation srcLoc = idents[i].getSourceLocation();
			result += srcStr.substring(srcLoc.getStart(), srcLoc.getEnd() + 1);
		}
		return result;
	}

	/**
	 * Utility method to convert an array of free identifiers to a comma
	 * separated primed values.
	 * 
	 * @param srcStr
	 *            a source string of the free identifiers.
	 * @param idents
	 *            an array of free identifiers.
	 * @return a CSV string corresponding the input free identifiers.
	 */
	public static String identsToPrimedCSVString(final String srcStr,
			final FreeIdentifier[] idents) {
		String result = ""; //$NON-NLS-1$
		for (int i = 0; i < idents.length; i++) {
			if (i != 0) {
				result += ", "; //$NON-NLS-1$
			}
			SourceLocation srcLoc = idents[i].getSourceLocation();
			result += srcStr.substring(srcLoc.getStart(), srcLoc.getEnd() + 1)
					+ "'"; //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Creates a new Rodin database exception with the given message and message
	 * arguments.
	 * <p>
	 * The created database exception just wraps up a core exception created
	 * with {@link #newCoreException(String)}.
	 * </p>
	 * 
	 * @param message
	 *            a human-readable message, localized to the current locale.
	 *            Should be one of the messages defined in the {@link Messages}
	 *            class
	 * 
	 * @param args
	 *            parameters to bind with the message
	 * @return a Rodin database exception
	 */
	public static RodinDBException newRodinDBException(final String message,
			final Object... args) {

		return new RodinDBException(new CoreException(new Status(IStatus.ERROR,
				DecompositionPlugin.PLUGIN_ID, IStatus.OK, Messages.bind(
						message, args), null)));
	}

	/**
	 * Returns a label with given prefix and postfix and an underline character
	 * in-between.
	 * 
	 * @param prefix
	 *            a string
	 * @param postfix
	 *            a string
	 * @return a string
	 */
	public static String makeLabel(String prefix, String postfix) {
		return prefix + "_" + postfix; //$NON-NLS-1$
	}

	/**
	 * Returns a label starting with the typing prefix and ending with the given
	 * string
	 * 
	 * @param ident
	 *            a unique identifier designing the typed element
	 * @return a string
	 */
	public static String makeTypingLabel(String ident) {
		return makeLabel(Messages.decomposition_typing, ident);
	}
}