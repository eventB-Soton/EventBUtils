package ch.ethz.eventb.internal.decomposition.wizards;

import static org.eventb.core.IConfigurationElement.DEFAULT_CONFIGURATION;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.BecomesMemberOf;
import org.eventb.core.ast.BecomesSuchThat;
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

/**
 * @author htson
 *         <p>
 *         Utility class contains utility methods related to manipulating
 *         Event-B elements (e.g. Event-B projects, machines, contexts).
 *         </p>
 */
public class EventBUtils {

	/**
	 * Utility method to create a new Event-B project with a given name. If
	 * there exists a project with the given name then the method returns
	 * <code>null</code>. Otherwise, a new Event-B project is created and
	 * returned.
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
		if (rodinProject.exists())
			return null;
		else {
			RodinCore.run(new IWorkspaceRunnable() {

				public void run(IProgressMonitor pMonitor) throws CoreException {
					IProject project = rodinProject.getProject();
					Assert.isTrue(!project.exists(),
							"The underlying project should not exist.");
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
	 *             {@link IRodinProject#getRootElementsOfType(org.rodinp.core.IInternalElementType)}.</li>
	 *             <li>copying any context to the destination project
	 *             {@link IRodinFile#copy(IRodinElement, IRodinElement, String, boolean, IProgressMonitor)}.</li>
	 *             </ul>
	 */
	public static void copyContexts(IEventBProject from, IEventBProject to,
			IProgressMonitor monitor) throws RodinDBException {
		IRodinProject fromPrj = from.getRodinProject();
		IContextRoot[] contexts = fromPrj.getRootElementsOfType(
				IContextRoot.ELEMENT_TYPE);
		for (IContextRoot context : contexts) {
			IRodinFile ctxFile = context.getRodinFile();
			ctxFile.copy(to.getRodinProject(), null, null, true,
					monitor);
		}
		return;
	}

	/**
	 * Utility method to create a new machine (*.bum) with the given name within
	 * an existing project. There must be no construct with the same bare-name
	 * exists already.
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
	 *             {@link IConfigurationElement#setConfiguration(String, IProgressMonitor)}.</li>
	 *             </ul>
	 */
	public static IMachineRoot createMachine(IEventBProject project,
			String fileName, IProgressMonitor monitor) throws RodinDBException {
		monitor.beginTask("Create new machine", 1);
		IRodinFile machine = project.getMachineFile(fileName);
		Assert.isTrue(!machine.exists(), "Machine should not exist");
		machine.create(false, new NullProgressMonitor());
		IInternalElement root = machine.getRoot();
		((IConfigurationElement) root).setConfiguration(DEFAULT_CONFIGURATION,
				monitor);
		monitor.worked(1);
		monitor.done();
		return (IMachineRoot) root;
	}

