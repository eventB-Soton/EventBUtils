/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.utils.tests;

import org.eventb.core.IAction;
import org.eventb.core.IAxiom;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IGuard;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IVariable;
import org.eventb.core.IWitness;
import org.junit.Before;
import org.junit.Test;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.utils.EventBUtils;

/**
 * @author htson
 *         <p>
 *         Abstract class to test handling of Event-B elements. A simple model
 *         of a channel is created by the setup method {@link #setUp()}.
 *         </p>
 */
public abstract class ChannelSetupTests extends AbstractEventBTests {

	/**
	 * Some predefined projects.
	 */
	protected String channelPrjName = "Channel";
	protected IEventBProject channelPrj;

	/**
	 * Some predefined contexts: - message_ctx, size_ctx in channelPrj.
	 */
	String messageCtxName = "message_ctx";
	protected IContextRoot message_ctx;

	String sizeCtxName = "size_ctx";
	IContextRoot size_ctx;

	/**
	 * Some carrier sets.
	 */
	protected ICarrierSet MESSAGE;

	/**
	 * Some constants.
	 */
	protected IConstant max_size;

	/**
	 * Some axioms and theorems.
	 */
	IAxiom message_ctx_axm_1;

	IAxiom message_ctx_thm_1;

	IAxiom size_ctx_axm_1;

	/**
	 * Some predefined machines. - channel, EO, EOIO in project basedPrj.
	 */
	protected String channelMchName = "channel";
	protected IMachineRoot channelMchRoot;

	protected String EOMchName = "EO";
	protected IMachineRoot EOMchRoot;

	protected String EOIOMchName = "EOIO";
	protected IMachineRoot EOIOMchRoot;

	/**
	 * Some variables.
	 */
	IVariable channel_s_count;

	IVariable channel_r_count;

	IVariable EO_s_count;

	IVariable EO_r_count;

	IVariable EO_channel;

	IVariable EO_sents;

	IVariable EO_receiveds;

	IVariable EOIO_s_count;

	IVariable EOIO_r_count;

	IVariable EOIO_channel;

	IVariable EOIO_sents;

	IVariable EOIO_receiveds;

	/**
	 * Some invariants within machines.
	 */
	IInvariant channel_inv_1;

	IInvariant channel_inv_2;

	IInvariant EO_inv_1;

	IInvariant EO_inv_2;

	IInvariant EO_inv_3;

	IInvariant EO_thm_1;

	IInvariant EO_inv_4;

	IInvariant EO_inv_5;

	IInvariant EO_thm_2;

	IInvariant EO_inv_6;

	IInvariant EO_thm_3;

	IInvariant EOIO_inv_1;

	IInvariant EOIO_thm_1;

	IInvariant EOIO_thm_2;

	IInvariant EOIO_thm_3;

	IInvariant EOIO_thm_4;

	IInvariant EOIO_thm_5;

	/**
	 * Some events within machines.
	 */
	IEvent channel_init;

	protected IEvent channel_sends;

	IEvent channel_receives;

	IEvent EO_init;

	IEvent EO_sends;

	protected IEvent EO_receives;

	IEvent EOIO_init;

	IEvent EOIO_sends;

	IEvent EOIO_receives;

	/**
	 * Some parameters of the events
	 */
	IParameter channel_sends_msg;

	IParameter channel_receives_msg;

	IParameter EO_receives_idx;

	/**
	 * Some guards within events
	 */
	IGuard channel_sends_grd_1;

	IGuard channel_receives_grd_1;

	IGuard EO_sends_grd_2;

	IGuard EO_sends_thm_1;

	IGuard EO_receives_grd_1;

	IGuard EOIO_receives_grd_2;

	/**
	 * Some witnesses within events
	 */
	IWitness EO_receives_msg;

	/**
	 * Some actions within events
	 */
	IAction channel_init_act_1;

	IAction channel_init_act_2;

	IAction channel_sends_act_1;

	IAction channel_receives_act_1;

	IAction EO_init_act_3;

