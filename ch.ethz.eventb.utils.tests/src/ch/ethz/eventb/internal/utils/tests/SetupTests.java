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
package ch.ethz.eventb.internal.utils.tests;

import org.eventb.core.IAction;

import org.eventb.core.IAxiom;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IGuard;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IVariable;
import org.eventb.core.IWitness;
import org.junit.Before;

import ch.ethz.eventb.utils.tests.AbstractEventBTests;

/**
 * @author htson
 *         <p>
 *         Abstract class to test handling of Event-B elements. A simple model
 *         of a channel is created by the setup method {@link #setUp()}.
 *         </p>
 */
public abstract class SetupTests extends AbstractEventBTests {

	/**
	 * Some predefined projects.
	 */
	protected IEventBProject channelPrj;
	
	/**
	 * Some predefined contexts:
	 * - message_ctx, size_ctx in channelPrj.
	 */
	protected IContextRoot message_ctx;
		
	protected IContextRoot size_ctx;

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
	protected IAxiom axm_message_1;
	
	protected IAxiom thm_message_2;

	protected IAxiom axm_maxsize_1;

	/**
	 * Some predefined machines.
	 * - channel, EO, EOIO in project basedPrj.
	 */
	protected IMachineRoot channel;
	
	protected IMachineRoot EO;
	
	protected IMachineRoot EOIO;
	
	/**
	 * Some variables.
	 */
	protected IVariable s_count_channel;

	protected IVariable r_count_channel;
		
	protected IVariable s_count_EO;

	protected IVariable r_count_EO;

	protected IVariable channel_EO;

	protected IVariable sents_EO;

	protected IVariable receiveds_EO;

	protected IVariable s_count_EOIO;

	protected IVariable r_count_EOIO;

	protected IVariable channel_EOIO;

	protected IVariable sents_EOIO;

	protected IVariable receiveds_EOIO;

	/**
	 * Some invariants within machines.
	 */
	protected IInvariant inv_channel_1;
	
	protected IInvariant inv_channel_2;
	
	protected IInvariant inv_EO_1;
	
	protected IInvariant inv_EO_2;

	protected IInvariant inv_EO_3;
	
	protected IInvariant thm_EO_4;

	protected IInvariant inv_EO_5;
	
	protected IInvariant inv_EO_6;

	protected IInvariant thm_EO_7;
	
	protected IInvariant inv_EO_8;
	
	protected IInvariant thm_EO_9;

	protected IInvariant inv_EOIO_1;
	
	protected IInvariant thm_EOIO_2;

	/**
	 * Some events within machines.
	 */
	protected IEvent init_channel;
	
	protected IEvent sends_channel;

	protected IEvent receives_channel;

	protected IEvent init_EO;
	
	protected IEvent sends_EO;

	protected IEvent receives_EO;

	protected IEvent init_EOIO;
	
	protected IEvent sends_EOIO;

	protected IEvent receives_EOIO;

	/**
	 * Some parameters of the events
	 */
	protected IParameter msg_sends_channel;

	protected IParameter msg_receives_channel;

	protected IParameter idx_receives_EO;

	/**
	 * Some guards within events
	 */
	protected IGuard grd_sends_channel_1;

	protected IGuard grd_receives_channel_1;

	protected IGuard grd_sends_EO_2;

	protected IGuard grd_receives_EO_1;

	protected IGuard grd_receives_EOIO_2;

	/**
	 * Some witnesses within events
	 */
	protected IWitness msg_receives_EO;
	
	/**
	 * Some actions within events
	 */
	protected IAction act_init_channel_1;
	
	protected IAction act_init_channel_2;

	protected IAction act_sends_channel_1;

	protected IAction act_receives_channel_1;

	protected IAction act_init_EO_3;
	
	protected IAction act_init_EO_4;

	protected IAction act_init_EO_5;

	protected IAction act_sends_EO_2;

	protected IAction act_sends_EO_3;

	protected IAction act_receives_EO_1;

	protected IAction act_receives_EO_2;

	protected IAction act_receives_EO_3;

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Create the project
		channelPrj = createEventBProject("Channel"); //$NON-NLS-1$
		
		// Create some contexts inside the project
		message_ctx = createContext(channelPrj, "message_ctx");
		size_ctx = createContext(channelPrj, "size_ctx");
		
		// Create content of message_ctx.
		// CONTEXT message_ctx
		// SETS MESSAGE
		// AXIOMS
		//   axm1: finite(MESSAGE)
		//   thm2: card(MESSAGE) : NAT1
		// END
		MESSAGE = createCarrierSet(message_ctx, "MESSAGE");
		axm_message_1 = createAxiom(message_ctx, "axm1",
				"finite(MESSAGE)", false);
		thm_message_2 = createAxiom(message_ctx, "thm2",
				"card(MESSAGE) ∈ ℕ1", true);
		message_ctx.getRodinFile().save(monitor, false);
		
