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

import org.junit.Test;

import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.decomposition.ISubModel;

/**
 * @author htson
 *         <p>
 *         Test class for {@link IModelDecomposition}.
 *         </p>
 */
public class ModelDecompositionTests extends AbstractDecompositionTests {

	/**
	 * Test method for {@link IModelDecomposition#getMachineRoot()}.
	 */
	@Test
	public void testGetMachineRoot() {
		assertEquals("Get machine root 1", mch1_1, modelDecomp1.getMachineRoot());
		assertEquals("Get machine root 2", mch1_2, modelDecomp2.getMachineRoot());
		assertEquals("Get machine root 3", mch1_3, modelDecomp3.getMachineRoot());
	}
	
	/**
	 * Test method for {@link IModelDecomposition#createSubModel()}.
	 */
	@Test
	public void testCreateElementDistribution() {
		testCreateElementDistribution("Create element distribution 1",
				modelDecomp1);
		testCreateElementDistribution("Create element distribution 2",
				modelDecomp2);
		testCreateElementDistribution("Create element distribution 3",
				modelDecomp3);
	}

	/**
	 * Utility method to test
	 * {@link IModelDecomposition#createSubModel()}.
	 * 
	 * @param message
	 *            a message.
	 * @param modelDecomp
	 *            a model distribution.
	 */
	private void testCreateElementDistribution(String message,
			IModelDecomposition modelDecomp) {
		ISubModel elemDist = modelDecomp.addSubModel();
		assertEquals(message + ": Incorrect project name",
				ISubModel.DEFAULT_PROJECT_NAME, elemDist
						.getComponentName());
		assertEquals(message + ": Incorrect model distribution", modelDecomp,
				elemDist.getModelDecomposition());
		ISubModel[] elemDists = modelDecomp.getSubModels();
		boolean found = false;
		for (ISubModel dist : elemDists) {
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
	 * Test method for {@link IModelDecomposition#getSubModels()}.
	 */
	@Test
	public void testGetElementDistributions() {
		ISubModel[] subModels = modelDecomp1.getSubModels();
		assertEquals("There should be no element distributions", 0,
				subModels.length);
		
		subModels = modelDecomp3.getSubModels();
		assertContains("Contain element distribution 1", subModels, subModel1);
		assertContains("Contain element distribution 2", subModels, subModel2);
		assertContains("Contain element distribution 3", subModels, subModel3);
	}

	/**
	 * Utility method to test
	 * {@link IModelDecomposition#getSubModels()} to check if an
	 * element distribution is contained in a list of element distributions.
	 * 
	 * @param message
	 *            a message.
	 * @param subModels
	 *            an array of element distribution.
	 * @param subModel
	 *            an element distribution.
	 */
	private void assertContains(String message,
			ISubModel[] subModels, ISubModel subModel) {
		boolean found = false;
		for (ISubModel dist : subModels) {
			if (dist.equals(subModel)) {
				found = true;
				break;
			}
		}
		if (!found) {
			fail(message + ": Cannot find element distribution "
					+ subModel.getComponentName());
		}
	}
	
	/**
	 * Test method for
	 * {@link IModelDecomposition#removeSubModel(ISubModel)}.
	 */
	@Test
	public void testRemoveElementDistribution() {
		ISubModel subModel = modelDecomp3.addSubModel();
		modelDecomp3.removeSubModel(subModel);
		ISubModel[] subModels = modelDecomp3.getSubModels();
		assertEquals("Incorrect number of distributions", 3, subModels.length);
		assertContains("Remove element distribution 1", subModels, subModel1);
		assertContains("Remove element distribution 2", subModels, subModel2);
		assertContains("Remove element distribution 3", subModels, subModel3);
		
		modelDecomp3.removeSubModel(subModel2);
		subModels = modelDecomp3.getSubModels();
		assertEquals("Incorrect number of distributions", 2, subModels.length);
		assertContains("Remove element distribution 4", subModels, subModel1);
		assertContains("Remove element distribution 5", subModels, subModel3);
	}
}