	IAction EO_init_act_4;

	IAction EO_init_act_5;

	IAction EO_sends_act_2;

	IAction EO_sends_act_3;

	IAction EO_receives_act_1;

	IAction EO_receives_act_2;

	IAction EO_receives_act_3;

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Create the project
		channelPrj = EventBUtils.createEventBProject(channelPrjName,
				nullMonitor);

		// Create some contexts inside the project
		message_ctx = EventBUtils.createContext(channelPrj, messageCtxName,
				nullMonitor);
		size_ctx = EventBUtils.createContext(channelPrj, sizeCtxName,
				nullMonitor);

		// Create content of message_ctx.
		// CONTEXT message_ctx
		// SETS MESSAGE
		// AXIOMS
		// axm1: finite(MESSAGE)
		// thm1: card(MESSAGE) : NAT1
		// END
		MESSAGE = EventBUtils.createCarrierSet(message_ctx, "MESSAGE", null,
				nullMonitor);
		message_ctx_axm_1 = EventBUtils.createAxiom(message_ctx, "axm1",
				"finite(MESSAGE)", false, null, nullMonitor);
		message_ctx_thm_1 = EventBUtils.createAxiom(message_ctx, "thm1",
				"card(MESSAGE) ∈ ℕ1", true, null, nullMonitor);
		message_ctx.getRodinFile().save(nullMonitor, false);

		// Create content for size_ctx
		// CONTEXT size_ctx
		// CONSTANTS max_size
		// AXIOMS
		// axm1: max_size : NAT1
		// END
		max_size = EventBUtils.createConstant(size_ctx, "max_size", null,
				nullMonitor);
		size_ctx_axm_1 = EventBUtils.createAxiom(size_ctx, "axm1",
				"max_size ∈ ℕ1", false, null, nullMonitor);
		size_ctx.getRodinFile().save(nullMonitor, false);

		// Create some machines inside the projects.
		channelMchRoot = EventBUtils.createMachine(channelPrj, channelMchName,
				nullMonitor);
		EOMchRoot = EventBUtils.createMachine(channelPrj, EOMchName,
				nullMonitor);
		EOIOMchRoot = EventBUtils.createMachine(channelPrj, EOIOMchName,
				nullMonitor);

		// Create content for channel.
		// MACHINE channel
		// SEES message_ctx
		// VARIABLES s_count, r_count
		// INVARIANTS
		// inv1: s_count : NAT
		// inv2: r_count : NAT
		// EVENTS
		// ...
		// END
		EventBUtils.createSeesContextClause(channelMchRoot, messageCtxName,
				null, nullMonitor);
		channel_s_count = EventBUtils.createVariable(channelMchRoot, "s_count",
				null, nullMonitor);
		channel_r_count = EventBUtils.createVariable(channelMchRoot, "r_count",
				null, nullMonitor);
		channel_inv_1 = EventBUtils.createInvariant(channelMchRoot, "inv1",
				"s_count ∈ ℕ", false, null, nullMonitor);
		channel_inv_2 = EventBUtils.createInvariant(channelMchRoot, "inv2",
				"r_count ∈ ℕ", false, null, nullMonitor);

		// INITIALISATION
		// STATUS ordinary
		// BEGIN
		// act1: s_count := 0
		// act2: r_count := 0
		// END
		channel_init = EventBUtils.createEvent(channelMchRoot,
				IEvent.INITIALISATION, Convergence.ORDINARY, false, null,
				nullMonitor);
		channel_init_act_1 = EventBUtils.createAction(channel_init, "act1",
				"s_count ≔ 0", null, nullMonitor);
		channel_init_act_2 = EventBUtils.createAction(channel_init, "act2",
				"r_count ≔ 0", null, nullMonitor);

