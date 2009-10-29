/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 ******************************************************************************/
package ch.ethz.eventb.internal.decomposition.tests.pog;

import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IPORoot;
import org.eventb.core.IPOSequent;

import org.junit.Test;

import ch.ethz.eventb.decomposition.IDecomposedElement;
import ch.ethz.eventb.internal.decomposition.tests.AbstractSCTests;

/**
 * The class used to test the behavior of the proof obligation generator (POG).
 */
public class POGTest extends AbstractSCTests {

	/**
	 * Checks that non-decomposed machine's POs are generated.
	 */
	@Test
	public void testNonDecomposedMachinePOGeneration() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot machine = createMachine(prj, "m0");
		createVariable(machine, "v0");
		createInvariant(machine, "inv0", "v0 ∈ ℕ", false);
		final IEvent init = createEvent(machine, IEvent.INITIALISATION);
		createAction(init, "act0", "v0 ≔ 0");
		machine.getRodinFile().save(monitor, false);

		// Builds the .bcm file
		runBuilder(prj);

		IPORoot po = machine.getPORoot();
		assertEquals(1, po.getChildrenOfType(IPOSequent.ELEMENT_TYPE).length);
	}
	
	/**
	 * Checks that decomposed machine's POs are not generated.
	 */
	@Test
	public void testDecomposedMachinePOGeneration() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IMachineRoot machine = createMachine(prj, "m0");
		IDecomposedElement elt = (IDecomposedElement) machine
				.getAdapter(IDecomposedElement.class);
		elt.setDecomposed(monitor);
		createVariable(machine, "v0");
		createInvariant(machine, "inv0", "v0 ∈ ℕ", false);
		final IEvent init = createEvent(machine, IEvent.INITIALISATION);
		createAction(init, "act0", "v0 ≔ 0");
		machine.getRodinFile().save(monitor, false);

		// Builds the .bcm file
		runBuilder(prj);

		IPORoot po = machine.getPORoot();
		assertEquals(0, po.getChildrenOfType(IPOSequent.ELEMENT_TYPE).length);
	}
	
	/**
	 * Checks that non-decomposed context's POs are generated.
	 */
	@Test
	public void testNonDecomposedContextPOGeneration() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IContextRoot context = createContext(prj, "ctx");
		createConstant(context, "a");
		createConstant(context, "b");
		createAxiom(context, "axm0", "a ∈ ℕ", false);
		createAxiom(context, "axm1", "a ≠ b", true);
		context.getRodinFile().save(monitor, false);

		// Builds the .bcm file
		runBuilder(prj);

		IPORoot po = context.getPORoot();
		assertEquals(1, po.getChildrenOfType(IPOSequent.ELEMENT_TYPE).length);
	}

	/**
	 * Checks that decomposed context's POs are not generated.
	 */
	@Test
	public void testDecomposedContextPOGeneration() throws Exception {
		IEventBProject prj = createRodinProject("P");
		IContextRoot context = createContext(prj, "ctx");
		IDecomposedElement elt = (IDecomposedElement) context
				.getAdapter(IDecomposedElement.class);
		elt.setDecomposed(monitor);
		createConstant(context, "a");
		createConstant(context, "b");
		createAxiom(context, "axm0", "a ∈ ℕ", false);
		createAxiom(context, "axm1", "a ≠ b", true);
		context.getRodinFile().save(monitor, false);

		// Builds the .bcm file
		runBuilder(prj);

		IPORoot po = context.getPORoot();
		assertEquals(0, po.getChildrenOfType(IPOSequent.ELEMENT_TYPE).length);
	}
}
