/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 ******************************************************************************/

package ch.ethz.eventb.internal.decomposition.tests.sc;

import org.eventb.core.IEventBProject;
import org.eventb.core.IMachineRoot;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.eventb.decomposition.IDecomposedElement;
import ch.ethz.eventb.internal.decomposition.tests.AbstractSCTests;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;

/**
 * The class used to test the behavior of the static checker on machines.
 */
public class MachineModuleTest extends AbstractSCTests {

	private IEventBProject prj;
	private IMachineRoot machine;

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		prj = createRodinProject("P");
		machine = createMachine(prj, "m0");
		machine.getRodinFile().save(monitor, false);
	}

	/**
	 * Checks that a non-decomposed machine has the expected configuration.
	 */
	@Test
	public void testNonDecomposedMachineConfiguration() throws Exception {
		// Builds the .bcm file
		runBuilder(prj);
		
		assertEquals(machine.getSCMachineRoot().getConfiguration(),
				EventBUtils.DECOMPOSITION_CONFIG_SC);
	}

	/**
	 * Checks that a decomposed machine has the expected configuration.
	 */
	@Test
	public void testDecomposedMachineConfiguration() throws Exception {
		IDecomposedElement elt = (IDecomposedElement) machine
				.getAdapter(IDecomposedElement.class);
		elt.setDecomposed(monitor);
		machine.getRodinFile().save(monitor, false);
		
		// Builds the .bcm file
		runBuilder(prj);

		assertEquals(machine.getSCMachineRoot().getConfiguration(),
				EventBUtils.DECOMPOSITION_CONFIG_POG);
	}
}