		// sends
		// STATUS ordinary
		// ANY msg
		// WHERE
		// grd1 : msg : MESSAGE
		// THEN
		// act1: s_count := s_count + 1
		// END
		channel_sends = EventBUtils.createEvent(channelMchRoot, "sends",
				Convergence.ORDINARY, false, null, nullMonitor);
		channel_sends_msg = EventBUtils.createParameter(channel_sends, "msg",
				null, nullMonitor);
		channel_sends_grd_1 = EventBUtils.createGuard(channel_sends, "grd1",
				"msg ∈ MESSAGE", false, null, nullMonitor);
		channel_sends_act_1 = EventBUtils.createAction(channel_sends, "act1",
				"s_count ≔ s_count + 1", null, nullMonitor);

		// receives
		// STATUS ordinary
		// ANY msg
		// WHERE
		// grd1 : msg : MESSAGE
		// THEN
		// act1: r_count := r_count + 1
		// END
		channel_receives = EventBUtils.createEvent(channelMchRoot, "receives",
				Convergence.ORDINARY, false, null, nullMonitor);
		channel_receives_msg = EventBUtils.createParameter(channel_receives,
				"msg", null, nullMonitor);
		channel_receives_grd_1 = EventBUtils.createGuard(channel_receives,
				"grd1", "msg ∈ MESSAGE", false, null, nullMonitor);
		channel_receives_act_1 = EventBUtils.createAction(channel_receives,
				"act1", "r_count ≔ r_count + 1", null, nullMonitor);

		// Save channel
		channelMchRoot.getRodinFile().save(nullMonitor, false);

		// Create content for EO.
		// MACHINE EO
		// REFINES channel
		// SEES message_ctx, size_ctx
		// VARIABLES s_count, r_count, sents, receiveds, channel
		// INVARIANTS
		// inv1: sents : 1..s_count --> MESSAGE
		// inv2: receiveds : 1..r_count >-> 1..s_count
		// inv3: ran(receiveds) \/ channel = 1..s_count
		// thm1: channel <: 1..s_count
		// inv4: ran(receiveds) /\ channel = {}
		// inv5: r_count + card(channel) = s_count
		// thm2: r_count <= s_count
		// inv6: card(channel) <= max_size
		// thm3: s_count <= r_count + max_size
		// EVENTS
		// ...
		// END
		EventBUtils.createRefinesMachineClause(EOMchRoot, channelMchName, null,
				nullMonitor);
		EventBUtils.createSeesContextClause(EOMchRoot, messageCtxName, null,
				nullMonitor);
		EventBUtils.createSeesContextClause(EOMchRoot, sizeCtxName, null,
				nullMonitor);
		EO_s_count = EventBUtils.createVariable(EOMchRoot, "s_count", null,
				nullMonitor);
		EO_r_count = EventBUtils.createVariable(EOMchRoot, "r_count", null,
				nullMonitor);
		EO_sents = EventBUtils.createVariable(EOMchRoot, "sents", null,
				nullMonitor);
		EO_receiveds = EventBUtils.createVariable(EOMchRoot, "receiveds", null,
				nullMonitor);
		EO_channel = EventBUtils.createVariable(EOMchRoot, "channel", null,
				nullMonitor);
		EO_inv_1 = EventBUtils.createInvariant(EOMchRoot, "inv1",
				"sents ∈ 1‥s_count → MESSAGE", false, null, nullMonitor);
		EO_inv_2 = EventBUtils.createInvariant(EOMchRoot, "inv2",
				"receiveds ∈ 1‥r_count ↣ 1‥s_count", false, null, nullMonitor);
		EO_inv_3 = EventBUtils.createInvariant(EOMchRoot, "inv3",
				"ran(receiveds) ∪ channel = 1‥s_count", false, null,
				nullMonitor);
		EO_thm_1 = EventBUtils.createInvariant(EOMchRoot, "thm1",
				"channel ⊆ 1‥s_count", true, null, nullMonitor);
		EO_inv_4 = EventBUtils.createInvariant(EOMchRoot, "inv4",
				"ran(receiveds) ∩ channel = ∅", false, null, nullMonitor);
		EO_inv_5 = EventBUtils.createInvariant(EOMchRoot, "inv5",
				"r_count + card(channel) = s_count", false, null, nullMonitor);
		EO_thm_2 = EventBUtils.createInvariant(EOMchRoot, "thm2",
				"r_count ≤ s_count", true, null, nullMonitor);
		EO_inv_6 = EventBUtils.createInvariant(EOMchRoot, "inv6",
				"card(channel) ≤ max_size", false, null, nullMonitor);
		EO_thm_3 = EventBUtils.createInvariant(EOMchRoot, "thm3",
				"s_count ≤ r_count + max_size", true, null, nullMonitor);

