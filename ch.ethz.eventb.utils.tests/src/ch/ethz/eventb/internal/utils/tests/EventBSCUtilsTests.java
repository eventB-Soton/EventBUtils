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
import ch.ethz.eventb.utils.tests.ChannelSetupTests;

public class EventBSCUtilsTests extends ChannelSetupTests {

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.eventb.internal.inst.tests.ChannelSetupTests#setUp()
	 */
	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();

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
			Collection<String> seenSCSetIdentStrs = EventBSCUtils
					.getSCSeenCarrierSetIdentifierStrings(channelMchRoot);
			assertSameStrings("Machine channel: seen SC carrier sets",
					seenSCSetIdentStrs, "MESSAGE");
			seenSCSetIdentStrs = EventBSCUtils
					.getSCSeenCarrierSetIdentifierStrings(EOMchRoot);
			assertSameStrings("Machine EO: seen SC carrier sets",
					seenSCSetIdentStrs, "MESSAGE");
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
			Collection<String> seenSCSetIdentStrs = EventBSCUtils
					.getSCSeenCarrierSetIdentifierStrings(channelMchRoot
							.getSCMachineRoot());
			assertSameStrings("Machine channel: seen SC carrier sets",
					seenSCSetIdentStrs, "MESSAGE");
			seenSCSetIdentStrs = EventBSCUtils
					.getSCSeenCarrierSetIdentifierStrings(EOMchRoot
							.getSCMachineRoot());
			assertSameStrings("Machine EO: seen SC carrier sets",
					seenSCSetIdentStrs, "MESSAGE");
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
			Collection<String> seenSCCstIdentStrs = EventBSCUtils
					.getSCSeenConstantIdentifierStrings(channelMchRoot);
			assertSameStrings("Machine channel: seen SC constants",
					seenSCCstIdentStrs);
			seenSCCstIdentStrs = EventBSCUtils
					.getSCSeenConstantIdentifierStrings(EOMchRoot);
			assertSameStrings("Machine EO: seen SC constants",
					seenSCCstIdentStrs, "max_size");
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
			Collection<String> seenSCCstIdentStrs = EventBSCUtils
					.getSCSeenConstantIdentifierStrings(channelMchRoot
							.getSCMachineRoot());
			assertSameStrings("Machine channel: seen SC constants",
					seenSCCstIdentStrs);
			seenSCCstIdentStrs = EventBSCUtils
					.getSCSeenConstantIdentifierStrings(EOMchRoot
							.getSCMachineRoot());
			assertSameStrings("Machine EO: seen SC constants",
					seenSCCstIdentStrs, "max_size");
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
