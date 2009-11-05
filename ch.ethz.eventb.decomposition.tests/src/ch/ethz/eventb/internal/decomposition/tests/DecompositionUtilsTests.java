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
package ch.ethz.eventb.internal.decomposition.tests;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBPlugin;
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
import org.eventb.core.IConvergenceElement.Convergence;
import org.junit.Test;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.ISubModel;
import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.internal.decomposition.DecompositionUtils;
import ch.ethz.eventb.internal.decomposition.astyle.AStyleUtils;
import ch.ethz.eventb.internal.decomposition.astyle.ModelDecomposition;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         Test class for {@link AStyleUtils}.
 *         </p>
 */
public class DecompositionUtilsTests extends AbstractDecompositionTests {

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
			vars = DecompositionUtils.getAccessedVariables(subModel, null);
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
			DecompositionUtils.decomposeInvariants(mch2_1, subModel1, null);
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
			DecompositionUtils.decomposeInvariants(mch3_1, subModel2, null);
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
			DecompositionUtils.decomposeInvariants(mch4_1, subModel3, null);
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
		
		DecompositionUtils.decomposeInvariants(mch2_1, subModel,
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
		srcEvt.setConvergence(Convergence.ORDINARY, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is convergent, the destination event is ordinary
		srcEvt.setConvergence(Convergence.CONVERGENT, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is anticipated, the destination event is
		// anticipated
		srcEvt.setConvergence(Convergence.ANTICIPATED, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
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
		destElt.setExternal(true, null);

		// If the source event is ordinary, the destination event is ordinary
		srcEvt.setConvergence(Convergence.ORDINARY, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is convergent, the destination event is ordinary
		srcEvt.setConvergence(Convergence.CONVERGENT, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is anticipated, the destination event is ordinary
		srcEvt.setConvergence(Convergence.ANTICIPATED, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
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
		srcEvt.setExtended(false, null);
		destEvt.setExtended(true, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertFalse("The destination event should not be extended", destEvt
				.isExtended());

		// If the source event is extended, the destination event is
		// non-extended
		srcEvt.setExtended(true, null);
		destEvt.setExtended(true, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
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
		srcElt.setExternal(true, null);
		destElt.setExternal(false, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be external", destElt
				.isExternal());
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