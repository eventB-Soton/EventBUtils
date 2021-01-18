/*******************************************************************************
 * Copyright (c) 2010,2020 ETH Zurich.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/

package ch.ethz.eventb.utils.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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

import ch.ethz.eventb.utils.EventBUtils;

/**
 * <p>
 * This utility class is for creating a test "Channel" project.
 * </p>
 *
 * @author htson
 * @version 0.1.0
 * @see EventBUtils
 * @since 0.2.1
 */
public class ChannelSetup {

	/**
	 * Some predefined projects.
	 */
	private final static String channelPrjName = "Channel";
	private static IEventBProject channelPrj;

	/**
	 * Some predefined contexts: - message_ctx, size_ctx in channelPrj.
	 */
	private final static String messageCtxName = "message_ctx";
	private static IContextRoot messageCtxRoot;

	private final static String sizeCtxName = "size_ctx";
	private static IContextRoot sizeCtxRoot;

	/**
	 * Some carrier sets.
	 */
	private static ICarrierSet MESSAGE;

	/**
	 * Some constants.
	 */
	private static IConstant max_size;

	/**
	 * Some axioms and theorems.
	 */
	private static IAxiom message_ctx_axm_1;

	private static IAxiom message_ctx_thm_1;

	private static IAxiom size_ctx_axm_1;

	/**
	 * Some predefined machines. - channel, EO, EOIO in project basedPrj.
	 */
	private static String channelMchName = "channel";
	private static IMachineRoot channelMchRoot;

	private static String EOMchName = "EO";
	private static IMachineRoot EOMchRoot;

	private static String EOIOMchName = "EOIO";
	private static IMachineRoot EOIOMchRoot;

	/**
	 * Some variables.
	 */
	private static IVariable channel_s_count;

	private static IVariable channel_r_count;

	private static IVariable EO_s_count;

	private static IVariable EO_r_count;

	private static IVariable EO_channel;

	private static IVariable EO_sents;

	private static IVariable EO_receiveds;

	private static IVariable EOIO_s_count;

	private static IVariable EOIO_r_count;

	private static IVariable EOIO_channel;

	private static IVariable EOIO_sents;

	private static IVariable EOIO_receiveds;

	/**
	 * Some invariants within machines.
	 */
	private static IInvariant channel_inv_1;

	private static IInvariant channel_inv_2;

	private static IInvariant EO_inv_1;

	private static IInvariant EO_inv_2;

	private static IInvariant EO_inv_3;

	private static IInvariant EO_thm_1;

	private static IInvariant EO_inv_4;

	private static IInvariant EO_inv_5;

	private static IInvariant EO_thm_2;

	private static IInvariant EO_inv_6;

	private static IInvariant EO_thm_3;

	private static IInvariant EOIO_inv_1;

	private static IInvariant EOIO_thm_1;

	private static IInvariant EOIO_thm_2;

	private static IInvariant EOIO_thm_3;

	private static IInvariant EOIO_thm_4;

	private static IInvariant EOIO_thm_5;

	/**
	 * Some events within machines.
	 */
	private static IEvent channel_init;

	private static IEvent channel_sends;

	private static IEvent channel_receives;

	private static IEvent EO_init;

	private static IEvent EO_sends;

	private static IEvent EO_receives;

	private static IEvent EOIO_init;

	private static IEvent EOIO_sends;

	private static IEvent EOIO_receives;

	/**
	 * Some parameters of the events
	 */
	private static IParameter channel_sends_msg;

	private static IParameter channel_receives_msg;

	private static IParameter EO_receives_idx;

	/**
	 * Some guards within events
	 */
	private static IGuard channel_sends_grd_1;

	private static IGuard channel_receives_grd_1;

	private static IGuard EO_sends_grd_2;

	private static IGuard EO_sends_thm_1;

	private static IGuard EO_receives_grd_1;

	private static IGuard EOIO_receives_grd_2;

	/**
	 * Some witnesses within events
	 */
	private static IWitness EO_receives_msg;

	/**
	 * Some actions within events
	 */
	private static IAction channel_init_act_1;

	private static IAction channel_init_act_2;

	private static IAction channel_sends_act_1;