		// Create content for size_ctx
		// CONTEXT size_ctx
		// CONSTANTS max_size
		// AXIOMS
		//   axm1: max_size : NAT1
		// END
		max_size = createConstant(size_ctx, "max_size");
		axm_maxsize_1 = createAxiom(size_ctx, "axm1", "max_size ∈ ℕ1",
				false);
		size_ctx.getRodinFile().save(monitor, false);
		
		// Create some machines inside the projects
		channel = createMachine(channelPrj, "channel");
		EO = createMachine(channelPrj, "EO");
		EOIO = createMachine(channelPrj, "EOIO");
		
		// Create content for channel.
		// MACHINE channel
		// SEES message_ctx
		// VARIABLES s_count, r_count
		// INVARIANTS
		//   inv1: s_count : NAT
		//   inv2: r_count : NAT
		// EVENTS
		//   ...
		// END
		createSeesContextClause(channel, "message_ctx");
		s_count_channel = createVariable(channel, "s_count");
		r_count_channel = createVariable(channel, "r_count");
		inv_channel_1 = createInvariant(channel, "inv1",
				"s_count ∈ ℕ", false);
		inv_channel_2 = createInvariant(channel, "inv2",
				"r_count ∈ ℕ", false);

		//   INITIALISATION
		//   STATUS ordinary
		//   BEGIN
		//     act1: s_count := 0
		//     act2: r_count := 0
		//   END
		init_channel = createEvent(channel, IEvent.INITIALISATION);
		act_init_channel_1 = createAction(init_channel, "act1",
				"s_count ≔ 0");
		act_init_channel_2 = createAction(init_channel, "act2",
				"r_count ≔ 0");
		
		//   sends
		//   STATUS ordinary
		//   ANY msg
		//   WHERE
		//     grd1 : msg :  MESSAGE
		//   THEN
		//     act1: s_count := s_count + 1
		//   END
		sends_channel = createEvent(channel, "sends");
		msg_sends_channel = createParameter(sends_channel, "msg");
		grd_sends_channel_1 = createGuard(sends_channel, "grd1",
				"msg ∈ MESSAGE");
		act_sends_channel_1 = createAction(sends_channel, "act1",
				"s_count ≔ s_count + 1");

		//   receives
		//   STATUS ordinary
		//   ANY msg
		//   WHERE
		//     grd1 : msg : MESSAGE
		//   THEN
		//     act1: s_count := s_count + 1
		//   END
		receives_channel = createEvent(channel, "receives");
		msg_receives_channel = createParameter(receives_channel, "msg");
		grd_receives_channel_1 = createGuard(receives_channel, "grd1",
				"msg ∈ MESSAGE");
		act_receives_channel_1 = createAction(receives_channel, "act1",
				"r_count ≔ r_count + 1");

		// Save channel
		channel.getRodinFile().save(monitor, false);

		// Create content for EO.
		// MACHINE EO
		// REFINES channel
		// SEES message_ctx, size_ctx
		// VARIABLES s_count, r_count, channel, sents, receiveds
		// INVARIANTS
		//   inv1: sents : 1..s_count --> MESSAGE
		//   inv2: receiveds : 1..r_count >-> 1..s_count
		//   inv3: ran(receiveds) \/ channel = 1..s_count
		//   thm4: channel <: 1..s_count
		//   inv5: ran(receiveds) /\ channel = {}
		//   inv6: r_count + card(channel) = s_count
		//   thm7: r_count <= s_count
		//   inv8: card(channel) <= max_size
		//   thm9: s_count <= r_count + max_size
		// EVENTS
		//   ...
		// END
		createRefinesMachineClause(EO, "channel");
		createSeesContextClause(EO, "message_ctx");
		createSeesContextClause(EO, "size_ctx");
		s_count_EO = createVariable(EO, "s_count");
		r_count_EO = createVariable(EO, "r_count");
		channel_EO = createVariable(EO, "channel");
		sents_EO = createVariable(EO, "sents");
		receiveds_EO = createVariable(EO, "receiveds");
		inv_EO_1 = createInvariant(EO, "inv1",
				"sents ∈ 1‥s_count → MESSAGE", false);
		inv_EO_2 = createInvariant(EO, "inv2",
				"receiveds ∈ 1‥r_count ↣ 1‥s_count", false);
		inv_EO_3 = createInvariant(EO, "inv3",
				"ran(receiveds) ∪ channel = 1‥s_count", false);
		thm_EO_4 = createInvariant(EO, "thm4",
				"channel ⊆ 1‥s_count", true);
		inv_EO_5 = createInvariant(EO, "inv5",
				"ran(receiveds) ∩ channel = ∅", false);
		inv_EO_6 = createInvariant(EO, "inv6",
				"r_count + card(channel) = s_count", false);
		thm_EO_7 = createInvariant(EO, "thm7",
				"r_count ≤ s_count", true);
		inv_EO_8 = createInvariant(EO, "inv8",
				"card(channel) ≤ max_size", false);
		thm_EO_9 = createInvariant(EO, "thm9",
				"s_count ≤ r_count + max_size", false);
	
