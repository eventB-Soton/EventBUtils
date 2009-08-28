package ch.ethz.eventb.decomposition.utils;

import static org.eventb.core.IConfigurationElement.DEFAULT_CONFIGURATION;

import java.util.ArrayList;
import java.util.Collection;
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
import org.eventb.core.IAxiom;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IConfigurationElement;
import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IExtendsContext;
import org.eventb.core.IGuard;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.IInvariant;
import org.eventb.core.ILabeledElement;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IRefinesEvent;
import org.eventb.core.IRefinesMachine;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ISCVariable;
import org.eventb.core.ISeesContext;
import org.eventb.core.ast.Assignment;
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

/**
 * @author htson
 *         <p>
 *         Utility class containing some useful methods to handle Event-B
 *         elements (<i>e.g.</i> Event-B projects, machines, contexts).
 *         </p>
 */
public class EventBUtils {

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
	public static IEventBProject createProject(String projectName,
			IProgressMonitor monitor) throws RodinDBException {
		final IRodinDB rodinDB = RodinCore.getRodinDB();
		final IRodinProject rodinProject = rodinDB.getRodinProject(projectName);
		if (!rodinProject.exists()) {
			RodinCore.run(new IWorkspaceRunnable() {

				public void run(IProgressMonitor pMonitor) throws CoreException {
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
	 * Utility method to create a new machine (*.bum) with the given name within
	 * an existing project. There must be no existing construct with the same
	 * bare-name.
	 * 
	 * @param project
	 *            The Event-B project.
	 * @param fileName
	 *            the full name with of the new machine.
	 * @param monitor
	 *            the progress monitor used to create this machine.
	 * @return the handle to newly created machine.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>creating a new machine
	 *             {@link IRodinFile#create(boolean, IProgressMonitor)}.</li>
	 *             <li>setting the configuration for the created machine
	 *             {@link IConfigurationElement#setConfiguration(String, IProgressMonitor)}
	 *             .</li>
	 *             </ul>
	 */
	public static IMachineRoot createMachine(IEventBProject project,
			String fileName, IProgressMonitor monitor) throws RodinDBException {
		monitor.beginTask(Messages.decomposition_machine, 1);
		IRodinFile machine = project.getMachineFile(fileName);
		Assert.isTrue(!machine.exists(),
				Messages.decomposition_error_existingmachine);
		machine.create(false, new NullProgressMonitor());
		IInternalElement root = machine.getRoot();
		((IConfigurationElement) root).setConfiguration(DEFAULT_CONFIGURATION,
				monitor);
		monitor.worked(1);
		monitor.done();
		return (IMachineRoot) root;
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
	public static void copyContexts(IEventBProject from, IEventBProject to,
			IProgressMonitor monitor) throws RodinDBException {
		IRodinProject fromPrj = from.getRodinProject();
		IContextRoot[] contexts = fromPrj
				.getRootElementsOfType(IContextRoot.ELEMENT_TYPE);
		for (IContextRoot context : contexts) {
			IRodinFile ctxFile = context.getRodinFile();
			ctxFile.copy(to.getRodinProject(), null, null, true, monitor);
		}
		return;
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
	public static void copySeesClauses(IMachineRoot src, IMachineRoot dest,
			IProgressMonitor monitor) throws RodinDBException {
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
	 * Utility method to flatten a context. This is done by merging the context
	 * with the content (carrier sets, constants and axioms) of flattened
	 * abstract contexts.
	 * 
	 * @param ctx
	 *            a context.
	 * @return a flatten version of the input context.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the EXTENDS clause of the input context
	 *             {@link IContextRoot#getExtendsClauses()}.</li>
	 *             <li>getting the abstract context name of the EXTENDS clause
	 *             of the input context
	 *             {@link IExtendsContext#getAbstractContextName()}.</li>
	 *             <li>merging the input context with any abstract context
	 *             {@link #merge(IContextRoot, IContextRoot)}.</li>
	 *             <li>deleting the EXTENDS clauses of the input context
	 *             {@link IExtendsContext#delete(boolean, IProgressMonitor)}.</li>
	 *             </ul>
	 */
	public static IContextRoot flatten(IContextRoot ctx)
			throws RodinDBException {
		IExtendsContext[] extClauses = ctx.getExtendsClauses();
		for (int i = extClauses.length - 1; 0 <= i; i--) {
			IExtendsContext extClause = extClauses[i];
			String absCtxName = extClause.getAbstractContextName();
			IEventBProject prj = ctx.getEventBProject();
			IContextRoot absCtx = (IContextRoot) prj.getContextFile(absCtxName)
					.getRoot();
			ctx = merge(ctx, flatten(absCtx));
			extClause.delete(false, new NullProgressMonitor());
		}
		return ctx;
	}

	/**
	 * Utility method to merge two contexts by copying the content of the source
	 * context into the destination context. The contents of the source context
	 * is copied to the beginning of the destination context.
	 * 
	 * @param dest
	 *            destination context.
	 * @param src
	 *            source context.
	 * @return the merged context.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the carrier sets of the source context
	 *             {@link IContextRoot#getCarrierSets()}.</li>
	 *             <li>creating a new carrier set/constant/axiom in the
	 *             destination context
	 *             {@link IContextRoot#createChild(org.rodinp.core.IInternalElementType, IInternalElement, IProgressMonitor)}.
	 *             <li>
	 *             <li>setting the identifier string of the created carrier set
	 *             {@link ICarrierSet#setIdentifierString(String, IProgressMonitor)}
	 *             .</li>
	 *             <li>getting the constants of the source context
	 *             {@link IContextRoot#getConstants()}.</li>
	 *             <li>setting the identifier string of the created constant
	 *             {@link IConstant#setIdentifierString(String, IProgressMonitor)}
	 *             .</li>
	 *             <li>getting the axioms of the source context
	 *             {@link IContextRoot#getAxioms()}.</li>
	 *             <li>setting the label of the created axiom
	 *             {@link IAxiom#setLabel(String, IProgressMonitor)}.</li>
	 *             <li>setting the predicate string of the created axiom
	 *             {@link IAxiom#setPredicateString(String, IProgressMonitor)}.</li>
	 *             <li>setting the theorem of the created axiom
	 *             {@link IAxiom#setTheorem(boolean, IProgressMonitor)}.</li>
	 *             </ul>
	 */
	public static IContextRoot merge(IContextRoot dest, IContextRoot src)
			throws RodinDBException {
		// Get the first current carrier set.
		ICarrierSet fstSet = null;
		ICarrierSet[] currSets = dest.getCarrierSets();
		if (currSets.length != 0)
			fstSet = currSets[0];

		// Copy carrier sets from the source context.
		ICarrierSet[] sets = src.getCarrierSets();
		for (ICarrierSet set : sets) {
			ICarrierSet newSet = dest.createChild(ICarrierSet.ELEMENT_TYPE,
					fstSet, new NullProgressMonitor());
			newSet.setIdentifierString(set.getIdentifierString(),
					new NullProgressMonitor());
		}

		// Get the first current constant.
		IConstant fstCst = null;
		IConstant[] currCsts = dest.getConstants();
		if (currCsts.length != 0)
			fstCst = currCsts[0];

		// Copy constants from the source context.
		IConstant[] csts = src.getConstants();
		for (IConstant cst : csts) {
			IConstant newCst = dest.createChild(IConstant.ELEMENT_TYPE, fstCst,
					new NullProgressMonitor());
			newCst.setIdentifierString(cst.getIdentifierString(),
					new NullProgressMonitor());
		}

		// Get the first current axiom.
		IAxiom fstAxm = null;
		IAxiom[] currAxms = dest.getAxioms();
		if (currAxms.length != 0)
			fstAxm = currAxms[0];

		// Copy axioms from the abstract context.
		IAxiom[] axms = src.getAxioms();
		for (IAxiom axm : axms) {
			IAxiom newAxm = dest.createChild(IAxiom.ELEMENT_TYPE, fstAxm,
					new NullProgressMonitor());
			newAxm.setLabel(axm.getLabel(), new NullProgressMonitor());
			newAxm.setPredicateString(axm.getPredicateString(),
					new NullProgressMonitor());
			newAxm.setTheorem(axm.isTheorem(), new NullProgressMonitor());
		}
		return dest;
	}

	/**
	 * Utility method to get the set of seen carrier sets and constants of a
	 * machine. This is the set of carrier sets and constants from all the seen
	 * contexts in the order of the SEES clauses.
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
	public static List<String> getSeenCarrierSetsAndConstants(IMachineRoot mch)
			throws RodinDBException {
		List<String> result = new ArrayList<String>();

		ISeesContext[] seesClauses = mch.getSeesClauses();
		for (int i = 0; i < seesClauses.length; i++) {
			ISeesContext seesClause = seesClauses[i];
			String seenCtxName = seesClause.getSeenContextName();
			IEventBProject prj = mch.getEventBProject();
			IContextRoot ctx = (IContextRoot) prj.getContextFile(seenCtxName)
					.getRoot();
			result.addAll(getCarrierSetsAndConstants(ctx));
		}
		return result;
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
		List<String> result = new ArrayList<String>();
		ctx = flatten(ctx);

		// Copy the carrier sets
		ICarrierSet[] sets = ctx.getCarrierSets();
		for (ICarrierSet set : sets) {
			result.add(set.getIdentifierString());
		}

		// Copy the constant
		IConstant[] csts = ctx.getConstants();
		for (IConstant cst : csts) {
			result.add(cst.getIdentifierString());
		}

		return result;
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
	public static void cleanUp(IMachineRoot machine, IProgressMonitor monitor) {
		// Make the machine consistent.
		try {
			IRodinFile rodinFile = machine.getRodinFile();
			if (rodinFile.hasUnsavedChanges())
				rodinFile.makeConsistent(monitor);
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
				if (rodinFile.hasUnsavedChanges())
					rodinFile.makeConsistent(monitor);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
		}
	}

	// =========================================================================
	// Variables
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
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the list of static checked variables from the
	 *             static checked machine
	 *             {@link ISCMachineRoot#getSCVariables()}.</li>
	 *             <li>getting the identifier string of any static checked
	 *             variable {@link ISCVariable#getIdentifierString()}.</li>
	 *             <li>getting the type of the static checked variable
	 *             {@link ISCVariable#getType(FormulaFactory)}.</li>
	 *             </ul>
	 */
	public static String getTypingTheorem(IMachineRoot src, String var)
			throws RodinDBException {
		if (!src.exists())
			return null;
		ISCMachineRoot mchSC = src.getSCMachineRoot();
		if (!mchSC.exists())
			return null;
		ISCVariable[] varSCs = mchSC.getSCVariables();
		for (ISCVariable varSC : varSCs) {
			if (varSC.getIdentifierString().equals(var)) {
				Type type = varSC.getType(FormulaFactory.getDefault());
				return var + " ∈ " //$NON-NLS-1$
						+ type.toExpression(FormulaFactory.getDefault());
			}
		}
		return null;
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
	 *             the input invariant {@link IInvariant#getPredicateString()}
	 *             .</li>
	 */
	public static List<String> getFreeIdentifiers(IInvariant inv)
			throws RodinDBException {
		return getPredicateFreeIdentifiers(inv.getPredicateString());
	}

	/**
	 * Copies the relevant invariants for a set of variables, from a source
	 * machine to a destination machine (recursively).
	 * 
	 * @param mch
	 *            the destination machine.
	 * @param src
	 *            the source machine.
	 * @param vars
	 *            the set of variables (in {@link String}).
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void copyInvariants(IMachineRoot mch, IMachineRoot src,
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
	public static boolean isRelevant(IInvariant inv, Set<String> vars)
			throws RodinDBException {
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
	 * Utility method to get the set of free identifiers of an input event. The
	 * order of the free identifiers is determined syntactically.
	 * 
	 * @param evt
	 *            an event.
	 * @return the set of free identifiers.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>flattening the input event {@link #flatten(IEvent)}.</li>
	 *             <li>getting the guards of the input event
	 *             {@link IEvent#getGuards()}.</li>
	 *             <li>getting the free identifiers of any guard
	 *             {@link #getFreeIdentifiers(IGuard)}.</li>
	 *             <li>getting the actions of the input event
	 *             {@link IEvent#getActions()}.</li>
	 *             <li>getting the free identifiers of any action
	 *             {@link #getFreeIdentifiers(IAction)}.</li>
	 *             <li>getting the parameters of the input event
	 *             {@link IEvent#getParameters()}.</li>
	 *             <li>getting the identifier string of any parameter
	 *             {@link IParameter#getIdentifierString()}.</li>
	 *             </ul>
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
				if (!idents.contains(grdIdent))
					idents.add(grdIdent);
			}
		}

		// Copy the free identifiers from all actions.
		IAction[] acts = evt.getActions();
		for (IAction act : acts) {
			List<String> actIdents = getFreeIdentifiers(act);
			for (String actIdent : actIdents) {
				if (!idents.contains(actIdent))
					idents.add(actIdent);
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
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the extended attribute of the input event
	 *             {@link IEvent#isExtended()}.</li>
	 *             <li>getting the abstract of the input event
	 *             {@link #getAbstract(IEvent)}.</li>
	 *             <li>flattening the abstract event {@link #flatten(IEvent)}.</li>
	 *             <li>merging the input event with the flatten version of the
	 *             abstract event {@link #merge(IEvent, IEvent)}.</li>
	 *             </ul>
	 */
	public static IEvent flatten(IEvent evt) throws RodinDBException {
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
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the REFINES clause of the input event
	 *             {@link IEvent#getRefinesClauses()}.</li>
	 *             <li>getting the abstract event label of the REFINES clause
	 *             {@link IRefinesEvent#getAbstractEventLabel()}.</li>
	 *             <li>getting the REFINES clause the machine contains the input
	 *             event {@link IMachineRoot#getRefinesClauses()}.</li>
	 *             <li>getting the abstract machine corresponding to the REFINES
	 *             clause of the machine
	 *             {@link IRefinesMachine#getAbstractMachine()}.</li>
	 *             <li>getting the abstract events
	 *             {@link IMachineRoot#getEvents()}.</li>
	 *             <li>getting the label of the abstract events
	 *             {@link IEvent#getLabel()}.</li>
	 *             </ul>
	 */
	public static IEvent getAbstract(IEvent event) throws RodinDBException {
		String absEvtLabel;
		if (event.isInitialisation())
			absEvtLabel = IEvent.INITIALISATION;
		else {
			IRefinesEvent[] evtRefinesClauses = event.getRefinesClauses();
			if (evtRefinesClauses.length != 1)
				return null;
			absEvtLabel = evtRefinesClauses[0].getAbstractEventLabel();
		}

		IMachineRoot mch = (IMachineRoot) event.getRoot();

		IRefinesMachine[] refinesClauses = mch.getRefinesClauses();
		if (refinesClauses.length != 1)
			return null;

		IRodinFile abstractMachine = refinesClauses[0].getAbstractMachine();
		if (!abstractMachine.exists())
			return null;
		IMachineRoot absMch = (IMachineRoot) abstractMachine.getRoot();
		IEvent[] absEvts = absMch.getEvents();
		for (IEvent absEvt : absEvts) {
			if (absEvt.getLabel().equals(absEvtLabel))
				return absEvt;
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
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the parameters of the destination event
	 *             {@link IEvent#getParameters()}.</li>
	 *             <li>getting the parameters of the source event
	 *             {@link IEvent#getParameters()}.</li>
	 *             <li>creating a new parameter/guard/action in the destination
	 *             event
	 *             {@link IEvent#createChild(org.rodinp.core.IInternalElementType, IInternalElement, IProgressMonitor)}
	 *             .</li>
	 *             <li>setting the identifier string of the created parameter
	 *             {@link IParameter#setIdentifierString(String, IProgressMonitor)}
	 *             .</li>
	 *             <li>getting the guards of the destination event
	 *             {@link IEvent#getGuards()}.
	 *             <li>
	 *             <li>getting the guards of the source event
	 *             {@link IEvent#getGuards()}.</li>
	 *             <li>setting the label of the created guard
	 *             {@link IGuard#setLabel(String, IProgressMonitor)}.</li>
	 *             <li>setting the predicate string of the created guard
	 *             {@link IGuard#setPredicateString(String, IProgressMonitor)}.</li>
	 *             <li>getting the actions of the destination event
	 *             {@link IEvent#getActions()}.
	 *             <li>
	 *             <li>getting the actions of the source event
	 *             {@link IEvent#getActions()}.</li>
	 *             <li>setting the label of the created action
	 *             {@link IAction#setLabel(String, IProgressMonitor)}.</li>
	 *             <li>setting the assignment of the created action
	 *             {@link IAction#setAssignmentString(String, IProgressMonitor)}
	 *             .</li>
	 *             <li>setting the extended attribute of the destination event
	 *             {@link IEvent#setExtended(boolean, IProgressMonitor)}.</li>
	 *             </ul>
	 */
	public static IEvent merge(IEvent dest, IEvent src) throws RodinDBException {

		// Get the current first parameter.
		IParameter[] currParams = dest.getParameters();
		IParameter fstParam = null;
		if (currParams.length != 0)
			fstParam = currParams[0];

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
		if (currGuards.length != 0)
			fstGrd = currGuards[0];

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
		if (currActs.length != 0)
			fstAct = currActs[0];

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
	 *             if some errors occurred when getting the event with label
	 *             {@link IEvent#INITIALISATION} (
	 *             {@link #getEventWithLabel(IMachineRoot, String)}).
	 */
	public static IEvent getInitialisation(IMachineRoot mch)
			throws RodinDBException {
		return getEventWithLabel(mch, IEvent.INITIALISATION);
	}

	/**
	 * Utility method to get the event of a machine with a given label.
	 * 
	 * @param mch
	 *            a machine
	 * @return the event with the given label of the input machine or
	 *         <code>null</code> if there is no event with the given label.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the events of the input machine
	 *             {@link IMachineRoot#getEvents()}.</li>
	 *             <li>getting the labels of the events
	 *             {@link IEvent#getLabel()}.</li>
	 *             </ul>
	 */
	public static IEvent getEventWithLabel(IMachineRoot mch, String label)
			throws RodinDBException {
		IEvent[] evts = mch.getEvents();
		for (IEvent evt : evts)
			if (evt.getLabel().equals(label))
				return evt;
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
	public static void copyParameters(IEvent dest, IEvent src)
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
	 *             if some errors occurred when getting the predicate string of
	 *             the input guard {@link IGuard#getPredicateString()}.
	 */
	public static List<String> getFreeIdentifiers(IGuard grd)
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
	public static void copyGuards(IEvent dest, IEvent src)
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
	public static void createExtraParametersAndGuards(IMachineRoot src,
			IEvent evt, Set<String> vars) throws RodinDBException {
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
		if (grds.length != 0)
			fstGrd = grds[0];

		for (String ident : idents) {
			String typThm = getTypingTheorem(src, ident);
			IGuard newGrd = evt.createChild(IGuard.ELEMENT_TYPE, fstGrd,
					new NullProgressMonitor());
			newGrd.setLabel(Messages.decomposition_typing + "_" + ident,
					new NullProgressMonitor());
			newGrd.setPredicateString(typThm, new NullProgressMonitor());
			newGrd.setTheorem(true, new NullProgressMonitor());
		}
	}

	// =========================================================================
	// Actions
	// =========================================================================
	/**
	 * Utility method to get free identifiers of an action. The order of the
	 * free identifiers is determined syntactically.
	 * 
	 * @param act
	 *            an action.
	 * @return the set of free identifiers appearing in an action.
	 * @throws RodinDBException
	 *             if some error occurred when getting the assignment string of
	 *             the input action {@link IAction#getAssignmentString()}.
	 */
	public static List<String> getFreeIdentifiers(IAction act)
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
	public static String getDisplayedText(IRodinElement element) {

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
			String predicateString) {
		Predicate parsePredicate = Lib.parsePredicate(predicateString);
		return toStringList(parsePredicate.getSyntacticallyFreeIdentifiers());
	}

	/**
	 * Utility method to get free identifiers of an assignment. The order of the
	 * free identifiers is determined syntactically
	 * {@link Assignment#getSyntacticallyFreeIdentifiers()}.
	 * 
	 * @param assignment
	 *            an assignment.
	 * @return the set of free identifiers appearing in an assignment string.
	 */
	public static List<String> getAssignmentFreeIdentifiers(
			String assignmentString) {
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
	public static List<String> toStringList(FreeIdentifier[] freeIdents) {
		List<String> result = new ArrayList<String>();

		for (FreeIdentifier freeIdentifier : freeIdents) {
			String name = freeIdentifier.getName();
			if (!result.contains(name))
				result.add(name);
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
	public static String identsToCSVString(String srcStr,
			FreeIdentifier[] idents) {
		String result = ""; //$NON-NLS-1$
		for (int i = 0; i < idents.length; i++) {
			if (i != 0)
				result += ", "; //$NON-NLS-1$
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
	public static String identsToPrimedCSVString(String srcStr,
			FreeIdentifier[] idents) {
		String result = ""; //$NON-NLS-1$
		for (int i = 0; i < idents.length; i++) {
			if (i != 0)
				result += ", "; //$NON-NLS-1$
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
	 */
	public static RodinDBException newRodinDBException(String message,
			Object... args) {

		return new RodinDBException(new CoreException(new Status(IStatus.ERROR,
				DecompositionPlugin.PLUGIN_ID, IStatus.OK, Messages.bind(
						message, args), null)));
	}
}