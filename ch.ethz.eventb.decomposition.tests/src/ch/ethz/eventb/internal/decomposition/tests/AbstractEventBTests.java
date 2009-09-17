/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/

package ch.ethz.eventb.internal.decomposition.tests;

import static ch.ethz.eventb.internal.decomposition.utils.EventBUtils.DECOMPOSITION_CONFIGURATION;
import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IAction;
import org.eventb.core.IAxiom;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
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
import org.eventb.core.IConvergenceElement.Convergence;
import org.junit.After;
import org.junit.Before;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;
import org.rodinp.internal.core.debug.DebugHelpers;

/**
 * @author htson
 *         <p>
 *         Abstract class for Event-B tests. This is the simplification of the
 *         builder tests by Laurent Voisin.
 */
public abstract class AbstractEventBTests extends TestCase {

	/**
	 * The progress monitor.
	 */
	private static final IProgressMonitor monitor = new NullProgressMonitor();

	/**
	 * The testing workspace.
	 */
	protected IWorkspace workspace = ResourcesPlugin.getWorkspace();

	/**
	 * Constructor: Create a test case.
	 */
	public AbstractEventBTests() {
		super();
	}

	/**
	 * Constructor: Create a test case with the given name.
	 * 
	 * @param name
	 *            the name of test
	 */
	public AbstractEventBTests(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// ensure autobuilding is turned off
		IWorkspaceDescription wsDescription = workspace.getDescription();
		if (wsDescription.isAutoBuilding()) {
			wsDescription.setAutoBuilding(false);
			workspace.setDescription(wsDescription);
		}

		// disable indexing
		DebugHelpers.disableIndexing();

		// Delete the old workspace
		workspace.getRoot().delete(true, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	@Override
	protected void tearDown() throws Exception {
		workspace.getRoot().delete(true, null);
		super.tearDown();
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
	protected IEventBProject createRodinProject(String name)
			throws CoreException {
		IProject project = workspace.getRoot().getProject(name);
		project.create(null);
		project.open(null);
		IProjectDescription pDescription = project.getDescription();
		pDescription.setNatureIds(new String[] { RodinCore.NATURE_ID });
		project.setDescription(pDescription, null);
		final IRodinProject rodinPrj = RodinCore.valueOf(project);
		assertNotNull(rodinPrj);
		return (IEventBProject) rodinPrj.getAdapter(IEventBProject.class);
	}

	/**
	 * Utility method to create a context with the given bare name. The context
	 * is created as a child of the input Event-B project.
	 * 
	 * @param project
	 *            an Event-B project.
	 * @param bareName
	 *            the bare name (without the extension .buc) of the context
	 * @return the newly created context.
	 * @throws RodinDBException
	 *             if some problems occur.
	 */
	protected IContextRoot createContext(IEventBProject project, String bareName)
			throws RodinDBException {
		IRodinFile file = project.getContextFile(bareName);
		file.create(true, null);
		IContextRoot result = (IContextRoot) file.getRoot();
		result.setConfiguration(DECOMPOSITION_CONFIGURATION, monitor);
		return result;
	}

	/**
	 * Utility method to create an EXTENDS clause within the input context for
	 * an abstract context.
	 * 
	 * @param ctx
	 *            a context.
	 * @param absCtxName
	 *            the abstract context label.
	 * @return the newly created extends clause.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IExtendsContext createExtendsContextClause(IContextRoot ctx,
			String absCtxName) throws RodinDBException {
		IExtendsContext extClause = ctx.createChild(
				IExtendsContext.ELEMENT_TYPE, null, monitor);
		extClause.setAbstractContextName(EventBPlugin
				.getComponentName(absCtxName), monitor);
		return extClause;
	}

	/**
	 * Utility method to create a carrier set within the input context with the
	 * given identifier string.
	 * 
	 * @param ctx
	 *            a context.
	 * @param identifierString
	 *            the identifier string.
	 * @return the newly created carrier set.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected ICarrierSet createCarrierSet(IContextRoot ctx,
			String identifierString) throws RodinDBException {
		ICarrierSet set = ctx.createChild(ICarrierSet.ELEMENT_TYPE, null,
				monitor);
		set.setIdentifierString(identifierString, monitor);
		return set;
	}

	/**
	 * Utility method to create a constant within the input context with the
	 * given identifier string.
	 * 
	 * @param ctx
	 *            a context.
	 * @param identifierString
	 *            the identifier string.
	 * @return the newly created constant.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IConstant createConstant(IContextRoot ctx, String identifierString)
			throws RodinDBException {
		IConstant cst = ctx.createChild(IConstant.ELEMENT_TYPE, null, monitor);
		cst.setIdentifierString(identifierString, monitor);
		return cst;
	}

	/**
	 * Utility method to create an axiom within the input context with the given
	 * label and predicate string.
	 * 
	 * @param ctx
	 *            a context.
	 * @param label
	 *            the label.
	 * @param predStr
	 *            the predicate string.
	 * @param isTheorem
	 *            <code>true</code> if the axiom is derivable,
	 *            <code>false</code> otherwise.
	 * @return the newly created axiom.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IAxiom createAxiom(IContextRoot ctx, String label,
			String predStr, boolean isTheorem) throws RodinDBException {
		IAxiom axm = ctx.createChild(IAxiom.ELEMENT_TYPE, null, monitor);
		axm.setLabel(label, monitor);
		axm.setPredicateString(predStr, monitor);
		axm.setTheorem(isTheorem, monitor);
		return axm;
	}

	/**
	 * Utility method to create a machine with the given bare name. The machine
	 * is created as a child of the input Event-B project.
	 * 
	 * @param bareName
	 *            the bare name (without the extension .bum) of the context
	 * @return the newly created context.
	 * @throws RodinDBException
	 *             if some problems occur.
	 */
	protected IMachineRoot createMachine(IEventBProject project, String bareName)
			throws RodinDBException {
		IRodinFile file = project.getMachineFile(bareName);
		file.create(true, null);
		IMachineRoot result = (IMachineRoot) file.getRoot();
		result.setConfiguration(DECOMPOSITION_CONFIGURATION, monitor);
		return result;
	}

	/**
	 * Utility method to create a REFINES machine clause within the input
	 * machine for the abstract machine.
	 * 
	 * @param mch
	 *            a machine.
	 * @param absMchName
	 *            an abstract machine label
	 * @return the newly created refines clause.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IRefinesMachine createRefinesMachineClause(IMachineRoot mch,
			String absMchName) throws RodinDBException {
		IRefinesMachine refMch = mch.createChild(IRefinesMachine.ELEMENT_TYPE,
				null, monitor);
		refMch.setAbstractMachineName(
				EventBPlugin.getComponentName(absMchName), monitor);
		return refMch;
	}

	/**
	 * Utility method to create a SEES clause within the input machine for the
	 * input context.
	 * 
	 * @param mch
	 *            a machine.
	 * @param ctxName
	 *            a context.
	 * @return the newly created sees clause ({@link ISeesContext}.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected ISeesContext createSeesContextClause(IMachineRoot mch,
			String ctxName) throws RodinDBException {
		ISeesContext seesContext = mch.createChild(ISeesContext.ELEMENT_TYPE,
				null, monitor);
		seesContext.setSeenContextName(ctxName, null);
		return seesContext;
	}

	/**
	 * Utility method to create a variable within the input machine with the
	 * given identifier string.
	 * 
	 * @param mch
	 *            a machine.
	 * @param identifierString
	 *            the identifier string.
	 * @return the newly created variable.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IVariable createVariable(IMachineRoot mch, String identifierString)
			throws RodinDBException {
		IVariable var = mch.createChild(IVariable.ELEMENT_TYPE, null, monitor);
		var.setIdentifierString(identifierString, monitor);
		return var;
	}

	/**
	 * Utility method to create an invariant within the input machine with a
	 * given label and predicate string.
	 * 
	 * @param mch
	 *            a machine.
	 * @param label
	 *            the label of the invariant.
	 * @param predicate
	 *            the predicate string of the invariant.
	 * @return the newly created invariant.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IInvariant createInvariant(IMachineRoot mch, String label,
			String predicate, boolean isTheorem) throws RodinDBException {
		IInvariant inv = mch
				.createChild(IInvariant.ELEMENT_TYPE, null, monitor);
		inv.setLabel(label, monitor);
		inv.setPredicateString(predicate, monitor);
		inv.setTheorem(isTheorem, monitor);
		return inv;
	}

	/**
	 * Utility method to create an event within the input machine with the given
	 * label. By default, the extended attribute of the event is set to
	 * <code>false</code>. and the convergence status is set to
	 * <code>ordinary</code>
	 * 
	 * @param mch
	 *            a machine.
	 * @param label
	 *            the label of the event.
	 * @return the newly created event.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IEvent createEvent(IMachineRoot mch, String label)
			throws RodinDBException {
		IEvent event = mch.createChild(IEvent.ELEMENT_TYPE, null, monitor);
		event.setLabel(label, monitor);
		event.setExtended(false, monitor);
		event.setConvergence(Convergence.ORDINARY, monitor);
		return event;
	}

	/**
	 * Utility method to create the refines event clause within the input event
	 * with the given abstract event label.
	 * 
	 * @param evt
	 *            an event.
	 * @param absEvtLabel
	 *            the abstract event label.
	 * @return the newly created refines event clause.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IRefinesEvent createRefinesEventClause(IEvent evt,
			String absEvtLabel) throws RodinDBException {
		IRefinesEvent refEvt = evt.createChild(IRefinesEvent.ELEMENT_TYPE,
				null, monitor);
		refEvt.setAbstractEventLabel(absEvtLabel, monitor);
		return refEvt;
	}

	/**
	 * Utility method to create a parameter within the input event with the
	 * given identifier string.
	 * 
	 * @param evt
	 *            an event.
	 * @param identifierString
	 *            the identifier string.
	 * @return the newly created parameter.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IParameter createParameter(IEvent evt, String identifierString)
			throws RodinDBException {
		IParameter param = evt.createChild(IParameter.ELEMENT_TYPE, null,
				monitor);
		param.setIdentifierString(identifierString, monitor);
		return param;
	}

	/**
	 * Utility method to create a guard within the input event with the given
	 * label and predicate string.
	 * 
	 * @param evt
	 *            an event.
	 * @param label
	 *            the label of the guard.
	 * @param predicateString
	 *            the predicate string of the guard.
	 * @return the newly created guard.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IGuard createGuard(IEvent evt, String label,
			String predicateString) throws RodinDBException {
		IGuard grd = evt.createChild(IGuard.ELEMENT_TYPE, null, monitor);
		grd.setLabel(label, monitor);
		grd.setPredicateString(predicateString, monitor);
		return grd;
	}

	/**
	 * Utility method to create a witness within the input event with the given
	 * label and predicate string.
	 * 
	 * @param evt
	 *            an event.
	 * @param label
	 *            the label of the witness.
	 * @param predicateString
	 *            the predicate string of the witness.
	 * @return the newly created witness.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IWitness createWitness(IEvent evt, String label,
			String predicateString) throws RodinDBException {
		IWitness wit = evt.createChild(IWitness.ELEMENT_TYPE, null, monitor);
		wit.setLabel(label, monitor);
		wit.setPredicateString(predicateString, monitor);
		return wit;
	}

	/**
	 * Utility method to create an action within the input event with the given
	 * label and assignment string.
	 * 
	 * @param evt
	 *            an event
	 * @param label
	 *            the label of the assignment
	 * @param assignmentString
	 *            the assignment string of the action
	 * @return the newly created action
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected IAction createAction(IEvent evt, String label,
			String assignmentString) throws RodinDBException {
		IAction act = evt.createChild(IAction.ELEMENT_TYPE, null, monitor);
		act.setLabel(label, monitor);
		act.setAssignmentString(assignmentString, monitor);
		return act;
	}

	/**
	 * Utility method to test event signature (e.g. label, extended and
	 * convergence attributes and abstract events).
	 * 
	 * @param message
	 *            a message.
	 * @param evt
	 *            an event.
	 * @param expLabel
	 *            the expected label.
	 * @param expExtended
	 *            the expected extended attribute.
	 * @param expConv
	 *            the expected convergence attribute.
	 * @param expAbsEvts
	 *            the expected abstract events.
	 */
	protected void testEventSignature(String message, IEvent evt,
			String expLabel, boolean expExtended, Convergence expConv,
			String... expAbsEvts) {
		try {
			assertEquals(message + ": Label incorrect", expLabel, evt
					.getLabel());
			assertEquals(message + ": Extended attribute incorrect",
					expExtended, evt.isExtended());
			assertEquals(message + ": Convergence attribute incorrect",
					expConv, evt.getConvergence());
			IRefinesEvent[] refClauses = evt.getRefinesClauses();
			assertEquals(message + ": Incorrect number of refines clauses",
					expAbsEvts.length, refClauses.length);
			for (IRefinesEvent refClause : refClauses) {
				String absEvtLabel = refClause.getAbstractEventLabel();
				boolean found = false;
				for (String expAbsEvt : expAbsEvts) {
					if (expAbsEvt.equals(absEvtLabel)) {
						found = true;
						break;
					}
				}
				if (!found) {
					fail("Do not expect refine clause for  " + absEvtLabel);
				}
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method to test event parameters.
	 * 
	 * @param message
	 *            a message.
	 * @param evt
	 *            an event.
	 * @param expected
	 *            the expected set of parameters (order is important).
	 */
	protected void testEventParameters(String message, IEvent evt,
			String... expected) {
		try {
			IParameter[] params = evt.getParameters();
			assertEquals(message + ": Incorrect number of parameters",
					expected.length, params.length);
			for (int i = 0; i < params.length; i++) {
				assertEquals(message + ": Incorrect parameter", expected[i],
						params[i].getIdentifierString());
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method to test event guards.
	 * 
	 * @param message
	 *            a message.
	 * @param evt
	 *            an event.
	 * @param expected
	 *            expected set of guards (each guard is in the form
	 *            "label: predicate: isTheorem") and order is important.
	 */
	protected void testEventGuards(String message, IEvent evt,
			String... expected) {
		try {
			IGuard[] grds = evt.getGuards();
			assertEquals(message + ": Incorrect number of guards",
					expected.length, grds.length);
			for (int i = 0; i < grds.length; i++) {
				testGuard(message, grds[i], expected[i]);

			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method to test a guard.
	 * 
	 * @param message
	 *            a message.
	 * @param grd
	 *            a guard.
	 * @param expected
	 *            expected guard (in the form of "label: predicate: isTheorem").
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	protected void testGuard(String message, IGuard grd, String expected)
			throws RodinDBException {
		String actual = grd.getLabel() + ": " + grd.getPredicateString() + ": "
				+ grd.isTheorem();
		assertEquals(message + ": Incorrect guard", expected, actual);
	}

	/**
	 * Utility method to test event witnesses.
	 * 
	 * @param message
	 *            a message.
	 * @param evt
	 *            an event.
	 * @param expected
	 *            expected set of witnesses (each witness is in the form
	 *            "label: predicate").
	 */
	protected void testEventWitnesses(String message, IEvent evt,
			String... expected) {
		try {
			IWitness[] wits = evt.getWitnesses();
			assertEquals(message + ": Incorrect number of witnesses",
					expected.length, wits.length);
			for (IWitness wit : wits) {
				String actual = wit.getLabel() + ": "
						+ wit.getPredicateString();
				boolean found = false;
				for (String expGrd : expected) {
					if (expGrd.equals(actual)) {
						found = true;
						break;
					}
				}
				if (!found) {
					fail(message + ": Do not expect witness " + actual);
				}
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method to test event actions.
	 * 
	 * @param message
	 *            a message.
	 * @param evt
	 *            an event.
	 * @param expected
	 *            expected set of actions (each action is in the form
	 *            "label: assignment") and the order is important.
	 */
	protected void testEventActions(String message, IEvent evt,
			String... expected) {
		try {
			IAction[] acts = evt.getActions();
			assertEquals(message + ": Incorrect number of actions",
					expected.length, acts.length);
			for (int i = 0; i < acts.length; i++) {
				testAction(message, acts[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method to test an action.
	 * 
	 * @param message
	 *            a message
	 * @param act
	 *            an action
	 * @param expected
	 *            expected action (in the form "label: assignment").
	 */
	protected void testAction(String message, IAction act, String expected) {
		try {
			String actual = act.getLabel() + ": " + act.getAssignmentString();
			assertEquals(message + ": Incorrect action", expected, actual);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
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
	protected IEvent getEventWithLabel(IMachineRoot mch, String label)
			throws RodinDBException {
		IEvent[] evts = mch.getEvents();
		for (IEvent evt : evts)
			if (evt.getLabel().equals(label))
				return evt;
		return null;
	}

}