		//   INITIALISATION
		//     extended
		//   STATUS ordinary
		//   BEGIN
		//     act3: channel := {}
		//     act4: sents := {}
		//     act5: receiveds := {}
		//   END
		init_EO = createEvent(EO, IEvent.INITIALISATION);
		init_EO.setExtended(true, monitor);
		act_init_EO_3 = createAction(init_EO, "act3", "channel ≔ ∅");
		act_init_EO_4 = createAction(init_EO, "act4", "sents ≔ ∅");
		act_init_EO_5 = createAction(init_EO, "act5", "receiveds ≔ ∅");

		//   sends
		//     extended
		//   STATUS ordinary
		//   REFINES sends
		//   WHEN
		//     grd2 : card(channel) /= max_size
		//   THEN
		//     act2: sents(s_count + 1) := msg
		//     act3: channel := channel \/ {s_count + 1}
		//   END
		sends_EO = createEvent(EO, "sends");
		sends_EO.setExtended(true, monitor);
		createRefinesEventClause(sends_EO, "sends");
		grd_sends_EO_2 = createGuard(sends_EO, "grd2",
				"card(channel) ≠ max_size");
		act_sends_EO_2 = createAction(sends_EO, "act2",
				"sents(s_count + 1) ≔ msg");
		act_sends_EO_3 = createAction(sends_EO, "act3",
				"channel ≔ channel ∪ {s_count + 1}");

		//   receives
		//   STATUS ordinary
		//   REFINES receives
		//   ANY idx
		//   WHERE
		//     grd1 : idx : channel
		//   WITH
		//     msg: msg = sents(idx)
		//   THEN
		//     act1: r_count := r_count + 1
		//     act2: channel := channel \ {idx}
		//     act3: receiveds(r_count + 1) := idx
		//   END
		receives_EO = createEvent(EO, "receives");
		createRefinesEventClause(receives_EO, "receives");
		idx_receives_EO = createParameter(receives_EO, "idx");
		grd_receives_EO_1 = createGuard(receives_EO, "grd1", "idx ∈ channel");
		msg_receives_EO = createWitness(receives_EO, "msg", "msg = sents(idx)");
		act_receives_EO_1 = createAction(receives_EO, "act1",
				"r_count ≔ r_count + 1");
		act_receives_EO_2 = createAction(receives_EO, "act2",
				"channel ≔ channel ∖ {idx}");
		act_receives_EO_3 = createAction(receives_EO, "act3",
				"receiveds(r_count + 1) ≔ idx");

		// Save EO
		EO.getRodinFile().save(monitor, false);
		
		// Create content for EOIO.
		// MACHINE EOIO
		// REFINES EO
		// SEES message_ctx, size_ctx
		// VARIABLES s_count, r_count, channel, sents, receiveds
		// INVARIANTS
		//   inv1: ran(receiveds) = 1..r_count
		//   thm2: channel = 1 + r_count..s_count
		// EVENTS
		//   ...
		// END
		createRefinesMachineClause(EOIO, "EO");
		createSeesContextClause(EOIO, "message_ctx");
		createSeesContextClause(EOIO, "size_ctx");
		s_count_EOIO = createVariable(EOIO, "s_count");
		r_count_EOIO = createVariable(EOIO, "r_count");
		channel_EOIO = createVariable(EOIO, "channel");
		sents_EOIO = createVariable(EOIO, "sents");
		receiveds_EOIO = createVariable(EOIO, "receiveds");
		inv_EOIO_1 = createInvariant(EOIO, "inv1",
				"ran(receiveds) = 1‥r_count", false);
		thm_EOIO_2 = createInvariant(EOIO, "thm2",
				"channel = 1 + r_count ‥ s_count", true);

		//   INITIALISATION
		//     extended
		//   STATUS ordinary
		//   END
		init_EOIO = createEvent(EOIO, IEvent.INITIALISATION);
		init_EOIO.setExtended(true, monitor);

		//   sends
		//     extended
		//   STATUS ordinary
		//   REFINES sends
		//   END
		sends_EOIO = createEvent(EOIO, "sends");
		sends_EOIO.setExtended(true, monitor);
		createRefinesEventClause(sends_EOIO, "sends");

		//   receives
		//     extended
		//   STATUS ordinary
		//   REFINES receives
		//   ANY i
		//   WHERE
		//     grd2: idx = r_count + 1
		//   THEN
		//     ...
		//   END
		receives_EOIO = createEvent(EOIO, "receives");
		receives_EOIO.setExtended(true, monitor);
		createRefinesEventClause(receives_EOIO, "receives");
		grd_receives_EOIO_2 = createGuard(receives_EOIO, "grd2",
				"idx = r_count + 1");

		// Save EOIO
		EOIO.getRodinFile().save(monitor, false);
	}


}