		// INITIALISATION
		// extended
		// STATUS ordinary
		// BEGIN
		// act3: sents := {}
		// act4: receiveds := {}
		// act5: channel := {}
		// END
		EO_init = EventBUtils.createEvent(EOMchRoot, IEvent.INITIALISATION,
				Convergence.ORDINARY, true, null, nullMonitor);
		EO_init.setExtended(true, nullMonitor);
		EO_init_act_3 = EventBUtils.createAction(EO_init, "act3", "sents ≔ ∅",
				null, nullMonitor);
		EO_init_act_4 = EventBUtils.createAction(EO_init, "act4",
				"receiveds ≔ ∅", null, nullMonitor);
		EO_init_act_5 = EventBUtils.createAction(EO_init, "act5",
				"channel ≔ ∅", null, nullMonitor);

		// sends
		// extended
		// STATUS ordinary
		// REFINES sends
		// WHEN
		// grd2 : card(channel) /= max_size
		// thm1 : {s_count + 1 |-> msg} : 1 .. s_count + 1 +-> MESSAGE
		// THEN
		// act2: sents(s_count + 1) := msg
		// act3: channel := channel \/ {s_count + 1}
		// END
		EO_sends = EventBUtils.createEvent(EOMchRoot, "sends",
				Convergence.ORDINARY, true, null, nullMonitor);
		EO_sends.setExtended(true, nullMonitor);
		EventBUtils.createRefinesEventClause(EO_sends, "sends", null,
				nullMonitor);
		EO_sends_grd_2 = EventBUtils.createGuard(EO_sends, "grd2",
				"card(channel) ≠ max_size", false, null, nullMonitor);
		EO_sends_thm_1 = EventBUtils.createGuard(EO_sends, "thm1",
				"{s_count + 1 ↦ msg} ∈ 1 ‥ s_count + 1 ⇸ MESSAGE", true, null,
				nullMonitor);
		EO_sends_act_2 = EventBUtils.createAction(EO_sends, "act2",
				"sents(s_count + 1) ≔ msg", null, nullMonitor);
		EO_sends_act_3 = EventBUtils.createAction(EO_sends, "act3",
				"channel ≔ channel ∪ {s_count + 1}", null, nullMonitor);

		// receives
		// STATUS ordinary
		// REFINES receives
		// ANY idx
		// WHERE
		// grd1 : idx : channel
		// WITH
		// msg: msg = sents(idx)
		// THEN
		// act1: r_count := r_count + 1
		// act2: channel := channel \ {idx}
		// act3: receiveds(r_count + 1) := idx
		// END
		EO_receives = EventBUtils.createEvent(EOMchRoot, "receives",
				Convergence.ORDINARY, false, null, nullMonitor);
		EventBUtils.createRefinesEventClause(EO_receives, "receives", null,
				nullMonitor);
		EO_receives_idx = EventBUtils.createParameter(EO_receives, "idx", null,
				nullMonitor);
		EO_receives_grd_1 = EventBUtils.createGuard(EO_receives, "grd1",
				"idx ∈ channel", false, null, nullMonitor);
		EO_receives_msg = EventBUtils.createWitness(EO_receives, "msg",
				"msg = sents(idx)", null, nullMonitor);
		EO_receives_act_1 = EventBUtils.createAction(EO_receives, "act1",
				"r_count ≔ r_count + 1", null, nullMonitor);
		EO_receives_act_2 = EventBUtils.createAction(EO_receives, "act2",
				"channel ≔ channel ∖ {idx}", null, nullMonitor);
		EO_receives_act_3 = EventBUtils.createAction(EO_receives, "act3",
				"receiveds(r_count + 1) ≔ idx", null, nullMonitor);

