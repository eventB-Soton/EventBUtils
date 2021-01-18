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

package ch.ethz.eventb.internal.utils.tests;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ISCMachineRoot;
import org.junit.Before;
import org.junit.Test;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.utils.EventBSCUtils;
import ch.ethz.eventb.utils.tests.AbstractEventBTests;
import ch.ethz.eventb.utils.tests.ChannelSetup;

/**
 * <p>
 * Tests for EventB SC Utilities.
 * </p>
 *
 * @author htson
 * @version 0.1.3
 * @see EventBSCUtils
 * @since 0.1.3
 */
public class EventBSCUtilsTests extends AbstractEventBTests {

	/**
	 * <ol>
	 * <li>Setup the <code>Channel</code> project.</li>
	 * <li>Build the workspace.</li>
	 * </ol>
	 * 
	 * @see AbstractEventBTests#setUp()
	 */
	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ChannelSetup.setup();
		
		// Fully build the project.
		workspace.build(IncrementalProjectBuilder.FULL_BUILD, nullMonitor);
	}

	/**
	 * Test method for
	 * {@link EventBSCUtils#getSCSeenAxioms(IMachineRoot, boolean)}
	 */
	@Test
	public void testGetSeenSCAxioms() {
		try {
			Map<String, String> expected;
			Map<String, String> scSeenAxms;

			IMachineRoot channelMchRoot = ChannelSetup.getChannelMachineRoot();
			scSeenAxms = EventBSCUtils.getSCSeenAxioms(channelMchRoot, false);
			expected = new HashMap<String, String>();
			expected.put("message_ctx/axm1", "finite(MESSAGE)");
			assertSameMap("Test get seen statically checked axioms channel",
					expected, scSeenAxms);

			scSeenAxms = EventBSCUtils.getSCSeenAxioms(channelMchRoot, true);
			expected = new HashMap<String, String>();
			expected.put("message_ctx/thm1", "card(MESSAGE) ∈ ℕ1");
			assertSameMap("Test get seen statically checked theorems channel",
					expected, scSeenAxms);

			IMachineRoot EOMchRoot = ChannelSetup.getEOIOMachineRoot();
			scSeenAxms = EventBSCUtils.getSCSeenAxioms(EOMchRoot, false);
			expected = new HashMap<String, String>();
			expected.put("message_ctx/axm1", "finite(MESSAGE)");
			expected.put("size_ctx/axm1", "max_size ∈ ℕ1");
			assertSameMap("Test get seen statically checked axioms EO",
					expected, scSeenAxms);

			scSeenAxms = EventBSCUtils.getSCSeenAxioms(EOMchRoot, true);
			expected = new HashMap<String, String>();
			expected.put("message_ctx/thm1", "card(MESSAGE) ∈ ℕ1");
			assertSameMap("Test get seen statically checked theorems EO",
					expected, scSeenAxms);

			IMachineRoot EOIOMchRoot = ChannelSetup.getEOIOMachineRoot();
			scSeenAxms = EventBSCUtils.getSCSeenAxioms(EOIOMchRoot, false);
			expected = new HashMap<String, String>();
			expected.put("message_ctx/axm1", "finite(MESSAGE)");
			expected.put("size_ctx/axm1", "max_size ∈ ℕ1");
			assertSameMap("Test get seen statically checked axioms EOIO",
					expected, scSeenAxms);

			scSeenAxms = EventBSCUtils.getSCSeenAxioms(EOIOMchRoot, true);
			expected = new HashMap<String, String>();
			expected.put("message_ctx/thm1", "card(MESSAGE) ∈ ℕ1");
			assertSameMap("Test get seen statically checked theorems EOIO",
					expected, scSeenAxms);

		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBSCUtils#getSCSeenCarrierSetIdentifierStrings(IMachineRoot)}.
	 */
	@Test
	public void testGetSCSeenCarrierSetIdentifierStrings() {
		try {
			IMachineRoot channelMchRoot = ChannelSetup.getChannelMachineRoot();
			Collection<String> seenSCSetIdentStrs = EventBSCUtils
					.getSCSeenCarrierSetIdentifierStrings(channelMchRoot);
			assertSameStrings("Machine channel: seen SC carrier sets",
					seenSCSetIdentStrs, "MESSAGE");
			
			IMachineRoot EOMchRoot = ChannelSetup.getEOMachineRoot();
			seenSCSetIdentStrs = EventBSCUtils
					.getSCSeenCarrierSetIdentifierStrings(EOMchRoot);
			assertSameStrings("Machine EO: seen SC carrier sets",
					seenSCSetIdentStrs, "MESSAGE");
			
			IMachineRoot EOIOMchRoot = ChannelSetup.getEOIOMachineRoot();
			seenSCSetIdentStrs = EventBSCUtils
					.getSCSeenCarrierSetIdentifierStrings(EOIOMchRoot);
			assertSameStrings("Machine EO: seen SC carrier sets",
					seenSCSetIdentStrs, "MESSAGE");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBSCUtils#getSCSeenCarrierSetIdentifierStrings(ISCMachineRoot)}
	 * .
	 */
	@Test
	public void testGetSCSeenCarrierSetIdentifierStringsSC() {
		try {
			IMachineRoot channelMchRoot = ChannelSetup.getChannelMachineRoot();
			Collection<String> seenSCSetIdentStrs = EventBSCUtils
					.getSCSeenCarrierSetIdentifierStrings(channelMchRoot
							.getSCMachineRoot());
			assertSameStrings("Machine channel: seen SC carrier sets",
					seenSCSetIdentStrs, "MESSAGE");
			
			IMachineRoot EOMchRoot = ChannelSetup.getEOMachineRoot();
			seenSCSetIdentStrs = EventBSCUtils
					.getSCSeenCarrierSetIdentifierStrings(EOMchRoot
							.getSCMachineRoot());
			assertSameStrings("Machine EO: seen SC carrier sets",
					seenSCSetIdentStrs, "MESSAGE");
			
			IMachineRoot EOIOMchRoot = ChannelSetup.getEOIOMachineRoot();
			seenSCSetIdentStrs = EventBSCUtils
					.getSCSeenCarrierSetIdentifierStrings(EOIOMchRoot
							.getSCMachineRoot());
			assertSameStrings("Machine EO: seen SC carrier sets",
					seenSCSetIdentStrs, "MESSAGE");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBSCUtils#getSCSeenConstantIdentifierStrings(IMachineRoot)}.
	 */
	@Test
	public void testGetSCSeenConstantIdentifierStrings() {
		try {
			IMachineRoot channelMchRoot = ChannelSetup.getChannelMachineRoot();
			Collection<String> seenSCCstIdentStrs = EventBSCUtils
					.getSCSeenConstantIdentifierStrings(channelMchRoot);
			assertSameStrings("Machine channel: seen SC constants",
					seenSCCstIdentStrs);
			
			IMachineRoot EOMchRoot = ChannelSetup.getEOMachineRoot();
			seenSCCstIdentStrs = EventBSCUtils
					.getSCSeenConstantIdentifierStrings(EOMchRoot);
			assertSameStrings("Machine EO: seen SC constants",
					seenSCCstIdentStrs, "max_size");
			
			IMachineRoot EOIOMchRoot = ChannelSetup.getEOIOMachineRoot();
			seenSCCstIdentStrs = EventBSCUtils
					.getSCSeenConstantIdentifierStrings(EOIOMchRoot);
			assertSameStrings("Machine EO: seen SC constants",
					seenSCCstIdentStrs, "max_size");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBSCUtils#getSCSeenConstantIdentifierStrings(ISCMachineRoot)}.
	 */
	@Test
	public void testGetSCSeenConstantIdentifierStringsSC() {
		try {
			IMachineRoot channelMchRoot = ChannelSetup.getChannelMachineRoot();
			Collection<String> seenSCCstIdentStrs = EventBSCUtils
					.getSCSeenConstantIdentifierStrings(channelMchRoot
							.getSCMachineRoot());
			assertSameStrings("Machine channel: seen SC constants",
					seenSCCstIdentStrs);
			
			IMachineRoot EOMchRoot = ChannelSetup.getEOMachineRoot();
			seenSCCstIdentStrs = EventBSCUtils
					.getSCSeenConstantIdentifierStrings(EOMchRoot
							.getSCMachineRoot());
			assertSameStrings("Machine EO: seen SC constants",
					seenSCCstIdentStrs, "max_size");
			
			IMachineRoot EOIOMchRoot = ChannelSetup.getEOIOMachineRoot();
			seenSCCstIdentStrs = EventBSCUtils
					.getSCSeenConstantIdentifierStrings(EOIOMchRoot
							.getSCMachineRoot());
			assertSameStrings("Machine EO: seen SC constants",
					seenSCCstIdentStrs, "max_size");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}
}
