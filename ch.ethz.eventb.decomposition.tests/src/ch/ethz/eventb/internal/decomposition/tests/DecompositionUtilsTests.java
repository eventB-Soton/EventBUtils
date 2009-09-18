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

package ch.ethz.eventb.internal.decomposition.tests;

import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IConvergenceElement.Convergence;
import org.junit.Test;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.ISubModel;
import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.internal.decomposition.DecompositionUtils;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         Test class for {@link AStyleUtils}.
 *         </p>
 */
public class DecompositionUtilsTests extends AbstractDecompositionTests {

	/**
	 * Test method for {@link DecompositionUtils#getAccessedVariables()}.
	 */
	@Test
	public void testGetAccessedVariables() {
		ISubModel subModel;

		subModel = modelDecomp1.addSubModel();
		subModel.setElements(evt1_1_1, evt1_1_2);
		testGetAccessedVariables("Calculate accessed variables 1", subModel,
				"x", "y");

		subModel = modelDecomp1.addSubModel();
		subModel.setElements(evt1_1_3);
		testGetAccessedVariables("Calculate accessed variables 2", subModel,
				"x", "y");

		subModel = modelDecomp2.addSubModel();
		subModel.setElements(evt1_2_1, evt1_2_3);
		testGetAccessedVariables("Calculate accessed variables 3", subModel,
				"x", "y", "v");

		subModel = modelDecomp2.addSubModel();
		subModel.setElements(evt1_2_2, evt1_2_4);
		testGetAccessedVariables("Calculate accessed variables 4", subModel,
				"x", "y", "u", "v");

		subModel = modelDecomp3.addSubModel();
		subModel.setElements(evt1_3_1, evt1_3_5);
		testGetAccessedVariables("Calculate accessed variables 5", subModel,
				"z", "v");

		subModel = modelDecomp3.addSubModel();
		subModel.setElements(evt1_3_2, evt1_3_3);
		testGetAccessedVariables("Calculate accessed variables 6", subModel,
				"u", "v", "y");

		subModel = modelDecomp3.addSubModel();
		subModel.setElements(evt1_3_1);
		testGetAccessedVariables("Calculate accessed variables 7", subModel,
				"z", "v");

		subModel = modelDecomp3.addSubModel();
		subModel.setElements(evt1_3_2);
		testGetAccessedVariables("Calculate accessed variables 8", subModel,
				"v", "y", "u");

		subModel = modelDecomp3.addSubModel();
		subModel.setElements(evt1_3_3);
		testGetAccessedVariables("Calculate accessed variables 9", subModel,
				"v", "y");

		subModel = modelDecomp3.addSubModel();
		subModel.setElements(evt1_3_4);
		testGetAccessedVariables("Calculate accessed variables 10", subModel,
				"u", "v", "p");

		subModel = modelDecomp3.addSubModel();
		subModel.setElements(evt1_3_5);
		testGetAccessedVariables("Calculate accessed variables 11", subModel,
				"z");

	}

	/**
	 * Utility method to test {@link DecompositionUtils#getAccessedVariables()}.
	 * 
	 * @param message
	 *            a message.
	 * @param subModel
	 *            an element distribution.
	 * @param expected
	 *            expected set of accessed variables (in {@link String}).
	 */
	private void testGetAccessedVariables(String message, ISubModel subModel,
			String... expected) {
		Set<String> vars;
		try {
			vars = DecompositionUtils.getAccessedVariables(subModel);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Get accessed variables: There should be no exception");
			return;
		}
		assertEqualsVariables(message, expected, vars);
	}

	/**
	 * Utility method to compare two set of variables (in {@link String}).
	 * 
	 * @param message
	 *            a message.
	 * @param expected
	 *            expected array of variables.
	 * @param actual
	 *            actual set of variables.
	 */
	private void assertEqualsVariables(String message, String[] expected,
			Set<String> actual) {
		assertEquals(message + ": Incorrect number of expected variables",
				expected.length, actual.size());
		for (String exp : expected) {
			assertTrue(message + ": Expected variable " + exp + " not found",
					actual.contains(exp));
		}
	}

