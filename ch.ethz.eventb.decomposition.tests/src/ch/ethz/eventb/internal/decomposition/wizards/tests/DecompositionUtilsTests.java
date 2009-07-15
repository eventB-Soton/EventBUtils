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

package ch.ethz.eventb.internal.decomposition.wizards.tests;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IEvent;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.eventb.core.IConvergenceElement.Convergence;
import org.junit.Test;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.wizards.DecompositionUtils;

/**
 * @author htson
 *         <p>
 *         Test class for {@link DecompositionUtils}.
 *         </p>
 */
public class DecompositionUtilsTests extends AbstractDecompositionTests {

	/**
	 * Test method
	 * {@link DecompositionUtils#createVariables(org.eventb.core.IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testCreateVariables() {
		try {
			DecompositionUtils.createVariables(mch2_1, elemDist1,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create variables 1: There should be no exception");
			return;
		}
		
		assertVariables("Create variables 1", mch2_1, "z: Private variable",
				"v: Shared variable, DO NOT REFINE");

		try {
			DecompositionUtils.createVariables(mch3_1, elemDist2,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create variables 2: There should be no exception");
			return;
		}
		
		assertVariables("Create variables 2", mch3_1, "y: Private variable",
				"v: Shared variable, DO NOT REFINE",
				"u: Shared variable, DO NOT REFINE");

		try {
			DecompositionUtils.createVariables(mch4_1, elemDist3,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create variables 3: There should be no exception");
			return;
		}
		
		assertVariables("Create variables 3", mch4_1,
				"v: Shared variable, DO NOT REFINE",
				"u: Shared variable, DO NOT REFINE", "p: Private variable");
	}

	/**
	 * Utility method for testing
	 * {@link DecompositionUtils#createVariables(IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}.
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
				for (String exp : expected) {
					if (exp.equals(var.getIdentifierString() + ": "
							+ var.getComment())) {
						found = true;
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
	 * Test method for {@link DecompositionUtils#createInvariants(IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testCreateInvariants() {
		try {
			DecompositionUtils.createInvariants(mch2_1, elemDist1,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create invariants 1: There should be no exception");
			return;
		}
		
		testInvariants("Create invariants 1", mch2_1, "typing_z: z ∈ ℤ: true",
				"typing_v: v ∈ ℤ: true", "inv1_2_2: v ∈ ℕ: false",
				"thm1_2_3: v ≥ 0: true");

		try {
			DecompositionUtils.createInvariants(mch3_1, elemDist2,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create invariants 2: There should be no exception");
			return;
		}
		
		testInvariants("Create invariants 2", mch3_1,
				"typing_u: u ∈ U: true", "typing_y: y ∈ ℤ: true",
				"typing_v: v ∈ ℤ: true", "inv1_1_2: y ∈ ℕ: false",
				"thm1_1_3: y ≥ 0: true", "inv1_2_1: u ∈ U: false",
				"inv1_2_2: v ∈ ℕ: false", "thm1_2_3: v ≥ 0: true");

		try {
			DecompositionUtils.createInvariants(mch4_1, elemDist3,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create invariants 3: There should be no exception");
			return;
		}
		
		testInvariants("Create invariants 3", mch4_1, "typing_u: u ∈ U: true",
				"typing_p: p ∈ ℙ(ℤ × V): true",
				"typing_v: v ∈ ℤ: true", 
				"inv1_2_1: u ∈ U: false", "inv1_2_2: v ∈ ℕ: false",
				"thm1_2_3: v ≥ 0: true", "inv1_3_5: p ∈ ℕ → V: false");
	}

	/**
	 * Utility method for testing
	 * {@link DecompositionUtils#createInvariants(IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}.
	 * 
	 * @param message
	 *            a message.
	 * @param mch
	 *            a machine.
	 * @param expected
	 *            expected set of invariant (in the form
	 *            "label: predicate: isTheorem").
	 */
	private void testInvariants(String message, IMachineRoot mch,
			String ... expected) {
		try {
			IInvariant[] invs = mch.getInvariants();
			assertEquals(message + ": Incorrect number of invariants",
					expected.length, invs.length);
			for (int i = 0; i < invs.length; i++) {
				String actual = invs[i].getLabel() + ": "
						+ invs[i].getPredicateString() + ": "
						+ invs[i].isTheorem();
				assertEquals(message + ": Incorrect invariant", expected[i],
						actual);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail(message + ": There should be no exception");
			return;
		}
	}
	
	@Test
	public void testCreateEvents() {
		try {
			DecompositionUtils.createEvents(mch2_1, elemDist1,
					new NullProgressMonitor());
			
			// Test number of events.
			IEvent[] events = mch2_1.getEvents();
			assertEquals("Create events 1: Incorrect number of events", 6,
					events.length);
			
			// Test the initialisation.
			IEvent evt = getEventWithLabel(mch2_1, IEvent.INITIALISATION);
			assertNotNull("Create events 1: Cannot find event INITIALISATION",
					evt);
			testEventSignature("Create events 1", evt, "INITIALISATION", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 1", evt);
			testEventGuards("Create events 1", evt);
			testEventWitnesses("Create events 1", evt);
			testEventActions("Create events 1", evt,
					"act_init1_3_2: v :∣ ∃u'·u' = e ∧ v' = 0",
					"act_init1_3_3: z :∣ ∃p'·z' = 0 ∧ p' = ℕ × {g}");
			
			// Test evt1_3_1 (internal).
			evt = getEventWithLabel(mch2_1, "evt1_3_1");
			assertNotNull("Create events 2: Cannot find event evt1_3_1", evt);
			testEventSignature("Create events 2", evt, "evt1_3_1", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 2", evt);
			testEventGuards("Create events 2", evt, "grd1_3_1_1: z = 0: false");
			testEventWitnesses("Create events 2", evt);
			testEventActions("Create events 2", evt, "act1_3_1_1: v ≔ 2");

			// Test evt1_3_5 (internal).
			evt = getEventWithLabel(mch2_1, "evt1_3_5");
			assertNotNull("Create events 3: Cannot find event evt1_3_5", evt);
			testEventSignature("Create events 3", evt, "evt1_3_5", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 3", evt);
			testEventGuards("Create events 3", evt, "grd1_3_5_1: z ≥ 2: false");
			testEventWitnesses("Create events 3", evt);
			testEventActions("Create events 3", evt, "act1_3_5_1: z ≔ z − 1");

			// Test evt1_3_2 (external).
			evt = getEventWithLabel(mch2_1, "evt1_3_2");
			assertNotNull("Create events 4: Cannot find event evt1_3_2", evt);
			testEventSignature("Create events 4", evt, "evt1_3_2", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 4", evt, "y", "u");
			testEventGuards("Create events 4", evt, "typing_y: y ∈ ℤ: true",
					"typing_u: u ∈ U: true", "grd1_3_2_1: y ≠ 0: false",
					"grd1_3_2_2: u = f: false");
			testEventWitnesses("Create events 2", evt);
			testEventActions("Create events 2", evt,
					"merged_act: v :∣ ∃y', u'·y' = y + 2 ∧ u' = e ∧ v' = v + 1");
			
			// Test evt1_3_3 (external).
			evt = getEventWithLabel(mch2_1, "evt1_3_3");
			assertNotNull("Create events 5: Cannot find event evt1_3_3", evt);
			testEventSignature("Create events 5", evt, "evt1_3_3", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 5", evt, "t", "y");
			testEventGuards("Create events 5", evt, "typing_y: y ∈ ℤ: true",
					"grd1_3_3_1: t ≠ a: false", "grd1_3_3_2: y ≥ 5: false",
					"grd1_3_3_3: y > v: false");
			testEventWitnesses("Create events 2", evt);
			testEventActions("Create events 5", evt,
					"merged_act: v :∣ ∃y'·y' = y − 4 ∧ v' = v + 1");

			// Test evt1_3_4 (external).
			evt = getEventWithLabel(mch2_1, "evt1_3_4");
			assertNotNull("Create events 6: Cannot find event evt1_3_4", evt);
			testEventSignature("Create events 6", evt, "evt1_3_4", false,
					Convergence.ORDINARY);
			testEventParameters("Create events 6", evt, "r", "p", "u");
			testEventGuards("Create events 6", evt,
					"typing_p: p ∈ ℙ(ℤ × V): true", "typing_u: u ∈ U: true",
					"grd1_2_4_1: v ≥ 3: false", "grd1_3_4_1: r ∈ ℕ: false",
					"grd1_3_4_2: p(r) = g: false");
			testEventWitnesses("Create events 2", evt);
			testEventActions("Create events 6", evt,
					"merged_act: v :∣ ∃u', p'·v' = v − 1 ∧ u' = e ∧ p' = p{r ↦ h}");

		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create events 1: There should be no exception");
			return;
		}
	

	}

	/**
	 * Test method for {@link DecompositionUtils#decomposeAction(IAction, Set)}.
	 */
	@Test
	public void testDecomposeAction() {
		try {
			Set<String> vars;
			String assignmentStr;
			vars = new HashSet<String>();
			vars.add("z");
			vars.add("v");
			
			assignmentStr = DecompositionUtils.decomposeAction(act_init1_3_1, vars);
			assertNull("Incorrect decomposed action 1", assignmentStr);
			
			assignmentStr = DecompositionUtils.decomposeAction(act_init1_3_2, vars);
			assertEquals("Incorrect decomposed action 2", assignmentStr,
					"v :∣ ∃u'·u' = e ∧ v' = 0");
			
			assignmentStr = DecompositionUtils.decomposeAction(act_init1_3_3, vars);
			assertEquals("Incorrect decomposed action 3", assignmentStr,
					"z :∣ ∃p'·z' = 0 ∧ p' = ℕ × {g}");

			assignmentStr = DecompositionUtils.decomposeAction(act1_3_1_1, vars);
			assertEquals("Incorrect decomposed action 4", assignmentStr,
					"v :∣ v' = 2");

			assignmentStr = DecompositionUtils.decomposeAction(act1_3_2_1, vars);
			assertNull("Incorrect decomposed action 5", assignmentStr);

			assignmentStr = DecompositionUtils.decomposeAction(act1_3_2_2, vars);
			assertNull("Incorrect decomposed action 6", assignmentStr);
			
			assignmentStr = DecompositionUtils.decomposeAction(act1_3_2_3, vars);
			assertEquals("Incorrect decomposed action 7", assignmentStr,
					"v :∣ v' = v + 1");

			assignmentStr = DecompositionUtils.decomposeAction(act1_3_3_1, vars);
			assertNull("Incorrect decomposed action 8", assignmentStr);

			assignmentStr = DecompositionUtils.decomposeAction(act1_3_3_2, vars);
			assertEquals("Incorrect decomposed action 9", assignmentStr,
					"v :∣ v' = v + 1");

			assignmentStr = DecompositionUtils.decomposeAction(act1_3_4_1, vars);
			assertNull("Incorrect decomposed action 10", assignmentStr);

			assignmentStr = DecompositionUtils.decomposeAction(act1_3_5_1, vars);
			assertEquals("Incorrect decomposed action 11", assignmentStr,
					"z :∣ z' = z − 1");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

}