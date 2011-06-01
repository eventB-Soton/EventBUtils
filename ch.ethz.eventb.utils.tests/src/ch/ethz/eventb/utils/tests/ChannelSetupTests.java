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
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IGuard;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IVariable;
import org.eventb.core.IWitness;
import org.junit.Before;


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
	 * Some predefined contexts:
	 * - message_ctx, size_ctx in channelPrj.
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
	 * Some predefined machines.
	 * - channel, EO, EOIO in project basedPrj.
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
		channelPrj = createEventBProject(channelPrjName); //$NON-NLS-1$
		
		// Create some contexts inside the project
		message_ctx = createContext(channelPrj, messageCtxName);
		size_ctx = createContext(channelPrj, sizeCtxName);
		
		// Create content of message_ctx.
		// CONTEXT message_ctx
		// SETS MESSAGE
		// AXIOMS
		//   axm1: finite(MESSAGE)
		//   thm1: card(MESSAGE) : NAT1
		// END
		MESSAGE = createCarrierSet(message_ctx, "MESSAGE");
		message_ctx_axm_1 = createAxiom(message_ctx, "axm1",
				"finite(MESSAGE)", false);
		message_ctx_thm_1 = createAxiom(message_ctx, "thm1",
				"card(MESSAGE) ∈ ℕ1", true);
		message_ctx.getRodinFile().save(monitor, false);
		
		// Create content for size_ctx
		// CONTEXT size_ctx
		// CONSTANTS max_size
		// AXIOMS
		//   axm1: max_size : NAT1
		// END
		max_size = createConstant(size_ctx, "max_size");
		size_ctx_axm_1 = createAxiom(size_ctx, "axm1", "max_size ∈ ℕ1",
				false);
		size_ctx.getRodinFile().save(monitor, false);
		
		// Create some machines inside the projects.
		channelMchRoot = createMachine(channelPrj, channelMchName);
		EOMchRoot = createMachine(channelPrj, EOMchName);
		EOIOMchRoot = createMachine(channelPrj, EOIOMchName);
		
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
		createSeesContextClause(channelMchRoot, messageCtxName);
		channel_s_count = createVariable(channelMchRoot, "s_count");
		channel_r_count = createVariable(channelMchRoot, "r_count");
		channel_inv_1 = createInvariant(channelMchRoot, "inv1",
				"s_count ∈ ℕ", false);
		channel_inv_2 = createInvariant(channelMchRoot, "inv2",
				"r_count ∈ ℕ", false);

		//   INITIALISATION
		//   STATUS ordinary
		//   BEGIN
		//     act1: s_count := 0
		//     act2: r_count := 0
		//   END
		channel_init = createEvent(channelMchRoot, IEvent.INITIALISATION);
		channel_init_act_1 = createAction(channel_init, "act1",
				"s_count ≔ 0");
		channel_init_act_2 = createAction(channel_init, "act2",
				"r_count ≔ 0");
		
		//   sends
		//   STATUS ordinary
		//   ANY msg
		//   WHERE
		//     grd1 : msg :  MESSAGE
		//   THEN
		//     act1: s_count := s_count + 1
		//   END
		channel_sends = createEvent(channelMchRoot, "sends");
		channel_sends_msg = createParameter(channel_sends, "msg");
		channel_sends_grd_1 = createGuard(channel_sends, "grd1",
				"msg ∈ MESSAGE", false);
		channel_sends_act_1 = createAction(channel_sends, "act1",
				"s_count ≔ s_count + 1");

		//   receives
		//   STATUS ordinary
		//   ANY msg
		//   WHERE
		//     grd1 : msg : MESSAGE
		//   THEN
		//     act1: r_count := r_count + 1
		//   END
		channel_receives = createEvent(channelMchRoot, "receives");
		channel_receives_msg = createParameter(channel_receives, "msg");
		channel_receives_grd_1 = createGuard(channel_receives, "grd1",
				"msg ∈ MESSAGE", false);
		channel_receives_act_1 = createAction(channel_receives, "act1",
				"r_count ≔ r_count + 1");

		// Save channel
		channelMchRoot.getRodinFile().save(monitor, false);

		// Create content for EO.
		// MACHINE EO
		// REFINES channel
		// SEES message_ctx, size_ctx
		// VARIABLES s_count, r_count, sents, receiveds, channel
		// INVARIANTS
		//   inv1: sents : 1..s_count --> MESSAGE
		//   inv2: receiveds : 1..r_count >-> 1..s_count
		//   inv3: ran(receiveds) \/ channel = 1..s_count
		//   thm1: channel <: 1..s_count
		//   inv4: ran(receiveds) /\ channel = {}
		//   inv5: r_count + card(channel) = s_count
		//   thm2: r_count <= s_count
		//   inv6: card(channel) <= max_size
		//   thm3: s_count <= r_count + max_size
		// EVENTS
		//   ...
		// END
		createRefinesMachineClause(EOMchRoot, channelMchName);
		createSeesContextClause(EOMchRoot, messageCtxName);
		createSeesContextClause(EOMchRoot, sizeCtxName);
		EO_s_count = createVariable(EOMchRoot, "s_count");
		EO_r_count = createVariable(EOMchRoot, "r_count");
		EO_sents = createVariable(EOMchRoot, "sents");
		EO_receiveds = createVariable(EOMchRoot, "receiveds");
		EO_channel = createVariable(EOMchRoot, "channel");
		EO_inv_1 = createInvariant(EOMchRoot, "inv1",
				"sents ∈ 1‥s_count → MESSAGE", false);
		EO_inv_2 = createInvariant(EOMchRoot, "inv2",
				"receiveds ∈ 1‥r_count ↣ 1‥s_count", false);
		EO_inv_3 = createInvariant(EOMchRoot, "inv3",
				"ran(receiveds) ∪ channel = 1‥s_count", false);
		EO_thm_1 = createInvariant(EOMchRoot, "thm1",
				"channel ⊆ 1‥s_count", true);
		EO_inv_4 = createInvariant(EOMchRoot, "inv4",
				"ran(receiveds) ∩ channel = ∅", false);
		EO_inv_5 = createInvariant(EOMchRoot, "inv5",
				"r_count + card(channel) = s_count", false);
		EO_thm_2 = createInvariant(EOMchRoot, "thm2",
				"r_count ≤ s_count", true);
		EO_inv_6 = createInvariant(EOMchRoot, "inv6",
				"card(channel) ≤ max_size", false);
		EO_thm_3 = createInvariant(EOMchRoot, "thm3",
				"s_count ≤ r_count + max_size", true);
	
		//   INITIALISATION
		//     extended
		//   STATUS ordinary
		//   BEGIN
		//     act3: sents := {}
		//     act4: receiveds := {}
		//     act5: channel := {}
		//   END
		EO_init = createEvent(EOMchRoot, IEvent.INITIALISATION);
		EO_init.setExtended(true, monitor);
		EO_init_act_3 = createAction(EO_init, "act3", "sents ≔ ∅");
		EO_init_act_4 = createAction(EO_init, "act4", "receiveds ≔ ∅");
		EO_init_act_5 = createAction(EO_init, "act5", "channel ≔ ∅");

		//   sends
		//     extended
		//   STATUS ordinary
		//   REFINES sends
		//   WHEN
		//     grd2 : card(channel) /= max_size
		//     thm1 : {s_count + 1 |-> msg} : 1 .. s_count + 1 +-> MESSAGE
		//   THEN
		//     act2: sents(s_count + 1) := msg
		//     act3: channel := channel \/ {s_count + 1}
		//   END
		EO_sends = createEvent(EOMchRoot, "sends");
		EO_sends.setExtended(true, monitor);
		createRefinesEventClause(EO_sends, "sends");
		EO_sends_grd_2 = createGuard(EO_sends, "grd2",
				"card(channel) ≠ max_size", false);
		EO_sends_thm_1 = createGuard(EO_sends, "thm1",
				"{s_count + 1 ↦ msg} ∈ 1 ‥ s_count + 1 ⇸ MESSAGE", true);
		EO_sends_act_2 = createAction(EO_sends, "act2",
				"sents(s_count + 1) ≔ msg");
		EO_sends_act_3 = createAction(EO_sends, "act3",
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
		EO_receives = createEvent(EOMchRoot, "receives");
		createRefinesEventClause(EO_receives, "receives");
		EO_receives_idx = createParameter(EO_receives, "idx");
		EO_receives_grd_1 = createGuard(EO_receives, "grd1", "idx ∈ channel", false);
		EO_receives_msg = createWitness(EO_receives, "msg", "msg = sents(idx)");
		EO_receives_act_1 = createAction(EO_receives, "act1",
				"r_count ≔ r_count + 1");
		EO_receives_act_2 = createAction(EO_receives, "act2",
				"channel ≔ channel ∖ {idx}");
		EO_receives_act_3 = createAction(EO_receives, "act3",
				"receiveds(r_count + 1) ≔ idx");

		// Save EO
		EOMchRoot.getRodinFile().save(monitor, false);
		
		// Create content for EOIO.
		// MACHINE EOIO
		// REFINES EO
		// SEES message_ctx, size_ctx
		// VARIABLES s_count, r_count, sents, receiveds, channel
		// INVARIANTS
		//   inv1: ran(receiveds) = 1..r_count
		//   thm1: r_count : NAT
		//   thm2: ran(receiveds) \/ channel = 1..s_count
		//   thm3: ran(receiveds) /\ channel = {}
		//   thm4: ran(receiveds) = 1 .. r_count
		//   thm5: channel = r_count + 1 .. s_count
		// EVENTS
		//   ...
		// END
		createRefinesMachineClause(EOIOMchRoot, EOMchName);
		createSeesContextClause(EOIOMchRoot, messageCtxName);
		createSeesContextClause(EOIOMchRoot, sizeCtxName);
		EOIO_s_count = createVariable(EOIOMchRoot, "s_count");
		EOIO_r_count = createVariable(EOIOMchRoot, "r_count");
		EOIO_sents = createVariable(EOIOMchRoot, "sents");
		EOIO_receiveds = createVariable(EOIOMchRoot, "receiveds");
		EOIO_channel = createVariable(EOIOMchRoot, "channel");
		EOIO_inv_1 = createInvariant(EOIOMchRoot, "inv1",
				"ran(receiveds) = 1‥r_count", false);
		EOIO_thm_1 = createInvariant(EOIOMchRoot, "thm1", "r_count ∈ ℕ", true);
		EOIO_thm_2 = createInvariant(EOIOMchRoot, "thm2",
				"ran(receiveds) ∪ channel = 1‥s_count", true);
		EOIO_thm_3 = createInvariant(EOIOMchRoot, "thm3",
				"ran(receiveds) ∩ channel = ∅", true);
		EOIO_thm_4 = createInvariant(EOIOMchRoot, "thm4",
				"ran(receiveds) = 1‥r_count", true);
		EOIO_thm_5 = createInvariant(EOIOMchRoot, "thm5",
				"channel = r_count + 1 ‥ s_count", true);

		//   INITIALISATION
		//     extended
		//   STATUS ordinary
		//   END
		EOIO_init = createEvent(EOIOMchRoot, IEvent.INITIALISATION);
		EOIO_init.setExtended(true, monitor);

		//   sends
		//     extended
		//   STATUS ordinary
		//   REFINES sends
		//   END
		EOIO_sends = createEvent(EOIOMchRoot, "sends");
		EOIO_sends.setExtended(true, monitor);
		createRefinesEventClause(EOIO_sends, "sends");

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
		EOIO_receives = createEvent(EOIOMchRoot, "receives");
		EOIO_receives.setExtended(true, monitor);
		createRefinesEventClause(EOIO_receives, "receives");
		EOIO_receives_grd_2 = createGuard(EOIO_receives, "grd2",
				"idx = r_count + 1", false);

		// Save EOIO
		EOIOMchRoot.getRodinFile().save(monitor, false);
	}


}
