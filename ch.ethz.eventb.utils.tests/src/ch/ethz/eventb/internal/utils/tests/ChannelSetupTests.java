/*******************************************************************************
 * Copyright (c) 2009--2015 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/

package ch.ethz.eventb.internal.utils.tests;

import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.junit.Before;
import org.junit.Test;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.utils.tests.AbstractEventBTests;
import ch.ethz.eventb.utils.tests.ChannelSetup;

/**
 * <p>
 * Utility class for testing the Channel project.
 * </p>
 *
 * @author htson
 * @version 0.2.1
 * @see AbstractEventBTests
 * @since 0.2.2
 */
public class ChannelSetupTests extends AbstractEventBTests {

	/**
	 * Create the <code>Channel</code> project.
	 * 
	 * @see AbstractEventBTests#setUp()
	 */
	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ChannelSetup.setup();
	}

	/**
	 * Test the generated context message_ctx.
	 */
	@Test
	public void testMessageContext() {
		IContextRoot messageCtxRoot = ChannelSetup.getMessageContextRoot();
		testContextExtendsClauses("Incorrect EXTENDS clauses for message_ctx",
				messageCtxRoot);
		testContextCarrierSets("Incorrect SETS for message_ctx",
				messageCtxRoot, "MESSAGE");
		testContextConstants("Incorrect CONSTANTS for message_ctx",
				messageCtxRoot);
		testContextAxioms("Incorrect AXIOMS for message_ctx", messageCtxRoot,
				"axm1:finite(MESSAGE):false", "thm1:card(MESSAGE) ∈ ℕ1:true");
	}

	/**
	 * Test the generated context size_ctx.
	 */
	@Test
	public void testMaxSizeContext() {
		IContextRoot sizeCtxRoot = ChannelSetup.getSizeContextRoot();
		testContextExtendsClauses("Incorrect EXTENDS clauses for size_ctx",
				sizeCtxRoot);
		testContextCarrierSets("Incorrect SETS for size_ctx", sizeCtxRoot);
		testContextConstants("Incorrect CONSTANTS for size_ctx", sizeCtxRoot,
				"max_size");
		testContextAxioms("Incorrect AXIOMS for size_ctx", sizeCtxRoot,
				"axm1:max_size ∈ ℕ1:false");
	}

	/**
	 * Test the generated machine channel.
	 */
	@Test
	public void testChannelMachine() {
		IMachineRoot channelMchRoot = ChannelSetup.getChannelMachineRoot();
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
		IMachineRoot EOMchRoot = ChannelSetup.getEOMachineRoot();
		testMachineRefinesClauses("Incorrect REFINES clauses for EO",
				EOMchRoot, "channel");
		testMachineSeesClauses("Incorrect SEES clauses for EO", EOMchRoot,
				"message_ctx", "size_ctx");
		testMachineVariables("Incorrect VARIABLES for EO", EOMchRoot,
				"s_count", "r_count", "sents", "receiveds", "channel");
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
		IMachineRoot EOIOMchRoot = ChannelSetup.getEOIOMachineRoot();
		testMachineRefinesClauses("Incorrect REFINES clauses for EOIO",
				EOIOMchRoot, "EO");
		testMachineSeesClauses("Incorrect SEES clauses for EOIO", EOIOMchRoot,
				"message_ctx", "size_ctx");
		testMachineVariables("Incorrect VARIABLES for EOIO", EOIOMchRoot,
				"s_count", "r_count", "sents", "receiveds", "channel");
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