		// Save EO
		EOMchRoot.getRodinFile().save(nullMonitor, false);

		// Create content for EOIO.
		// MACHINE EOIO
		// REFINES EO
		// SEES message_ctx, size_ctx
		// VARIABLES s_count, r_count, sents, receiveds, channel
		// INVARIANTS
		// inv1: ran(receiveds) = 1..r_count
		// thm1: r_count : NAT
		// thm2: ran(receiveds) \/ channel = 1..s_count
		// thm3: ran(receiveds) /\ channel = {}
		// thm4: ran(receiveds) = 1 .. r_count
		// thm5: channel = r_count + 1 .. s_count
		// EVENTS
		// ...
		// END
		EventBUtils.createRefinesMachineClause(EOIOMchRoot, EOMchName, null,
				nullMonitor);
		EventBUtils.createSeesContextClause(EOIOMchRoot, messageCtxName, null,
				nullMonitor);
		EventBUtils.createSeesContextClause(EOIOMchRoot, sizeCtxName, null,
				nullMonitor);
		EOIO_s_count = EventBUtils.createVariable(EOIOMchRoot, "s_count", null,
				nullMonitor);
		EOIO_r_count = EventBUtils.createVariable(EOIOMchRoot, "r_count", null,
				nullMonitor);
		EOIO_sents = EventBUtils.createVariable(EOIOMchRoot, "sents", null,
				nullMonitor);
		EOIO_receiveds = EventBUtils.createVariable(EOIOMchRoot, "receiveds",
				null, nullMonitor);
		EOIO_channel = EventBUtils.createVariable(EOIOMchRoot, "channel", null,
				nullMonitor);
		EOIO_inv_1 = EventBUtils.createInvariant(EOIOMchRoot, "inv1",
				"ran(receiveds) = 1‥r_count", false, null, nullMonitor);
		EOIO_thm_1 = EventBUtils.createInvariant(EOIOMchRoot, "thm1",
				"r_count ∈ ℕ", true, null, nullMonitor);
		EOIO_thm_2 = EventBUtils
				.createInvariant(EOIOMchRoot, "thm2",
						"ran(receiveds) ∪ channel = 1‥s_count", true, null,
						nullMonitor);
		EOIO_thm_3 = EventBUtils.createInvariant(EOIOMchRoot, "thm3",
				"ran(receiveds) ∩ channel = ∅", true, null, nullMonitor);
		EOIO_thm_4 = EventBUtils.createInvariant(EOIOMchRoot, "thm4",
				"ran(receiveds) = 1‥r_count", true, null, nullMonitor);
		EOIO_thm_5 = EventBUtils.createInvariant(EOIOMchRoot, "thm5",
				"channel = r_count + 1 ‥ s_count", true, null, nullMonitor);

		// INITIALISATION
		// extended
		// STATUS ordinary
		// END
		EOIO_init = EventBUtils.createEvent(EOIOMchRoot, IEvent.INITIALISATION,
				Convergence.ORDINARY, true, null, nullMonitor);
		EOIO_init.setExtended(true, nullMonitor);

		// sends
		// extended
		// STATUS ordinary
		// REFINES sends
		// END
		EOIO_sends = EventBUtils.createEvent(EOIOMchRoot, "sends",
				Convergence.ORDINARY, true, null, nullMonitor);
		EOIO_sends.setExtended(true, nullMonitor);
		EventBUtils.createRefinesEventClause(EOIO_sends, "sends", null,
				nullMonitor);

