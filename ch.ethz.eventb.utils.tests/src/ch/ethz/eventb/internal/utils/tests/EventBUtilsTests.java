package ch.ethz.eventb.internal.utils.tests;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IAxiom;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IExtendsContext;
import org.eventb.core.IGuard;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.ISeesContext;
import org.eventb.core.IVariable;
import org.eventb.core.IConvergenceElement.Convergence;
import org.junit.Test;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.utils.EventBUtils;

public class EventBUtilsTests extends ChannelSetupTests {

	/**
	 * Test method for {@link EventBUtils#getEventBProject(String)}.
	 */
	@Test
	public void testGetEventBProject() {
		IEventBProject prj = EventBUtils.getEventBProject("Channel");
		assertNotNull("getEventBProject(String) method never returns null", prj);
		assertEquals(
				"The Event-B project returns should be the same as Channel project",
				channelPrj, prj);

		prj = EventBUtils.getEventBProject("Dummy");
		assertNotNull("getEventBProject(String) method never returns null", prj);
		assertFalse("The Dummy Event-B project should not exist", prj
				.getRodinProject().exists());
	}

	/**
	 * Test method for
	 * {@link EventBUtils#createContext(IEventBProject, String, IProgressMonitor)}.
	 */
	@Test
	public void testCreateContext() {
		try {
			IContextRoot ctx = EventBUtils.createContext(channelPrj, "axioms",
					monitor);
			String actualName = ctx.getElementName();
			String expectedName = "axioms";
			assertEquals("Incorect context name", expectedName, actualName);
			ctx = EventBUtils.createContext(channelPrj, "axioms", monitor);
			actualName = ctx.getElementName();
			expectedName = "axioms_0";
			assertEquals("Incorect context name", expectedName, actualName);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBUtils#createExtendsClause(IContextRoot, String, IInternalElement, IProgressMonitor)}.
	 */
	@Test
	public void testCreateExtendsClause() {
		try {
			IExtendsContext extendsClause = EventBUtils.createExtendsClause(
					message_ctx, "ctx0", null, monitor);
			testContextExtendsClauses("Create EXTENDS clause1", message_ctx,
					"ctx0");
			testExtendsClause("Create EXTENDS clause 1", extendsClause, "ctx0");

			extendsClause = EventBUtils.createExtendsClause(message_ctx,
					"ctx1", null, monitor);
			testContextExtendsClauses("Create EXTENDS clause2", message_ctx,
					"ctx0", "ctx1");
			testExtendsClause("Create EXTENDS clause 2", extendsClause, "ctx1");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBUtils#createAxiom(IContextRoot, String, String, boolean, IProgressMonitor)}.
	 */
	@Test
	public void testCreateAxiom() {
		try {
			IAxiom axiom = EventBUtils.createAxiom(message_ctx, "axm3",
					"finite(PROPOSAL)", false, monitor);
			testContextAxioms("Create axiom 1", message_ctx,
					"axm1:finite(MESSAGE):false",
					"thm2:card(MESSAGE) ∈ ℕ1:true",
					"axm3:finite(PROPOSAL):false");
			testAxiom("Create axiom 1", axiom, "axm3:finite(PROPOSAL):false");
			axiom = EventBUtils.createAxiom(message_ctx, "thm4",
					"card(PROPOSAL) ∈ ℕ1", true, monitor);
			testContextAxioms("Create axiom 2", message_ctx,
					"axm1:finite(MESSAGE):false",
					"thm2:card(MESSAGE) ∈ ℕ1:true",
					"axm3:finite(PROPOSAL):false",
					"thm4:card(PROPOSAL) ∈ ℕ1:true");
			testAxiom("Create axiom 2", axiom, "thm4:card(PROPOSAL) ∈ ℕ1:true");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBUtils#createSeesClause(IMachineRoot, String, IInternalElement, IProgressMonitor)}.
	 */
	@Test
	public void testCreateSeesClause() {
		try {
			ISeesContext seesClause = EventBUtils.createSeesClause(channel,
					"ctx", null, monitor);
			testMachineSeesClauses("Create SEES clause 1", channel,
					"message_ctx", "ctx");
			testSeesClause("Create SEES clause 1", seesClause, "ctx");
			seesClause = EventBUtils.createSeesClause(channel,
					"ctx1", seesClause, monitor);
			testMachineSeesClauses("Create SEES clause 2", channel,
					"message_ctx", "ctx1", "ctx");
			testSeesClause("Create SEES clause 2", seesClause, "ctx1");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}
	
	
	/**
	 * Test method for
	 * {@link EventBUtils#createVariable(IMachineRoot, String, IProgressMonitor)}.
	 */
	@Test
	public void testCreateVariable() {
		try {
			IVariable var = EventBUtils.createVariable(channel, "x", monitor);
			testMachineVariables("Create variable 1", channel, "s_count",
					"r_count", "x");
			testVariable("Create variable 1", var, "x");
			var = EventBUtils.createVariable(channel, "y", monitor);
			testMachineVariables("Create variable 2", channel, "s_count",
					"r_count", "x", "y");
			testVariable("Create variable 2", var, "y");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBUtils#createInvariant(IMachineRoot, String, String, boolean, IProgressMonitor)}.
	 */
	@Test
	public void testCreateInvariant() {
		try {
			IInvariant inv = EventBUtils.createInvariant(channel, "thm3",
					"s_count ≥ 0", true, monitor);
			testMachineInvariants("Create invariant 1", channel,
					"inv1:s_count ∈ ℕ:false", "inv2:r_count ∈ ℕ:false",
					"thm3:s_count ≥ 0:true");
			testInvariant("Create invariant 1", inv, "thm3:s_count ≥ 0:true");

			inv = EventBUtils.createInvariant(channel, "inv4", "r_count ≤ 5",
					false, monitor);
			testMachineInvariants("Create invariant 2", channel,
					"inv1:s_count ∈ ℕ:false", "inv2:r_count ∈ ℕ:false",
					"thm3:s_count ≥ 0:true", "inv4:r_count ≤ 5:false");
			testInvariant("Create invariant 2", inv, "inv4:r_count ≤ 5:false");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBUtils#createEvent(IMachineRoot, String, Convergence, boolean, IProgressMonitor)}.
	 */
	@Test
	public void testCreateEvent() {
		try {
			IEvent evt = EventBUtils.createEvent(channel, "evt1",
					Convergence.ANTICIPATED, true, monitor);
			testMachineEvents("Create event 1", channel,
					"INITIALISATION:ORDINARY:false", "sends:ORDINARY:false",
					"receives:ORDINARY:false", "evt1:ANTICIPATED:true");
			testEvent("Create event 1", evt, "evt1:ANTICIPATED:true");
			evt = EventBUtils.createEvent(EO, "evt2", Convergence.CONVERGENT,
					false, monitor);
			testMachineEvents("Create event 2", EO,
					"INITIALISATION:ORDINARY:true", "sends:ORDINARY:true",
					"receives:ORDINARY:false", "evt2:CONVERGENT:false");
			testEvent("Create event 2", evt, "evt2:CONVERGENT:false");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBUtils#createGuard(IEvent, String, String, boolean, IProgressMonitor)}.
	 */
	public void testCreateParameter() {
		try {
			IParameter par = EventBUtils.createParameter(channel_sends, "x",
					monitor);
			testEventParameters("Create parameter 1", channel_sends, "msg", "x");
			testParameter("Create parameter 1", par, "x");
			par = EventBUtils.createParameter(EO_receives, "y", monitor);
			testEventParameters("Create parameter 2", EO_receives, "idx", "y");
			testParameter("Create parameter 2", par, "y");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBUtils#createGuard(IEvent, String, String, boolean, IProgressMonitor)}.
	 */
	public void testCreateGuard() {
		try {
			IGuard grd = EventBUtils.createGuard(channel_sends, "grd2",
					"s_count = 0", false, monitor);
			testEventGuards("Create guard 1", channel_sends,
					"grd1:msg ∈ MESSAGE:false", "grd2:s_count = 0:false");
			testGuard("Create guard 1", grd, "grd2:s_count = 0:false");
			grd = EventBUtils.createGuard(EO_receives, "grd2",
					"idx ≥ r_count + 1", true, monitor);
			testEventGuards("Create guard 2", EO_receives,
					"grd1:idx ∈ channel:false", "grd2:idx ≥ r_count + 1:true");
			testGuard("Create guard 2", grd, "grd2:idx ≥ r_count + 1:true");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBUtils#createAction(IEvent, String, String, IProgressMonitor)}.
	 */
	public void testCreateAction() {
		try {
			IAction act = EventBUtils.createAction(channel_sends, "act2",
					"r_count ≔ r_count + 1", monitor);
			testEventActions("Create action 1", channel_sends,
					"act1:s_count ≔ s_count + 1", "act2:r_count ≔ r_count + 1");
			testAction("Create action 1", act, "act2:r_count ≔ r_count + 1");
			act = EventBUtils.createAction(EO_receives, "act4",
					"s_count ≔ s_count + 1", monitor);
			testEventActions("Create action 2", EO_receives,
					"act1:r_count ≔ r_count + 1",
					"act2:channel ≔ channel ∖ {idx}",
					"act3:receiveds(r_count + 1) ≔ idx",
					"act4:s_count ≔ s_count + 1");
			testAction("Create action 2", act, "act4:s_count ≔ s_count + 1");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

}