	/**
	 * Test method for
	 * {@link DecompositionUtils#decomposeInvariants(IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testCreateInvariants() {
		try {
			DecompositionUtils.decomposeInvariants(mch2_1, subModel1,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create invariants 1: There should be no exception");
			return;
		}

		testInvariants("Create invariants 1", mch2_1,
				Messages.decomposition_typing + "_z: z ∈ ℤ: true",
				Messages.decomposition_typing + "_v: v ∈ ℤ: true",
				"mch1_2_inv1_2_2: v ∈ ℕ: false", "mch1_2_thm1_2_3: v ≥ 0: true");

		try {
			DecompositionUtils.decomposeInvariants(mch3_1, subModel2,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create invariants 2: There should be no exception");
			return;
		}

		testInvariants("Create invariants 2", mch3_1,
				Messages.decomposition_typing + "_u: u ∈ U: true",
				Messages.decomposition_typing + "_y: y ∈ ℤ: true",
				Messages.decomposition_typing + "_v: v ∈ ℤ: true",
				"mch1_1_inv1_1_2: y ∈ ℕ: false",
				"mch1_1_thm1_1_3: y ≥ 0: true",
				"mch1_2_inv1_2_1: u ∈ U: false",
				"mch1_2_inv1_2_2: v ∈ ℕ: false", "mch1_2_thm1_2_3: v ≥ 0: true");

		try {
			DecompositionUtils.decomposeInvariants(mch4_1, subModel3,
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create invariants 3: There should be no exception");
			return;
		}

		testInvariants("Create invariants 3", mch4_1,
				Messages.decomposition_typing + "_u: u ∈ U: true",
				Messages.decomposition_typing + "_p: p ∈ ℙ(ℤ × V): true",
				Messages.decomposition_typing + "_v: v ∈ ℤ: true",
				"mch1_2_inv1_2_1: u ∈ U: false",
				"mch1_2_inv1_2_2: v ∈ ℕ: false",
				"mch1_2_thm1_2_3: v ≥ 0: true",
				"mch1_3_inv1_3_5: p ∈ ℕ → V: false");
	}

	/**
	 * Utility method to test
	 * {@link DecompositionUtils#decomposeInvariants(IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
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
			String... expected) {
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

	/**
	 * Test method for
	 * {@link DecompositionUtils#setEventStatus(IEvent, IEvent, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testConvergenceAttributeOnInternal() throws Exception {
		IEventBProject prj = createRodinProject("P");
		// The non-decomposed machine with a source event
		IMachineRoot m = createMachine(prj, "M");
		IEvent srcEvt = createEvent(m, "evt");

		// The sub-machine with a destination event
		IMachineRoot m1 = createMachine(prj, "M1");
		IEvent destEvt = createEvent(m1, "evt");

		// If the source event is ordinary, the destination event is ordinary
		srcEvt.setConvergence(Convergence.ORDINARY, monitor);
		destEvt.setConvergence(Convergence.CONVERGENT, monitor);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, monitor);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is convergent, the destination event is ordinary
		srcEvt.setConvergence(Convergence.CONVERGENT, monitor);
		destEvt.setConvergence(Convergence.CONVERGENT, monitor);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, monitor);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is anticipated, the destination event is
		// anticipated
		srcEvt.setConvergence(Convergence.ANTICIPATED, monitor);
		destEvt.setConvergence(Convergence.CONVERGENT, monitor);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, monitor);
		assertTrue("The destination event should be anticipated", destEvt
				.getConvergence().equals(Convergence.ANTICIPATED));
	}

	/**
	 * Test method for
	 * {@link DecompositionUtils#setEventStatus(IEvent, IEvent, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testConvergenceAttributeOnExternal() throws Exception {
		IEventBProject prj = createRodinProject("P");
		// The non-decomposed machine with a source event
		IMachineRoot m = createMachine(prj, "M");
		IEvent srcEvt = createEvent(m, "evt");

		// The sub-machine with a destination event
		IMachineRoot m1 = createMachine(prj, "M1");
		IEvent destEvt = createEvent(m1, "evt");
		IExternalElement destElt = (IExternalElement) destEvt
				.getAdapter(IExternalElement.class);
		destElt.setExternal(true, monitor);

		// If the source event is ordinary, the destination event is ordinary
		srcEvt.setConvergence(Convergence.ORDINARY, monitor);
		destEvt.setConvergence(Convergence.CONVERGENT, monitor);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, monitor);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is convergent, the destination event is ordinary
		srcEvt.setConvergence(Convergence.CONVERGENT, monitor);
		destEvt.setConvergence(Convergence.CONVERGENT, monitor);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, monitor);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is anticipated, the destination event is ordinary
		srcEvt.setConvergence(Convergence.ANTICIPATED, monitor);
		destEvt.setConvergence(Convergence.CONVERGENT, monitor);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, monitor);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
	}

	/**
	 * Test method for
	 * {@link DecompositionUtils#setEventStatus(IEvent, IEvent, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testExtendedAttribute() throws Exception {
		IEventBProject prj = createRodinProject("P");
		// The non-decomposed machine with a source event
		IMachineRoot m = createMachine(prj, "M");
		IEvent srcEvt = createEvent(m, "evt");

		// The sub-machine with a destination event
		IMachineRoot m1 = createMachine(prj, "M1");
		IEvent destEvt = createEvent(m1, "evt");

		// If the source event is non-extended, the destination event is
		// non-extended
		srcEvt.setExtended(false, monitor);
		destEvt.setExtended(true, monitor);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, monitor);
		assertFalse("The destination event should not be extended", destEvt
				.isExtended());

		// If the source event is extended, the destination event is
		// non-extended
		srcEvt.setExtended(true, monitor);
		destEvt.setExtended(true, monitor);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, monitor);
		assertFalse("The destination event should not be extended", destEvt
				.isExtended());
	}

	/**
	 * Test method for
	 * {@link DecompositionUtils#setEventStatus(IEvent, IEvent, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testExternalAttribute() throws Exception {
		IEventBProject prj = createRodinProject("P");
		// The non-decomposed machine with a source event
		IMachineRoot m = createMachine(prj, "M");
		IEvent srcEvt = createEvent(m, "evt");
		IExternalElement srcElt = (IExternalElement) srcEvt
				.getAdapter(IExternalElement.class);

		// The sub-machine with a destination event
		IMachineRoot m1 = createMachine(prj, "M1");
		IEvent destEvt = createEvent(m1, "evt");
		IExternalElement destElt = (IExternalElement) destEvt
				.getAdapter(IExternalElement.class);
		
		// If the source event is external, the destination event is external
		srcElt.setExternal(true, monitor);
		destElt.setExternal(false, monitor);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, monitor);
		assertTrue("The destination event should be external", destElt
				.isExternal());
	}
}
