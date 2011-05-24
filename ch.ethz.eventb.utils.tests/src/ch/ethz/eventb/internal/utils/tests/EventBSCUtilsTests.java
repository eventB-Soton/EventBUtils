package ch.ethz.eventb.internal.utils.tests;

import java.util.Collection;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eventb.core.IMachineRoot;
import org.junit.Before;
import org.junit.Test;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.utils.EventBSCUtils;

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
		workspace.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
	}

	/**
	 * Test method for
	 * {@link EventBSCUtils#getSCSeenCarrierSetIdentifierStrings(IMachineRoot)}.
	 */
	@Test
	public void testGetSeenCarrierSetIdentifierStrings() {
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
	 * Test method for {@link EventBUtils#getSeenAxioms(IMachineRoot, int)}.
	 */
//	@Test
//	public void testGetSeenAxioms() {
//		try {
//			// Test for channel machine.
//			Map<String, Predicate> seenAxioms = EventBSCUtils.getSeenAxioms(
//					channel, EventBSCUtils.AXIOMS);
//			assertUntypedPredicates("Seen axioms only for channel machine",
//					seenAxioms, "message_ctx/axm1:finite(MESSAGE)");
//			seenAxioms = EventBSCUtils.getSeenAxioms(channel,
//					EventBSCUtils.THEOREMS);
//			assertUntypedPredicates("Seen theorems only for channel machine",
//					seenAxioms, "message_ctx/thm2:card(MESSAGE)∈ℕ1");
//			seenAxioms = EventBSCUtils.getSeenAxioms(channel, EventBSCUtils.AXIOMS
//					| EventBSCUtils.THEOREMS);
//			assertUntypedPredicates(
//					"Seen axioms and theorems for channel machine", seenAxioms,
//					"message_ctx/axm1:finite(MESSAGE)",
//					"message_ctx/thm2:card(MESSAGE)∈ℕ1");
//
//			// Test for EO machine.
//			seenAxioms = EventBSCUtils.getSeenAxioms(EO, EventBSCUtils.AXIOMS);
//			assertUntypedPredicates("Seen axioms only for EO machine",
//					seenAxioms, "message_ctx/axm1:finite(MESSAGE)",
//					"size_ctx/axm1:max_size∈ℕ1");
//			seenAxioms = EventBSCUtils.getSeenAxioms(EO, EventBSCUtils.THEOREMS);
//			assertUntypedPredicates("Seen theorems only for EO machine",
//					seenAxioms, "message_ctx/thm2:card(MESSAGE)∈ℕ1");
//			seenAxioms = EventBSCUtils.getSeenAxioms(EO, EventBSCUtils.AXIOMS
//					| EventBSCUtils.THEOREMS);
//			assertUntypedPredicates("Seen axioms and theorems for EO machine",
//					seenAxioms, "message_ctx/axm1:finite(MESSAGE)",
//					"message_ctx/thm2:card(MESSAGE)∈ℕ1",
//					"size_ctx/axm1:max_size∈ℕ1");
//
//			// Test for EOIO machine.
//			seenAxioms = EventBSCUtils.getSeenAxioms(EOIO, EventBSCUtils.AXIOMS);
//			assertUntypedPredicates("Seen axioms only for EOIO machine",
//					seenAxioms, "message_ctx/axm1:finite(MESSAGE)",
//					"size_ctx/axm1:max_size∈ℕ1");
//			seenAxioms = EventBSCUtils.getSeenAxioms(EOIO, EventBSCUtils.THEOREMS);
//			assertUntypedPredicates("Seen theorems only for EOIO machine",
//					seenAxioms, "message_ctx/thm2:card(MESSAGE)∈ℕ1");
//			seenAxioms = EventBSCUtils.getSeenAxioms(EOIO, EventBSCUtils.AXIOMS
//					| EventBSCUtils.THEOREMS);
//			assertUntypedPredicates(
//					"Seen axioms and theorems for EOIO machine", seenAxioms,
//					"message_ctx/axm1:finite(MESSAGE)",
//					"message_ctx/thm2:card(MESSAGE)∈ℕ1",
//					"size_ctx/axm1:max_size∈ℕ1");
//
//		} catch (RodinDBException e) {
//			e.printStackTrace();
//			fail("There should be no exception");
//			return;
//		}
//	}


	/**
	 * Utility method for testing a collection of untyped predicates.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param axioms
	 *            a map of axioms (mapping from label to predicate) under test.
	 * @param expected
	 *            an array of expected pretty-print of axioms of the form
	 *            "label:predicateString".
	 */
//	private void assertUntypedPredicates(String message,
//			Map<String, Predicate> axioms, String... expected) {
//		Collection<String> actual = new ArrayList<String>(expected.length);
//		Set<Entry<String, Predicate>> entrySet = axioms.entrySet();
//		for (Entry<String, Predicate> axiom : entrySet) {
//			Predicate predicate = axiom.getValue();
//			assertFalse(message + ": The predicate should not be type-checked",
//					predicate.isTypeChecked());
//			String label = axiom.getKey();
//			actual.add(label + ":" + predicate.toString());
//
//		}
//		assertSameStrings(message, "axiom", actual, expected);
//	}

}
