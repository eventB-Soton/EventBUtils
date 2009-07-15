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

import java.util.Set;

import org.junit.Test;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution;
import ch.ethz.eventb.internal.decomposition.wizards.IModelDistribution;

/**
 * @author htson
 *         <p>
 *         Test class for {@link IModelDistribution}.
 *         </p>
 */
public class ModelDistributionTests extends AbstractDecompositionTests {

	/**
	 * Test method for {@link IModelDistribution#getMachineRoot()}.
	 */
	@Test
	public void testGetMachineRoot() {
		assertEquals("Get machine root 1", mch1_1, modelDist1.getMachineRoot());
		assertEquals("Get machine root 2", mch1_2, modelDist2.getMachineRoot());
		assertEquals("Get machine root 3", mch1_3, modelDist3.getMachineRoot());
	}
	
	/**
	 * Test method for {@link IModelDistribution#createElementDistribution()}.
	 */
	@Test
	public void testCreateElementDistribution() {
		testCreateElementDistribution("Create element distribution 1",
				modelDist1);
		testCreateElementDistribution("Create element distribution 2",
				modelDist2);
		testCreateElementDistribution("Create element distribution 3",
				modelDist3);
	}

	/**
	 * Utility method for testing
	 * {@link IModelDistribution#createElementDistribution()}.
	 * 
	 * @param message
	 *            a message.
	 * @param modelDist
	 *            a model distribution.
	 */
	private void testCreateElementDistribution(String message,
			IModelDistribution modelDist) {
		IElementDistribution elemDist = modelDist.createElementDistribution();
		assertEquals(message + ": Incorrect project name",
				IElementDistribution.DEFAULT_PROJECT_NAME, elemDist
						.getProjectName());
		assertEquals(message + ": Incorrect model distribution", modelDist,
				elemDist.getModelDistribution());
		IElementDistribution[] elemDists = modelDist.getElementDistributions();
		boolean found = false;
		for (IElementDistribution dist : elemDists) {
			if (elemDist.equals(dist)) {
				found = true;
				break;
			}
		}
		if (!found)
			fail(message
					+ ": Cannot find the newly created element distribution");
	}

	/**
	 * Test method for {@link IModelDistribution#getElementDistributions()}.
	 */
	@Test
	public void testGetElementDistributions() {
		IElementDistribution[] elemDists = modelDist1.getElementDistributions();
		assertEquals("There should be no element distributions", 0,
				elemDists.length);
		
		elemDists = modelDist3.getElementDistributions();
		assertContains("Contain element distribution 1", elemDists, elemDist1);
		assertContains("Contain element distribution 2", elemDists, elemDist2);
		assertContains("Contain element distribution 3", elemDists, elemDist3);
	}

	/**
	 * Utility method for testing
	 * {@link IModelDistribution#getElementDistributions()} to check if an
	 * element distribution is contained in a list of element distributions.
	 * 
	 * @param message
	 *            a message.
	 * @param elemDists
	 *            an array of element distribution.
	 * @param elemDist
	 *            an element distribution.
	 */
	private void assertContains(String message,
			IElementDistribution[] elemDists, IElementDistribution elemDist) {
		boolean found = false;
		for (IElementDistribution dist : elemDists) {
			if (dist.equals(elemDist)) {
				found = true;
				break;
			}
		}
		if (!found) {
			fail(message + ": Cannot find element distribution "
					+ elemDist.getProjectName());
		}
	}
	
	/**
	 * Test method for
	 * {@link IModelDistribution#removeElementDistribution(IElementDistribution)}.
	 */
	@Test
	public void testRemoveElementDistribution() {
		IElementDistribution elemDist = modelDist3.createElementDistribution();
		modelDist3.removeElementDistribution(elemDist);
		IElementDistribution[] elemDists = modelDist3.getElementDistributions();
		assertEquals("Incorrect number of distributions", 3, elemDists.length);
		assertContains("Remove element distribution 1", elemDists, elemDist1);
		assertContains("Remove element distribution 2", elemDists, elemDist2);
		assertContains("Remove element distribution 3", elemDists, elemDist3);
		
		modelDist3.removeElementDistribution(elemDist2);
		elemDists = modelDist3.getElementDistributions();
		assertEquals("Incorrect number of distributions", 2, elemDists.length);
		assertContains("Remove element distribution 4", elemDists, elemDist1);
		assertContains("Remove element distribution 5", elemDists, elemDist3);
	}

	/**
	 * Test method for {@link IModelDistribution#getSharedVariables()}.
	 */
	@Test
	public void testGetSharedVariables() {
		Set<String> vars;
		try {
			vars = modelDist3.getSharedVariables();
			assertEqualsVariables("", vars, "u", "v");
			vars = modelDist1.getSharedVariables();
			assertEqualsVariables("", vars);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method to compare two set of variables (in {@link String}).
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
