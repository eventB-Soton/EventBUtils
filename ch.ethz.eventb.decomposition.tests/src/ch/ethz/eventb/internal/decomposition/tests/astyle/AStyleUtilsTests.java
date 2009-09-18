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

package ch.ethz.eventb.internal.decomposition.tests.astyle;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.eventb.core.IConvergenceElement.Convergence;
import org.junit.Test;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.astyle.AStyleUtils;
import ch.ethz.eventb.internal.decomposition.tests.AbstractDecompositionTests;
import ch.ethz.eventb.internal.decomposition.utils.Messages;
import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement.Nature;

/**
 * @author htson
 *         <p>
 *         Test class for {@link AStyleUtils}.
 *         </p>
 */
public class AStyleUtilsTests extends AbstractDecompositionTests {

	/**
	 * Test method
	 * {@link AStyleUtils#decomposeVariables(org.eventb.core.IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testCreateVariables() {

		try {
			AStyleUtils.decomposeVariables(mch2_1, subModel1,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create variables 1: There should be no exception");
			return;
		}

		assertVariables("Create variables 1", mch2_1, "z: "
				+ Messages.decomposition_private_comment, "v: "
				+ Messages.decomposition_shared_comment);

		try {
			AStyleUtils.decomposeVariables(mch3_1, subModel2,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create variables 2: There should be no exception");
			return;
		}

		assertVariables("Create variables 2", mch3_1, "y: "
				+ Messages.decomposition_private_comment, "v: "
				+ Messages.decomposition_shared_comment, "u: "
				+ Messages.decomposition_shared_comment);

		try {
			AStyleUtils.decomposeVariables(mch4_1, subModel3,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create variables 3: There should be no exception");
			return;
		}

		assertVariables("Create variables 3", mch4_1, "v: "
				+ Messages.decomposition_shared_comment, "u: "
				+ Messages.decomposition_shared_comment, "p: "
				+ Messages.decomposition_private_comment);
	}

	/**
	 * Utility method to test
	 * {@link AStyleUtils#decomposeVariables(IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 * 
	 * @param message
	 *            a message.
	 * @param mch
	 *            a machine
	 * @param expected
	 *            expected set of variables (in {@link String}).
	 */
	private void assertVariables(String message, IMachineRoot mch,
			String... expected) {
		try {
			IVariable[] vars = mch.getVariables();
			assertEquals(message + ": Incorrect number of variables",
					expected.length, vars.length);

			for (IVariable var : vars) {
				boolean found = false;
				INatureElement elt = (INatureElement) var
						.getAdapter(INatureElement.class);
				for (String exp : expected) {
					if (exp.equals(var.getIdentifierString() + ": "
							+ var.getComment())) {
						found = true;
						if ((var.getComment()
								.equals(Messages.decomposition_shared_comment))
								&& (elt.getNature() != Nature.SHARED))
							fail(message
									+ ": The shared nature should be set for variable "
									+ var.getIdentifierString());
						else if ((var.getComment()
								.equals(Messages.decomposition_private_comment))
								&& (elt.getNature() != Nature.PRIVATE))
							fail(message
									+ ": The private nature should be set for variable "
									+ var.getIdentifierString());
						break;
					}
				}
				if (!found) {
					fail(message + ": Do not expect variable "
							+ var.getIdentifierString());
				}
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail(message + ": There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link AStyleUtils#decomposeEvents(IMachineRoot, ISubModel, IProgressMonitor)}
	 * .
	 */
	@Test
	public void testDecomposeEvents() {
		try {
			AStyleUtils.decomposeEvents(mch2_1, subModel1,
					new NullProgressMonitor());

			// Test number of events.
			IEvent[] events = mch2_1.getEvents();
			assertEquals("Create events 1: Incorrect number of events", 6,
					events.length);

			// Test the initialization.
			IEvent evt = getEventWithLabel(mch2_1, IEvent.INITIALISATION);
			assertNotNull("Create events 1: Cannot find event INITIALISATION",
					evt);
			assertEvent("Create events 1", evt,
					Messages.decomposition_external_comment);
			testEventSignature("Create events 1", evt, "INITIALISATION", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 1", evt);
			testEventGuards("Create events 1", evt);
			testEventWitnesses("Create events 1", evt);
			testEventActions("Create events 1", evt, "act_init1_3_2: v ≔ 0",
					"act_init1_3_3: z ≔ 0");

			// Test evt1_3_1 (internal).
			evt = getEventWithLabel(mch2_1, "evt1_3_1");
			assertNotNull("Create events 2: Cannot find event evt1_3_1", evt);
			assertEvent("Create events 1", evt,
					Messages.decomposition_internal_comment);
			testEventSignature("Create events 2", evt, "evt1_3_1", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 2", evt);
			testEventGuards("Create events 2", evt, "grd1_3_1_1: z = 0: false");
			testEventWitnesses("Create events 2", evt);
			testEventActions("Create events 2", evt, "act1_3_1_1: v ≔ 2");

			// Test evt1_3_5 (internal).
			evt = getEventWithLabel(mch2_1, "evt1_3_5");
			assertNotNull("Create events 3: Cannot find event evt1_3_5", evt);
			assertEvent("Create events 1", evt,
					Messages.decomposition_internal_comment);
			testEventSignature("Create events 3", evt, "evt1_3_5", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 3", evt);
			testEventGuards("Create events 3", evt, "grd1_3_5_1: z ≥ 2: false");
			testEventWitnesses("Create events 3", evt);
			testEventActions("Create events 3", evt, "act1_3_5_1: z ≔ z − 1");

			// Test evt1_3_2 (external).
			evt = getEventWithLabel(mch2_1, "evt1_3_2");
			assertNotNull("Create events 4: Cannot find event evt1_3_2", evt);
			assertEvent("Create events 1", evt,
					Messages.decomposition_external_comment);
			testEventSignature("Create events 4", evt, "evt1_3_2", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 4", evt, "y", "u");
			testEventGuards("Create events 4", evt,
					Messages.decomposition_typing + "_y: y ∈ ℤ: true",
					Messages.decomposition_typing + "_u: u ∈ U: true",
					"grd1_3_2_1: y ≠ 0: false", "grd1_3_2_2: u = f: false");
			testEventWitnesses("Create events 2", evt);
			testEventActions("Create events 2", evt, "act1_3_2_3: v ≔ v + 1");

			// Test evt1_3_3 (external).
			evt = getEventWithLabel(mch2_1, "evt1_3_3");
			assertNotNull("Create events 5: Cannot find event evt1_3_3", evt);
			assertEvent("Create events 1", evt,
					Messages.decomposition_external_comment);
			testEventSignature("Create events 5", evt, "evt1_3_3", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 5", evt, "t", "y");
			testEventGuards("Create events 5", evt,
					Messages.decomposition_typing + "_y: y ∈ ℤ: true",
					"grd1_3_3_1: t ≠ a: false", "grd1_3_3_2: y ≥ 5: false",
					"grd1_3_3_3: y > v: false");
			testEventWitnesses("Create events 2", evt);
			testEventActions("Create events 5", evt, "act1_3_3_2: v ≔ v + 1");

			// Test evt1_3_4 (external).
			evt = getEventWithLabel(mch2_1, "evt1_3_4");
			assertNotNull("Create events 6: Cannot find event evt1_3_4", evt);
			assertEvent("Create events 1", evt,
					Messages.decomposition_external_comment);
			testEventSignature("Create events 6", evt, "evt1_3_4", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 6", evt, "r", "p");
			testEventGuards("Create events 6", evt,
					Messages.decomposition_typing + "_p: p ∈ ℙ(ℤ × V): true",
					"grd1_2_4_1: v ≥ 3: false", "grd1_3_4_1: r ∈ ℕ: false",
					"grd1_3_4_2: p(r) = g: false");
			testEventWitnesses("Create events 2", evt);
			testEventActions("Create events 6", evt, "act1_2_4_1: v ≔ v − 1");

		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create events: There should be no exception");
			return;
		}

	}

	/**
	 * Utility method to test
	 * {@link AStyleUtils#decomposeEvents(IMachineRoot, ISubModel, IProgressMonitor)}
	 * .
	 * 
	 * @param message
	 *            a message.
	 * @param mch
	 *            a machine
	 * @param expected
	 *            expected attribute (in {@link String}).
	 */
	private void assertEvent(String message, IEvent evt, String expected) {
		IExternalElement elt = (IExternalElement) evt
				.getAdapter(IExternalElement.class);
		try {
			if (((expected.equals(Messages.decomposition_external_comment)) && !elt
					.isExternal())
					|| (expected
							.equals(Messages.decomposition_internal_comment) && elt
							.isExternal()))
				fail(message + ": Incorrect attribute for event "
						+ evt.getLabel());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for {@link AStyleUtils#decomposeAction(IAction, Set)}.
	 */
	@Test
	public void testDecomposeAction() {
		try {
			Set<String> vars;
			String assignmentStr;
			vars = new HashSet<String>();
			vars.add("z");
			vars.add("v");

			assignmentStr = AStyleUtils.decomposeAction(act_init1_3_1,
					vars);
			assertNull("Incorrect decomposed action 1", assignmentStr);

			assignmentStr = AStyleUtils.decomposeAction(act_init1_3_2,
					vars);
			assertEquals("Incorrect decomposed action 2", assignmentStr,
					"v ≔ 0");

			assignmentStr = AStyleUtils.decomposeAction(act_init1_3_3,
					vars);
			assertEquals("Incorrect decomposed action 3", assignmentStr,
					"z ≔ 0");

			assignmentStr = AStyleUtils
					.decomposeAction(act1_3_1_1, vars);
			assertEquals("Incorrect decomposed action 4", assignmentStr,
					"v ≔ 2");

			assignmentStr = AStyleUtils
					.decomposeAction(act1_3_2_1, vars);
			assertNull("Incorrect decomposed action 5", assignmentStr);

			assignmentStr = AStyleUtils
					.decomposeAction(act1_3_2_2, vars);
			assertNull("Incorrect decomposed action 6", assignmentStr);

			assignmentStr = AStyleUtils
					.decomposeAction(act1_3_2_3, vars);
			assertEquals("Incorrect decomposed action 7", assignmentStr,
					"v ≔ v + 1");

			assignmentStr = AStyleUtils
					.decomposeAction(act1_3_3_1, vars);
			assertNull("Incorrect decomposed action 8", assignmentStr);

			assignmentStr = AStyleUtils
					.decomposeAction(act1_3_3_2, vars);
			assertEquals("Incorrect decomposed action 9", assignmentStr,
					"v ≔ v + 1");

			assignmentStr = AStyleUtils
					.decomposeAction(act1_3_4_1, vars);
			assertNull("Incorrect decomposed action 10", assignmentStr);

			assignmentStr = AStyleUtils
					.decomposeAction(act1_3_5_1, vars);
			assertEquals("Incorrect decomposed action 11", assignmentStr,
					"z ≔ z − 1");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}
	
	/**
	 * Test method for {@link IModelDecomposition#getSharedVariables()}.
	 */
	@Test
	public void testGetSharedVariables() {
		Set<String> vars;
		try {
			vars = AStyleUtils.getSharedVariables(modelDecomp3);
			assertEqualsVariables("", vars, "u", "v");
			vars = AStyleUtils.getSharedVariables(modelDecomp1);
			assertEqualsVariables("", vars);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method to compare two sets of variables (in {@link String}).
	 * 
	 * @param message
	 *            a message.
	 * @param actual
	 *            actual set of variables.
	 * @param expected
	 *            expected array of variables.
	 */
	private void assertEqualsVariables(String message, Set<String> actual,
			String... expected) {
		assertEquals(message + ": Incorrect number of expected variables",
				expected.length, actual.size());
		for (String exp : expected) {
			assertTrue(message + ": Expected variable " + exp + " not found",
					actual.contains(exp));
		}
	}
}
