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
package ch.ethz.eventb.internal.decomposition.tests.astyle;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IAction;
import org.eventb.core.IAxiom;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IConstant;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IPredicateElement;
import org.eventb.core.ISeesContext;
import org.eventb.core.IVariable;
import org.eventb.core.IConvergenceElement.Convergence;
import org.junit.Test;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.ISubModel;
import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement.Nature;
import ch.ethz.eventb.internal.decomposition.DecompositionUtils;
import ch.ethz.eventb.internal.decomposition.astyle.AStyleUtils;
import ch.ethz.eventb.internal.decomposition.astyle.ModelDecomposition;
import ch.ethz.eventb.internal.decomposition.astyle.AStyleUtils.DecomposedEventType;
import ch.ethz.eventb.internal.decomposition.tests.AbstractDecompositionTests;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         Test class for {@link AStyleUtils}.
 *         </p>
 */
public class AStyleUtilsTests extends AbstractDecompositionTests {

	private static final String DECOMPOSED_MCH_NAME = "decomposed";
	private static final String DECOMPOSED_MCH_NAME_EXT = EventBPlugin.getMachineFileName(DECOMPOSED_MCH_NAME);

	/**
	 * Test method for {@link DecompositionUtils#getAccessedVariables()}.
	 */
	@Test
	public void testGetAccessedVariables() throws Exception {
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
			String... expected) throws Exception {
		Set<String> vars;
		try {
			vars = AStyleUtils.getAccessedVariables(subModel, null);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Get accessed variables: There should be no exception");
			return;
		}
		assertSameStrings("Accessed Variables", "variable", vars, expected);
	}

	/**
	 * Test method for
	 * {@link DecompositionUtils#decomposeInvariants(IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testCreateInvariants() throws Exception {
		try {
			AStyleUtils.decomposeInvariants(mch2_1, subModel1, null);
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
			AStyleUtils.decomposeInvariants(mch3_1, subModel2, null);
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
			AStyleUtils.decomposeInvariants(mch4_1, subModel3, null);
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
	 * Test method for
	 * {@link DecompositionUtils#decomposeInvariants(IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}
	 * Related to WD invariant creation
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateWDInvariants() throws Exception {
		final String mchName = "mch1_4";
		final IMachineRoot mch = createMachine(P1, mchName);
		createVariable(mch, "x");
		createVariable(mch, "y");
		createInvariant(mch, "inv1", "0 < x + y", false);
		final String inv2Label = "inv2";
		// invariant with WD: x≠0 ∧ y≠0
		createInvariant(mch, inv2Label, "(1÷x)÷y > 0", false);
		final IEvent event = createEvent(mch, "evt");
		createAction(event, "act", "x ≔ x+1");
		mch.getRodinFile().save(null, true);
		// runBuilder
		mch.getRodinProject().getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

		final ModelDecomposition decomp = new ModelDecomposition(mch);
		final ISubModel subModel = decomp.createSubModel();
		subModel.setElements(event);
		
		AStyleUtils.decomposeInvariants(mch2_1, subModel,
				null);
		testInvariants("WD invariants", mch2_1,
				Messages.decomposition_typing + "_x: x ∈ ℤ: true",
				"WD_" + mchName + "_" + inv2Label + ": x≠0: true");
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
	 * Test method
	 * {@link AStyleUtils#decomposeVariables(org.eventb.core.IMachineRoot, ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testCreateVariables() throws Exception {

		try {
			AStyleUtils.decomposeVariables(mch2_1, subModel1, monitor);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("Create variables 1: There should be no exception");
			return;
		}

		assertVariables("Create variables 1", mch2_1, "z: "
				+ Messages.decomposition_private_comment, "v: "
				+ Messages.decomposition_shared_comment);

		try {
			AStyleUtils.decomposeVariables(mch3_1, subModel2, monitor);
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
			AStyleUtils.decomposeVariables(mch4_1, subModel3, monitor);
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
	public void testDecomposeEvents() throws Exception {
		try {
			AStyleUtils.decomposeEvents(mch2_1, subModel1, monitor);

			// Test number of events.
			IEvent[] events = mch2_1.getEvents();
			assertEquals("Create events 1: Incorrect number of events", 6,
					events.length);

			// Test the initialization.
			IEvent evt = getEventWithLabel(mch2_1, IEvent.INITIALISATION);
			assertNotNull("Create events 1: Cannot find event INITIALISATION",
					evt);
			assertEvent("Create events 1", evt,
					Messages.decomposition_internal_comment);
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
	 * Test method for {@link AStyleUtils#getEventType(ISubModel, IEvent)} .
	 */
	@Test
	public void testGetEventType1() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot mch = createMachine(prj, "M");
		createVariable(mch, "v1");
		final IEvent evt1 = createEvent(mch, "evt1");
		createAction(evt1, "act1", "v1 ≔ 0");
		createVariable(mch, "v2");
		final IEvent evt2 = createEvent(mch, "evt2");
		createAction(evt2, "act2", "v2 ≔ v1");
		mch.getRodinFile().save(monitor, false);