	/**
	 * Utility method for copying the sees clauses from a source machine to a
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
	 *             {@link IMachineRoot#createChild(org.rodinp.core.IInternalElementType, IInternalElement, IProgressMonitor)}.</li>
	 *             <li>setting the seen context name of the created SEES clause
	 *             {@link ISeesContext#setSeenContextName(String, IProgressMonitor)}.</li>
	 *             </ul>
	 */
	public static void copySeesClauses(IMachineRoot src, IMachineRoot dest,
			IProgressMonitor monitor) throws RodinDBException {
		ISeesContext[] seesClauses = src.getSeesClauses();
		monitor.beginTask("Copy sees context clause", seesClauses.length);
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
	 * Utility method for getting the display text of an Event-B element which
	 * is a sub-type of {@link IRodinElement}.
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
	 * @return the display text corresponding to the input element.
	 */
	public static String getDisplayText(IRodinElement element) {

		// If the element is a guard element then return the predicate of the
		// element.
		if (element instanceof IGuard) {
			try {
				return ((IGuard) element).getPredicateString();
			} catch (RodinDBException e) {
				return "";
			}
		}

		// If the element is an action element then return the assignment of the
		// element.
		if (element instanceof IAction) {
			try {
				return ((IAction) element).getAssignmentString();
			} catch (RodinDBException e) {
				return "";
			}
		}

		// If the element is an invariant element then return the predicate of
		// the element.
		if (element instanceof IInvariant) {
			try {
				return ((IInvariant) element).getPredicateString();
			} catch (RodinDBException e) {
				return "";
			}
		}

		// If the element has label then return the label.
		if (element instanceof ILabeledElement) {
			try {
				return ((ILabeledElement) element).getLabel();
			} catch (RodinDBException e) {
				return "";
			}
		}

		// If the element has identifier string then return it.
		if (element instanceof IIdentifierElement) {
			try {
				return ((IIdentifierElement) element).getIdentifierString();
			} catch (RodinDBException e) {
				return "";
			}
		}

		// Otherwise return the element name of the element.
		return element.getElementName();
	}

	/**
	 * Utility method for getting the set of free identifiers of an input event.
	 * The order of the free identifiers is determined syntactically.
	 * 
	 * @param evt
	 *            an event.
	 * @return the set of free identifiers .
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

		// Initialised the list of free identifiers.
		List<String> idents = new ArrayList<String>();

		// Copy the free identifiers from all guards
		IGuard[] grds = evt.getGuards();
		for (IGuard grd : grds) {
			List<String> grdIdents = getFreeIdentifiers(grd);
			for (String grdIdent : grdIdents) {
				if (!idents.contains(grdIdent))
					idents.add(grdIdent);
			}
		}

		// Copy the free identifiers from all actions
		IAction[] acts = evt.getActions();
		for (IAction act : acts) {
			List<String> actIdents = getFreeIdentifiers(act);
			for (String actIdent : actIdents) {
				if (!idents.contains(actIdent))
					idents.add(actIdent);
			}
		}

		// Remove the parameters
		IParameter[] params = evt.getParameters();
		for (IParameter param : params) {
			idents.remove(param.getIdentifierString());
		}

		return idents;
	}

	/**
	 * Utility method for flatten an Event-B event:
	 * <ul>
	 * <li>If the input event is not extended then return the event.</li>
	 * <li>If the input event is extended then this is the merge
	 * {@link #merge(IEvent, IEvent)} of the event with the flatten of the
	 * abstract event.</li>
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
	 *             <li>flattening the abstract event {@link #flatten(IEvent)}.
	 *             </li>
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
	 * Utility method for getting the abstract event of an input event. Return
	 * <code>null</code> if there is no abstract event (e.g. there is no refines
	 * event clause or the abstract machine does not exist).
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
	private static IEvent getAbstract(IEvent event) throws RodinDBException {
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
	 * Utility method for merging two events by copying the content of the
	 * source event to the destination event.
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
	 *             {@link IEvent#createChild(org.rodinp.core.IInternalElementType, IInternalElement, IProgressMonitor)}.</li>
	 *             <li>setting the identifier string of the created parameter
	 *             {@link IParameter#setIdentifierString(String, IProgressMonitor)}.</li>
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
	 *             {@link IAction#setAssignmentString(String, IProgressMonitor)}.</li>
	 *             <li>setting the extended attribute of the destination event
	 *             {@link IEvent#setExtended(boolean, IProgressMonitor)}.</li>
	 *             </ul>
	 */
	private static IEvent merge(IEvent dest, IEvent src)
			throws RodinDBException {
		
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
	 * Utility method for getting free identifiers of a guard. The order of the
	 * free identifiers is determined syntactically.
	 * 
	 * @param grd
	 *            a guard.
	 * @return the set of free identifiers appeared in the guard.
	 * @throws RodinDBException
	 *             if some errors occurred when getting the predicate string of
	 *             the input guard {@link IGuard#getPredicateString()}.
	 */
	public static List<String> getFreeIdentifiers(IGuard grd)
			throws RodinDBException {
		return getPredicateFreeIdentifiers(grd.getPredicateString());
	}

	/**
	 * Utility method for getting free identifiers of a predicate. The order of
	 * the free identifiers is determined syntactically
	 * {@link Predicate#getSyntacticallyFreeIdentifiers()}.
	 * 
	 * @param predicateString
	 *            a predicate string.
	 * @return the set of free identifiers appeared in a predicate string.
	 */
	public static List<String> getPredicateFreeIdentifiers(String predicateString) {
		Predicate parsePredicate = Lib.parsePredicate(predicateString);
		return toStringList(parsePredicate.getSyntacticallyFreeIdentifiers());
	}

	/**
	 * Utility method for converting an array of free identifiers to a set of
	 * string.
	 * 
	 * @param freeIdents
	 *            an array of free identifiers.
	 * @return a set of string corresponding to the input array.
	 */
	private static List<String> toStringList(FreeIdentifier[] freeIdents) {
		List<String> result = new ArrayList<String>();

		for (FreeIdentifier freeIdentifier : freeIdents) {
			String name = freeIdentifier.getName();
			if (!result.contains(name))
				result.add(name);
		}
		return result;
	}

	/**
	 * Utility method for getting free identifiers of an action. The order of
	 * the free identifiers is determined syntactically.
	 * 
	 * @param act
	 *            an action.
	 * @return the set of free identifiers appeared in an action.
	 * @throws RodinDBException
	 *             if some error occurred when getting the assignment string of
	 *             the input action {@link IAction#getAssignmentString()}.
	 */
	public static List<String> getFreeIdentifiers(IAction act)
			throws RodinDBException {
		return getAssignmentFreeIdentifiers(act.getAssignmentString());
	}

	/**
	 * Utility method for getting free identifiers an assignment. The order of
	 * the free identifiers is determined syntactically
	 * {@link Assignment#getSyntacticallyFreeIdentifiers()}.
	 * 
	 * @param assignmentString
	 *            an assignment string.
	 * @return the set of free identifiers appeared in an assignment string.
	 */
	public static List<String> getAssignmentFreeIdentifiers(
			String assignmentString) {
		Assignment parseAssignment = Lib.parseAssignment(assignmentString);
		return toStringList(parseAssignment.getSyntacticallyFreeIdentifiers());
	}

	/**
	 * Utility method for getting the set of seen carrier sets and constants of
	 * a machine. This is the set of carrier sets and constants from all the
	 * seen contexts in the order of the SEEN clauses.
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
	 * Utility method for getting the set of carrier sets and constants of a
	 * context (include ones from the abstract contexts). This is done by first
	 * flatten the context and get the carrier sets and constants. The carrier
	 * sets are returned before the constants.
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
	 * Utility method for flatten a context. This is done by merging the context
	 * with the content (carrier sets, constants and axioms) of flatten abstract
	 * contexts.
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
	 * Utility method for merging two contexts by copying the content of the
	 * source context into the destination context. The contents of the source
	 * context is copied to the beginning of the destination context.
	 * 
	 * @param dest
	 *            destination context
	 * @param src
	 *            source context
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
	private static IContextRoot merge(IContextRoot dest, IContextRoot src)
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
	 * Utility method for getting the set of free identifiers of an invariant.
	 * The order of the free identifiers is determined syntactically.
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
	 * Utility method for normalising event by normalising the actions of the
	 * event.
	 * 
	 * @param evt
	 *            an event.
	 * @return return the normalised event.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>flattening the input event {@link #flatten(IEvent)}.</li>
	 *             <li>getting the actions of the input event
	 *             {@link IEvent#getActions()}.</li>
	 *             <li>normalise the actions of the input event
	 *             {@link #normalise(IAction)}.</li>
	 *             <li>creating a new child of the input event
	 *             {@link IEvent#createChild(org.rodinp.core.IInternalElementType, IInternalElement, IProgressMonitor)}.</li>
	 *             <li>setting the label of the newly created action
	 *             {@link IAction#setLabel(String, IProgressMonitor)}.</li>
	 *             <li>getting the assignment string of the actions of the input
	 *             event {@link IAction#getAssignmentString()}.</li>
	 *             <li>deleting the actions of the input event
	 *             {@link IAction#delete(boolean, IProgressMonitor)}.</li>
	 *             <li>setting the assignment string of the newly created action
	 *             {@link IAction#setAssignmentString(String, IProgressMonitor)}.</li>
	 *             </ul>
	 */
	public static IEvent normalise(IEvent evt) throws RodinDBException {
		// flatten the event
		evt = flatten(evt);
		
		// Normalise the actions (Rewriting rules 1-4).
		IAction[] acts = evt.getActions();
		
		if (acts.length == 0)
			return evt;
		
		for (IAction act : acts) {
			normalise(act);
		}
		
		// Merge the actions into one (Rewriting rules 4).
		IAction newAct = evt.createChild(IAction.ELEMENT_TYPE, null,
				new NullProgressMonitor());
		newAct.setLabel("merged_act", new NullProgressMonitor());
		String left = "";
		String right = "";
		for (IAction act : acts) {
			String assignmentStr = act.getAssignmentString();
			Assignment assignment = Lib.parseAssignment(assignmentStr);

			assert assignment instanceof BecomesSuchThat;
			BecomesSuchThat bcmsuch = (BecomesSuchThat) assignment;

			FreeIdentifier[] idents = bcmsuch.getAssignedIdentifiers();
			Predicate condition = bcmsuch.getCondition();

			if (!left.equals(""))
				left += ", ";
			left += identsToCSVString(assignmentStr, idents);

			if (!right.equals(""))
				right += " ∧ ";
			SourceLocation srcLoc = condition.getSourceLocation();
			right += assignmentStr.substring(srcLoc.getStart(), srcLoc.getEnd() + 1);

			act.delete(false, new NullProgressMonitor());
		}
		newAct.setAssignmentString(left + " :∣ " + right,
				new NullProgressMonitor());
		return evt;
	}

	/**
	 * Utility method for normalising an action according to the following
	 * rewriting rules.
	 * <ol>
	 * <li>x := E becomes x :| x' = E</li>
	 * <li>x, y := E, F becomes x, y :| x' = E & y' = F</li>
	 * <li>x :: E becomes x :| x' : E</li>
	 * <li>v(E) := F becomes v :| v' = v <+ {E |-> F}</li>
	 * </ol>
	 * 
	 * @param act
	 *            an action.
	 * @throws RodinDBException
	 *             if some errors occurred when
	 *             <ul>
	 *             <li>getting the assignment string of the input action
	 *             {@link IAction#getAssignmentString()}.</li>
	 *             <li>setting the assignment string of the input action
	 *             {@link IAction#setAssignmentString(String, IProgressMonitor)}.</li>
	 *             </ul>
	 */
	public static void normalise(IAction act) throws RodinDBException {
		// TODO Should use formula rewrite or using the type-checked version for
		// getting the before-after predicate?
		
		String assignmentStr = act.getAssignmentString();
		Assignment assignment = Lib.parseAssignment(assignmentStr);
//		assignment.rewrite(new AssignmentNormalisation());
//		act.setAssignmentString(assignment.toString(),
//				new NullProgressMonitor());
		
		// Rule (1) and (3)
		// Rule (2) v(E) := F is done automatically with parseAssignment()
		if (assignment instanceof BecomesEqualTo) {
			BecomesEqualTo bcmeq = (BecomesEqualTo) assignment;
			FreeIdentifier[] idents = bcmeq.getAssignedIdentifiers();
			Expression[] exps = bcmeq.getExpressions();
			
			String newAssignmentStr = "";
			newAssignmentStr += identsToCSVString(assignmentStr, idents);
			newAssignmentStr += " :∣ ";
	
			for (int i = 0; i < idents.length; i++) {
				if (i != 0)
					newAssignmentStr += " ∧ ";
				SourceLocation srcLoc = idents[i].getSourceLocation();
				String identStr = assignmentStr.substring(srcLoc.getStart(),
						srcLoc.getEnd() + 1);
	
				srcLoc = exps[i].getSourceLocation();
				
				String expStr;
				
				if (srcLoc == null)
					// Expression of v(E) := F
					expStr = exps[i].toString();
				else
					expStr = assignmentStr.substring(srcLoc.getStart(),
						srcLoc.getEnd() + 1);
				newAssignmentStr += identStr + "' = " + expStr;
			}
			act
					.setAssignmentString(newAssignmentStr,
							new NullProgressMonitor());
		}
	
		// Rule (3) v :: S ==> v :| v' : S
		if (assignment instanceof BecomesMemberOf) {
			BecomesMemberOf bcmin = (BecomesMemberOf) assignment;
			FreeIdentifier[] idents = bcmin.getAssignedIdentifiers();
			assert idents.length == 1;
	
			SourceLocation srcLoc = idents[0].getSourceLocation();
			String v = assignmentStr.substring(srcLoc.getStart(), srcLoc
					.getEnd() + 1);
			srcLoc = bcmin.getSet().getSourceLocation();
			String E = assignmentStr.substring(srcLoc.getStart(), srcLoc
					.getEnd() + 1);
	
			String newAssignmentStr = v + " :∣ " + v + "' ∈ " + E;
			act
					.setAssignmentString(newAssignmentStr,
							new NullProgressMonitor());
		}
	
	}

	/**
	 * Utility method for converting an array of free identifiers to a comma
	 * separated values string.
	 * 
	 * @param srcStr
	 *            a source string of the free identifiers.
	 * @param idents
	 *            an array of free identifiers.
	 * @return a CSV string corresponding the input free identifiers.
	 */
	public static String identsToCSVString(String srcStr,
			FreeIdentifier[] idents) {
		String result = "";
		for (int i = 0; i < idents.length; i++) {
			if (i != 0)
				result += ", ";
			SourceLocation srcLoc = idents[i].getSourceLocation();
			result += srcStr.substring(srcLoc.getStart(), srcLoc.getEnd() + 1);
		}
		return result;
	}

	/**
	 * Utility method for converting an array of free identifiers to a comma
	 * separated (primed-)values string.
	 * 
	 * @param srcStr
	 *            a source string of the free identifiers.
	 * @param idents
	 *            an array of free identifiers.
	 * @return a CSV string corresponding the input free identifiers.
	 */
	public static String identsToPrimedCSVString(String srcStr,
			FreeIdentifier[] idents) {
		String result = "";
		for (int i = 0; i < idents.length; i++) {
			if (i != 0)
				result += ", ";
			SourceLocation srcLoc = idents[i].getSourceLocation();
			result += srcStr.substring(srcLoc.getStart(), srcLoc.getEnd() + 1)
					+ "'";
		}
		return result;
	}

	/**
	 * Utility method for getting the INITIALISATION of a machine.
	 * 
	 * @param mch
	 *            a machine
	 * @return the INITIALISATION of the input machine or <code>null</code> if
	 *         there is no INITIALISATION.
	 * @throws RodinDBException
	 *             if some errors occurred when getting the event with label
	 *             {@link IEvent#INITIALISATION} ({@link #getEventWithLabel(IMachineRoot, String)}).
	 */
	public static IEvent getInitialisation(IMachineRoot mch)
			throws RodinDBException {
		return getEventWithLabel(mch, IEvent.INITIALISATION);
	}

	/**
	 * Utility method for getting the event of a machine with a given label.
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

	/**
	 * Utility method for getting the list of accessed variables of an input
	 * event. They are the free identifiers of the event except any carrier sets
	 * and constants.
	 * 
	 * @param evt
	 *            an event
	 * @return the list of accessed variables by the event.
	 * @throws RodinDBException
	 *             if errors occurred when
	 *             <ul>
	 *             <li>getting the free identifiers of the event
	 *             {@link #getFreeIdentifiers(IEvent)}.</li>
	 *             <li>getting the seen carrier sets and constants of the
	 *             machine contains the event
	 *             {@link #getSeenCarrierSetsAndConstants(IMachineRoot)}.</li>
	 *             </ul>
	 */
	public static List<String> getAccessedVars(IEvent evt)
			throws RodinDBException {
		List<String> idents = getFreeIdentifiers(evt);

		idents.removeAll(getSeenCarrierSetsAndConstants((IMachineRoot) evt
				.getRoot()));

		return idents;
	}

	/**
	 * Getting the predicate string corresponding to a variable identifier given
	 * an input machine. This is done by checking the static checked version of
	 * the machine. Return <code>null</code> the source machine or the static
	 * checked version of it do not exists, or there is no static checked
	 * variable is the input identifier.
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
				return var + " ∈ "
						+ type.toExpression(FormulaFactory.getDefault());
			}
		}
		return null;
	}

}