	private static IAction channel_receives_act_1;

	private static IAction EO_init_act_3;

	private static IAction EO_init_act_4;

	private static IAction EO_init_act_5;

	private static IAction EO_sends_act_2;

	private static IAction EO_sends_act_3;

	private static IAction EO_receives_act_1;

	private static IAction EO_receives_act_2;

	private static IAction EO_receives_act_3;

	/**
	 * Utility method to create the "Channel" project.
	 * 
	 * @throws CoreException
	 *             if some unexpected problems occur.
	 */
	public static void setup() throws CoreException {

		IProgressMonitor nullMonitor = new NullProgressMonitor();

		// Create the project
		channelPrj = EventBUtils.createEventBProject(channelPrjName,
				nullMonitor);

		// Create some contexts inside the project
		messageCtxRoot = EventBUtils.createContext(channelPrj, messageCtxName,
				nullMonitor);
		sizeCtxRoot = EventBUtils.createContext(channelPrj, sizeCtxName,
				nullMonitor);

		// Create content of message_ctx.
		// CONTEXT message_ctx
		// SETS MESSAGE
		// AXIOMS
		// axm1: finite(MESSAGE)
		// thm1: card(MESSAGE) : NAT1
		// END
		MESSAGE = EventBUtils.createCarrierSet(messageCtxRoot, "MESSAGE", null,
				nullMonitor);
		message_ctx_axm_1 = EventBUtils.createAxiom(messageCtxRoot, "axm1",
				"finite(MESSAGE)", false, null, nullMonitor);
		message_ctx_thm_1 = EventBUtils.createAxiom(messageCtxRoot, "thm1",
				"card(MESSAGE) ∈ ℕ1", true, null, nullMonitor);
		messageCtxRoot.getRodinFile().save(nullMonitor, false);

		// Create content for size_ctx
		// CONTEXT size_ctx
		// CONSTANTS max_size
		// AXIOMS
		// axm1: max_size : NAT1
		// END
		max_size = EventBUtils.createConstant(sizeCtxRoot, "max_size", null,
				nullMonitor);
		size_ctx_axm_1 = EventBUtils.createAxiom(sizeCtxRoot, "axm1",
				"max_size ∈ ℕ1", false, null, nullMonitor);
		sizeCtxRoot.getRodinFile().save(nullMonitor, false);

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
	 * Returns the <code>Channel</code> project.
	 * 
	 * @return the <code>Channel</code> project.
	 */
	public static IEventBProject getChannelProject() {
		return channelPrj;
	}

	/**
	 * Returns the <code>message_ctx</code> context root.
	 * 
	 * @return the <code>message_ctx</code> context root.
	 */
	public static IContextRoot getMessageContextRoot() {
		return messageCtxRoot;
	}

	/**
	 * Returns the <code>size_ctx</code> context root.
	 * 
	 * @return the <code>size_ctx</code> context root.
	 */
	public static IContextRoot getSizeContextRoot() {
		return sizeCtxRoot;
	}

	/**
	 * Returns the <code>Channel</code> machine root.
	 * 
	 * @return the <code>Channel</code> machine root.
	 */
	public static IMachineRoot getChannelMachineRoot() {
		return channelMchRoot;
	}

	/**
	 * Returns the <code>EO</code> machine root.
	 * 
	 * @return the <code>EO</code> machine root.
	 */
	public static IMachineRoot getEOMachineRoot() {
		return EOMchRoot;
	}

	/**
	 * Returns the <code>EOIO</code> machine root.
	 * 
	 * @return the <code>EOIO</code> machine root.
	 */
	public static IMachineRoot getEOIOMachineRoot() {
		return EOIOMchRoot;
	}

	/**
	 * Returns the <code>Channel</code>'s <code>sends</code> event.
	 * 
	 * @return the <code>Channel</code>'s <code>sends</code> event.
	 */
	public static IEvent getChannelSendsEvent() {
		return channel_sends;
	}

	/**
	 * Returns the <code>EO</code>'s <code>receives</code> event.
	 * 
	 * @return the <code>EO</code>'s <code>receives</code> event.
	 */
	public static IEvent getEOReceivesEvent() {
		return EO_receives;
	}

}