		// receives
		// extended
		// STATUS ordinary
		// REFINES receives
		// ANY i
		// WHERE
		// grd2: idx = r_count + 1
		// THEN
		// ...
		// END
		EOIO_receives = EventBUtils.createEvent(EOIOMchRoot, "receives",
				Convergence.ORDINARY, true, null, nullMonitor);
		EOIO_receives.setExtended(true, nullMonitor);
		EventBUtils.createRefinesEventClause(EOIO_receives, "receives", null,
				nullMonitor);
		EOIO_receives_grd_2 = EventBUtils.createGuard(EOIO_receives, "grd2",
				"idx = r_count + 1", false, null, nullMonitor);

		// Save EOIO
		EOIOMchRoot.getRodinFile().save(nullMonitor, false);
	}

	/**
	 * Test the generated context message_ctx.
	 */
	@Test
	public void testMessageContext() {
		testContextExtendsClauses("Incorrect EXTENDS clauses for message_ctx",
				message_ctx);
		testContextCarrierSets("Incorrect SETS for message_ctx", message_ctx,
				"MESSAGE");
		testContextConstants("Incorrect CONSTANTS for message_ctx", message_ctx);
		testContextAxioms("Incorrect AXIOMS for message_ctx", message_ctx,
				"axm1:finite(MESSAGE):false", "thm1:card(MESSAGE) ∈ ℕ1:true");
	}

	/**
	 * Test the generated context size_ctx.
	 */
	@Test
	public void testMaxSizeContext() {
		testContextExtendsClauses("Incorrect EXTENDS clauses for size_ctx",
				size_ctx);
		testContextCarrierSets("Incorrect SETS for size_ctx", size_ctx);
		testContextConstants("Incorrect CONSTANTS for size_ctx", size_ctx,
				"max_size");
		testContextAxioms("Incorrect AXIOMS for size_ctx", size_ctx,
				"axm1:max_size ∈ ℕ1:false");
	}

	/**
	 * Test the generated machine channel.
	 */
	@Test
	public void testChannelMachine() {
		testMachineRefinesClauses("Incorrect REFINES clauses for channel",
				channelMchRoot);
		testMachineSeesClauses("Incorrect SEES clauses for channel",
				channelMchRoot, "message_ctx");
		testMachineVariables("Incorrect VARIABLES for channel", channelMchRoot,
				"s_count", "r_count");
		testMachineInvariants("Incorrect INVARIANTS for channel",
				channelMchRoot, "inv1:s_count ∈ ℕ:false",
				"inv2:r_count ∈ ℕ:false");
		testMachineEvents("Incorrect EVENTS for channel", channelMchRoot,
				"INITIALISATION:ORDINARY:false", "sends:ORDINARY:false",
				"receives:ORDINARY:false");
		try {
			IEvent[] events = channelMchRoot.getEvents();

			// Test INITIALISATION
			IEvent channel_init_evt = events[0];
			testEventRefinesClauses(
					"Incorrect REFINES clauses for INITIALISATION for channel",
					channel_init_evt);
			testEventParameters("Incorrect ANY for INITIALISATION for channel",
					channel_init_evt);
			testEventGuards("Incorrect WHERE for INITIALISATION for channel",
					channel_init_evt);
			testEventWitnesses(
					"Incorrect WITNESSES for INITIALISATION for channel",
					channel_init_evt);
			testEventActions("Incorrect THEN for INITIALISATION for channel",
					channel_init_evt, "act1:s_count ≔ 0", "act2:r_count ≔ 0");

			// Test sends
			IEvent channel_sends_evt = events[1];
			testEventRefinesClauses(
					"Incorrect REFINES clauses for sends for channel",
					channel_sends_evt);
			testEventParameters("Incorrect ANY for sends for channel",
					channel_sends_evt, "msg");
			testEventGuards("Incorrect WHERE for sends for channel",
					channel_sends_evt, "grd1:msg ∈ MESSAGE:false");
			testEventWitnesses("Incorrect WITNESSES for sends for channel",
					channel_sends_evt);
			testEventActions("Incorrect THEN for sends for channel",
					channel_sends_evt, "act1:s_count ≔ s_count + 1");

			// Test receives
			IEvent channel_receives_evt = events[2];
			testEventRefinesClauses(
					"Incorrect REFINES clauses for receives for channel",
					channel_receives_evt);
			testEventParameters("Incorrect ANY for receives for channel",
					channel_receives_evt, "msg");
			testEventGuards("Incorrect WHERE for receives for channel",
					channel_receives_evt, "grd1:msg ∈ MESSAGE:false");
			testEventWitnesses("Incorrect WITNESSES for receives for channel",
					channel_receives_evt);
			testEventActions("Incorrect THEN for receives for channel",
					channel_receives_evt, "act1:r_count ≔ r_count + 1");

		} catch (RodinDBException e) {
			fail("There should not be any RodinDB exception");
		}

	}

	/**
	 * Test the generated machine EO.
	 */
	@Test
	public void testEOMachine() {
		testMachineRefinesClauses("Incorrect REFINES clauses for EO",
				EOMchRoot, "channel");
		testMachineSeesClauses("Incorrect SEES clauses for EO", EOMchRoot,
				"message_ctx", "size_ctx");
		testMachineVariables("Incorrect VARIABLES for EO", EOMchRoot, "s_count",
				"r_count", "sents", "receiveds", "channel");
		testMachineInvariants("Incorrect INVARIANTS for EO", EOMchRoot,
				"inv1:sents ∈ 1‥s_count → MESSAGE:false",
				"inv2:receiveds ∈ 1‥r_count ↣ 1‥s_count:false",
				"inv3:ran(receiveds) ∪ channel = 1‥s_count:false",
				"thm1:channel ⊆ 1‥s_count:true",
				"inv4:ran(receiveds) ∩ channel = ∅:false",
				"inv5:r_count + card(channel) = s_count:false",
				"thm2:r_count ≤ s_count:true",
				"inv6:card(channel) ≤ max_size:false",
				"thm3:s_count ≤ r_count + max_size:true");
		testMachineEvents("Incorrect EVENTS for EO", EOMchRoot,
				"INITIALISATION:ORDINARY:true", "sends:ORDINARY:true",
				"receives:ORDINARY:false");
		try {
			IEvent[] events = EOMchRoot.getEvents();

			// Test INITIALISATION
			IEvent EO_init_evt = events[0];
			testEventRefinesClauses(
					"Incorrect REFINES clauses for INITIALISATION for EO",
					EO_init_evt);
			testEventParameters("Incorrect ANY for INITIALISATION for EO",
					EO_init_evt);
			testEventGuards("Incorrect WHERE for INITIALISATION for EO",
					EO_init_evt);
			testEventWitnesses("Incorrect WITNESSES for INITIALISATION for EO",
					EO_init_evt);
			testEventActions("Incorrect THEN for INITIALISATION for EO",
					EO_init_evt, "act3:sents ≔ ∅", "act4:receiveds ≔ ∅",
					"act5:channel ≔ ∅");

			// Test sends
			IEvent EO_sends_evt = events[1];
			testEventRefinesClauses(
					"Incorrect REFINES clauses for sends for EO", EO_sends_evt,
					"sends");
			testEventParameters("Incorrect ANY for sends for EO", EO_sends_evt);
			testEventGuards("Incorrect WHERE for sends for EO", EO_sends_evt,
					"grd2:card(channel) ≠ max_size:false",
					"thm1:{s_count + 1 ↦ msg} ∈ 1 ‥ s_count + 1 ⇸ MESSAGE:true");
			testEventWitnesses("Incorrect WITNESSES for sends for EO",
					EO_sends_evt);
			testEventActions("Incorrect THEN for sends for EO", EO_sends_evt,
					"act2:sents(s_count + 1) ≔ msg",
					"act3:channel ≔ channel ∪ {s_count + 1}");

			// Test receives
			IEvent EO_receives_evt = events[2];
			testEventRefinesClauses(
					"Incorrect REFINES clauses for receives for EO",
					EO_receives_evt, "receives");
			testEventParameters("Incorrect ANY for receives for EO",
					EO_receives_evt, "idx");
			testEventGuards("Incorrect WHERE for receives for EO",
					EO_receives_evt, "grd1:idx ∈ channel:false");
			testEventWitnesses("Incorrect WITNESSES for receives for EO",
					EO_receives_evt, "msg:msg = sents(idx)");
			testEventActions("Incorrect THEN for receives for EO",
					EO_receives_evt, "act1:r_count ≔ r_count + 1",
					"act2:channel ≔ channel ∖ {idx}",
					"act3:receiveds(r_count + 1) ≔ idx");
		} catch (RodinDBException e) {
			fail("There should not be any RodinDB exception");
		}

	}

	/**
	 * Test the generated machine EOIO.
	 */
	@Test
	public void testEOIOMachine() {
		testMachineRefinesClauses("Incorrect REFINES clauses for EOIO", EOIOMchRoot,
				"EO");
		testMachineSeesClauses("Incorrect SEES clauses for EOIO", EOIOMchRoot,
				"message_ctx", "size_ctx");
		testMachineVariables("Incorrect VARIABLES for EOIO", EOIOMchRoot, "s_count",
				"r_count", "sents", "receiveds", "channel");
		testMachineInvariants("Incorrect INVARIANTS for EOIO", EOIOMchRoot,
				"inv1:ran(receiveds) = 1‥r_count:false",
				"thm1:r_count ∈ ℕ:true",
				"thm2:ran(receiveds) ∪ channel = 1‥s_count:true",
				"thm3:ran(receiveds) ∩ channel = ∅:true",
				"thm4:ran(receiveds) = 1‥r_count:true",
				"thm5:channel = r_count + 1 ‥ s_count:true");
		testMachineEvents("Incorrect EVENTS for EOIO", EOIOMchRoot,
				"INITIALISATION:ORDINARY:true", "sends:ORDINARY:true",
				"receives:ORDINARY:true");

		try {
			IEvent[] events = EOIOMchRoot.getEvents();

			// Test INITIALISATION
			IEvent EOIO_init_evt = events[0];
			testEventRefinesClauses(
					"Incorrect REFINES clauses for INITIALISATION for EOIO",
					EOIO_init_evt);
			testEventParameters("Incorrect ANY for INITIALISATION for EOIO",
					EOIO_init_evt);
			testEventGuards("Incorrect WHERE for INITIALISATION for EOIO",
					EOIO_init_evt);
			testEventWitnesses(
					"Incorrect WITNESSES for INITIALISATION for EOIO",
					EOIO_init_evt);
			testEventActions("Incorrect THEN for INITIALISATION for EOIO",
					EOIO_init_evt);

			// Test sends
			IEvent EOIO_sends_evt = events[1];
			testEventRefinesClauses(
					"Incorrect REFINES clauses for sends for EOIO",
					EOIO_sends_evt, "sends");
			testEventParameters("Incorrect ANY for sends for EOIO",
					EOIO_sends_evt);
			testEventGuards("Incorrect WHERE for sends for EOIO",
					EOIO_sends_evt);
			testEventWitnesses("Incorrect WITNESSES for sends for EOIO",
					EOIO_sends_evt);
			testEventActions("Incorrect THEN for sends for EOIO",
					EOIO_sends_evt);

			// Test receives
			IEvent EOIO_receives_evt = events[2];
			testEventRefinesClauses(
					"Incorrect REFINES clauses for receives for EOIO",
					EOIO_receives_evt, "receives");
			testEventParameters("Incorrect ANY for receives for EOIO",
					EOIO_receives_evt);
			testEventGuards("Incorrect WHERE for receives for EOIO",
					EOIO_receives_evt, "grd2:idx = r_count + 1:false");
			testEventWitnesses("Incorrect WITNESSES for receives for EOIO",
					EOIO_receives_evt);
			testEventActions("Incorrect THEN for receives for EOIO",
					EOIO_receives_evt);

		} catch (RodinDBException e) {
			fail("There should not be any RodinDB exception");
		}
	}

}