		ModelDecomposition modelDecomp = new ModelDecomposition(mch);
		ISubModel subModel = modelDecomp.addSubModel();
		subModel.setElements(evt2);

		assertEquals(AStyleUtils.getEventType(subModel, evt1, monitor),
				DecomposedEventType.NONE);
	}

	/**
	 * Test method for {@link AStyleUtils#getEventType(ISubModel, IEvent)} .
	 */
	@Test
	public void testGetEventType2() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot mch = createMachine(prj, "M");
		createVariable(mch, "v1");
		createVariable(mch, "v2");
		createVariable(mch, "v3");
		final IEvent evt1 = createEvent(mch, "evt1");
		createAction(evt1, "act1", "v2 ≔ v1");
		final IEvent evt2 = createEvent(mch, "evt2");
		createAction(evt2, "act2", "v3 ≔ v1");
		mch.getRodinFile().save(monitor, false);

		ModelDecomposition modelDecomp = new ModelDecomposition(mch);
		ISubModel subModel1 = modelDecomp.addSubModel();
		subModel1.setElements(evt1);
		ISubModel subModel2 = modelDecomp.addSubModel();
		subModel2.setElements(evt2);

		assertEquals(AStyleUtils.getEventType(subModel1, evt2, monitor),
				DecomposedEventType.NONE);
	}

	/**
	 * Test method for {@link AStyleUtils#getEventType(ISubModel, IEvent)} .
	 */
	@Test
	public void testGetEventType3() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot mch = createMachine(prj, "M");
		createVariable(mch, "v1");
		createVariable(mch, "v2");
		createVariable(mch, "v3");
		final IEvent evt1 = createEvent(mch, "evt1");
		createAction(evt1, "act1", "v1 ≔ v2");
		final IEvent evt2 = createEvent(mch, "evt2");
		createAction(evt2, "act2", "v3 ≔ v1");
		final IEvent evt3 = createEvent(mch, "evt3");
		createAction(evt3, "act3", "v3 ≔ v2");
		mch.getRodinFile().save(monitor, false);

		ModelDecomposition modelDecomp = new ModelDecomposition(mch);
		ISubModel subModel1 = modelDecomp.addSubModel();
		subModel1.setElements(evt1);
		ISubModel subModel2 = modelDecomp.addSubModel();
		subModel2.setElements(evt2);
		ISubModel subModel3 = modelDecomp.addSubModel();
		subModel3.setElements(evt3);

		assertEquals(AStyleUtils.getEventType(subModel3, evt1, monitor),
				DecomposedEventType.NONE);
	}

	/**
	 * Test method for {@link AStyleUtils#getEventType(ISubModel, IEvent)} .
	 */
	@Test
	public void testGetEventType4() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot mch = createMachine(prj, "M");
		createVariable(mch, "v1");
		createVariable(mch, "v2");
		createVariable(mch, "v3");
		final IEvent evt1 = createEvent(mch, "evt1");
		createAction(evt1, "act1", "v1 ≔ v2");
		final IEvent evt2 = createEvent(mch, "evt2");
		createAction(evt2, "act2", "v3 ≔ v1");
		mch.getRodinFile().save(monitor, false);

		ModelDecomposition modelDecomp = new ModelDecomposition(mch);
		ISubModel subModel1 = modelDecomp.addSubModel();
		subModel1.setElements(evt1);
		ISubModel subModel2 = modelDecomp.addSubModel();
		subModel2.setElements(evt2);

		assertEquals(AStyleUtils.getEventType(subModel2, evt1, monitor),
				DecomposedEventType.EXTERNAL);
	}

	/**
	 * Test method for {@link AStyleUtils#getEventType(ISubModel, IEvent)} .
	 */
	@Test
	public void testGetEventType5() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot mch = createMachine(prj, "M");
		createVariable(mch, "v1");
		final IEvent evt1 = createEvent(mch, "evt1");
		createAction(evt1, "act1", "v1 ≔ 0");
		mch.getRodinFile().save(monitor, false);

		ModelDecomposition modelDecomp = new ModelDecomposition(mch);
		ISubModel subModel = modelDecomp.addSubModel();
		subModel.setElements(evt1);

		assertEquals(AStyleUtils.getEventType(subModel, evt1, monitor),
				DecomposedEventType.INTERNAL);
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
	public void testDecomposeAction() throws Exception {
		try {
			Set<String> vars;
			String assignmentStr;
			vars = new HashSet<String>();
			vars.add("z");
			vars.add("v");

			assignmentStr = AStyleUtils.decomposeAction(act_init1_3_1, vars,
					monitor);
			assertNull("Incorrect decomposed action 1", assignmentStr);

			assignmentStr = AStyleUtils.decomposeAction(act_init1_3_2, vars,
					monitor);
			assertEquals("Incorrect decomposed action 2", assignmentStr,
					"v ≔ 0");

			assignmentStr = AStyleUtils.decomposeAction(act_init1_3_3, vars,
					monitor);
			assertEquals("Incorrect decomposed action 3", assignmentStr,
					"z ≔ 0");

			assignmentStr = AStyleUtils.decomposeAction(act1_3_1_1, vars,
					monitor);
			assertEquals("Incorrect decomposed action 4", assignmentStr,
					"v ≔ 2");

			assignmentStr = AStyleUtils.decomposeAction(act1_3_2_1, vars,
					monitor);
			assertNull("Incorrect decomposed action 5", assignmentStr);

			assignmentStr = AStyleUtils.decomposeAction(act1_3_2_2, vars,
					monitor);
			assertNull("Incorrect decomposed action 6", assignmentStr);

			assignmentStr = AStyleUtils.decomposeAction(act1_3_2_3, vars,
					monitor);
			assertEquals("Incorrect decomposed action 7", assignmentStr,
					"v ≔ v + 1");

			assignmentStr = AStyleUtils.decomposeAction(act1_3_3_1, vars,
					monitor);
			assertNull("Incorrect decomposed action 8", assignmentStr);

			assignmentStr = AStyleUtils.decomposeAction(act1_3_3_2, vars,
					monitor);
			assertEquals("Incorrect decomposed action 9", assignmentStr,
					"v ≔ v + 1");

			assignmentStr = AStyleUtils.decomposeAction(act1_3_4_1, vars,
					monitor);
			assertNull("Incorrect decomposed action 10", assignmentStr);

			assignmentStr = AStyleUtils.decomposeAction(act1_3_5_1, vars,
					monitor);
			assertEquals("Incorrect decomposed action 11", assignmentStr,
					"z ≔ z − 1");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for {@link AStyleUtils#getSharedVariables()}.
	 */
	@Test
	public void testGetSharedVariables() throws Exception {
		Set<String> vars;
		try {
			vars = AStyleUtils.getSharedVariables(modelDecomp3, monitor);
			assertSameStrings("Shared Variables", "variable", vars, "u", "v");
			vars = AStyleUtils.getSharedVariables(modelDecomp1, monitor);
			assertSameStrings("Shared Variables", "variable", vars);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for {@link AStyleUtils#check()}.
	 */
	@Test
	public void testCheck1() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot m = createMachine(prj, "M");
		IEvent init = createEvent(m, IEvent.INITIALISATION);
		createVariable(m, "v1"); // private
		createVariable(m, "v2"); // shared
		createAction(init, "act0", "v1, v2 :∣ v1' = 0 ∧ v2' = 0");
		IEvent evt1 = createEvent(m, "evt1");
		createAction(evt1, "act1", "v1 ≔ 1");
		createAction(evt1, "act2", "v2 ≔ 2");
		IEvent evt2 = createEvent(m, "evt2");
		createAction(evt2, "act3", "v2 ≔ 3");
		m.getRodinFile().save(monitor, false);

		ModelDecomposition modelDecomp = new ModelDecomposition(m);
		ISubModel subModel1 = modelDecomp.addSubModel();
		subModel1.setElements(evt1);
		ISubModel subModel2 = modelDecomp.addSubModel();
		subModel2.setElements(evt2);

		try {
			AStyleUtils.check(modelDecomp, monitor);
			fail("An exception should have been thrown");
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), Messages.bind(
					Messages.scuser_ActionOnPrivateAndSharedError,
					"act0"));
		}
	}
	
	/**
	 * Test method for {@link AStyleUtils#check()}.
	 */
	@Test
	public void testCheck2() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot m = createMachine(prj, "M");
		IEvent init = createEvent(m, IEvent.INITIALISATION);
		createVariable(m, "v1"); // private
		createVariable(m, "v2"); // private
		createAction(init, "act0", "v1, v2 :∣ v1' = 0 ∧ v2' = 0");
		IEvent evt1 = createEvent(m, "evt1");
		createAction(evt1, "act1", "v1 ≔ 1");
		IEvent evt2 = createEvent(m, "evt2");
		createAction(evt2, "act2", "v2 ≔ 2");
		m.getRodinFile().save(monitor, false);

		ModelDecomposition modelDecomp = new ModelDecomposition(m);
		ISubModel subModel1 = modelDecomp.addSubModel();
		subModel1.setElements(evt1);
		ISubModel subModel2 = modelDecomp.addSubModel();
		subModel2.setElements(evt2);

		try {
			assertTrue(AStyleUtils.check(modelDecomp, monitor));
		} catch (IllegalArgumentException e) {
			fail("An exception should not have been thrown");
		}
	}
	
	/**
	 * Test method for {@link AStyleUtils#check()}.
	 */
	@Test
	public void testCheck3() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot m = createMachine(prj, "M");
		IEvent init = createEvent(m, IEvent.INITIALISATION);
		createVariable(m, "v1"); // shared
		createVariable(m, "v2"); // shared
		createAction(init, "act0", "v1, v2 :∣ v1' = 0 ∧ v2' = 0");
		IEvent evt1 = createEvent(m, "evt1");
		createAction(evt1, "act1", "v1 ≔ 1");
		createAction(evt1, "act2", "v2 ≔ 2");
		IEvent evt2 = createEvent(m, "evt2");
		createAction(evt2, "act3", "v1 ≔ 3");
		createAction(evt2, "act3", "v2 ≔ 3");
		m.getRodinFile().save(monitor, false);

		ModelDecomposition modelDecomp = new ModelDecomposition(m);
		ISubModel subModel1 = modelDecomp.addSubModel();
		subModel1.setElements(evt1);
		ISubModel subModel2 = modelDecomp.addSubModel();
		subModel2.setElements(evt2);

		try {
			assertTrue(AStyleUtils.check(modelDecomp, monitor));
		} catch (IllegalArgumentException e) {
			fail("An exception should not have been thrown");
		}
	}
	
	/**
	 * Test method for {@link AStyleUtils#check()}.
	 */
	@Test
	public void testCheck4() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot m = createMachine(prj, "M");
		IEvent init = createEvent(m, IEvent.INITIALISATION);
		createVariable(m, "v1"); // private
		createVariable(m, "v2"); // shared
		createAction(init, "act0", "v1, v2 :∣ v1' = 0 ∧ v2' = 0");
		IEvent evt1 = createEvent(m, "evt1");
		createAction(evt1, "act1", "v1 ≔ 1");
		IEvent evt2 = createEvent(m, "evt2");
		createAction(evt2, "act2", "v2 ≔ 2");
		IEvent evt3 = createEvent(m, "evt3");
		createAction(evt2, "act3", "v2 ≔ 3");
		m.getRodinFile().save(monitor, false);

		ModelDecomposition modelDecomp = new ModelDecomposition(m);
		ISubModel subModel1 = modelDecomp.addSubModel();
		subModel1.setElements(evt1);
		ISubModel subModel2 = modelDecomp.addSubModel();
		subModel2.setElements(evt2);
		ISubModel subModel3 = modelDecomp.addSubModel();
		subModel3.setElements(evt3);

		try {
			assertTrue(AStyleUtils.check(modelDecomp, monitor));
		} catch (IllegalArgumentException e) {
			fail("An exception should not have been thrown");
		}
	}
	
	private static void assertIdentifiers(String message, String type,
			IIdentifierElement[] actual, String... expected)
			throws RodinDBException {
		final Set<String> idents = new HashSet<String>();
		for (IIdentifierElement element : actual) {
			idents.add(element.getIdentifierString());
		}
		assertSameStrings(message, type, idents, expected);
	}
	
	private static void assertPredicates(String message, String type,
			IPredicateElement[] actual, String... expected)
			throws RodinDBException {
		final Set<String> predicates = new HashSet<String>();
		for (IPredicateElement element : actual) {
			predicates.add(element.getPredicateString());
		}
		assertSameStrings(message, type, predicates, expected);
	}
	
	
	private static <T> T[] ls(T... t) {
		return t;
	}
	
	/**
	 * Test method for
	 * {@link AStyleUtils#decomposeContexts(IMachineRoot, ISubModel, IProgressMonitor)}
	 */
	@Test
	public void testDecomposeContexts1() throws Exception {
		final ModelDecomposition modelDecomp = new ModelDecomposition(mch1_1);
		final ISubModel subModel = modelDecomp.addSubModel();
		mch1_1.getRodinFile().copy(P2.getRodinProject(), null, DECOMPOSED_MCH_NAME_EXT, false, null);
		final IMachineRoot decomposedMch = P2.getMachineRoot(DECOMPOSED_MCH_NAME);
		decomposedMch.getEvent(evt1_1_3.getElementName()).delete(true, null);
		deleteSeesClauses(decomposedMch);
		
		doTestDecompContext(decomposedMch, subModel, ls("a","b"), ls("S"),ls("a ∈ S","b ∈ S","a ≠ b"));
	}

	/**
	 * Test method for
	 * {@link AStyleUtils#decomposeContexts(IMachineRoot, ISubModel, IProgressMonitor)}
	 */
	@Test
	public void testDecomposeContexts2() throws Exception {
		final ModelDecomposition modelDecomp = new ModelDecomposition(mch1_3);
		final ISubModel subModel = modelDecomp.addSubModel();
		mch1_3.getRodinFile().copy(P2.getRodinProject(), null, DECOMPOSED_MCH_NAME_EXT, false, null);
		final IMachineRoot decomposedMch = P2.getMachineRoot(DECOMPOSED_MCH_NAME);
		decomposedMch.getEvent(evt1_3_1.getElementName()).delete(true, null);
		decomposedMch.getEvent(evt1_3_2.getElementName()).delete(true, null);
		deleteSeesClauses(decomposedMch);
		
		doTestDecompContext(decomposedMch, subModel,
				ls("a", "b", "c", "e", "g", "h"),
				ls("S", "U", "V"),
				ls("a ∈ S","b ∈ S","c ∈ S","partition(S, {a}, {b}, {c})", "a ≠ b",
						"e ∈ U","g ∈ V","h ∈ V","partition(V, {g}, {h})", "g ≠ h"));
	}

	private static void deleteSeesClauses(IMachineRoot mchRoot)
			throws RodinDBException {
		final ISeesContext[] seesClauses = mchRoot.getSeesClauses();
		for (ISeesContext seesContext : seesClauses) {
			seesContext.delete(true, null);
		}
	}

	private void doTestDecompContext(IMachineRoot decomposedMch, ISubModel subModel,
			String[] expectedConstants, String[] expectedSets,
			String[] expectedAxioms) {
		try {
			ctx2_1.clear(true, null);
			AStyleUtils.decomposeContext(ctx2_1, decomposedMch, subModel, null);

			// Test constants
			IConstant[] constants = ctx2_1.getConstants();
			assertIdentifiers("decomposing context", "constant", constants, expectedConstants);

			// Test carrier sets
			final ICarrierSet[] carrierSets = ctx2_1.getCarrierSets();
			assertIdentifiers("decomposing context", "carrier set", carrierSets, expectedSets);
			
			// Test  axioms
			final IAxiom[] axioms = ctx2_1.getAxioms();
			assertPredicates("decomposing context", "axiom", axioms, expectedAxioms);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Decompose contexts: There should be no exception");
		}
	}
}
