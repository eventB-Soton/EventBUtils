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

import org.eventb.core.IEvent;
import org.junit.Test;
import org.rodinp.core.IRodinElement;

import ch.ethz.eventb.decomposition.ISubModel;
import ch.ethz.eventb.internal.decomposition.DefaultSubModel;

/**
 * @author htson
 *         <p>
 *         Test class for {@link ISubModel}.
 *         </p>
 */
public class SubModelTests extends AbstractDecompositionTests {
	
	/**
	 * Test method for {@link ISubModel#getModelDecomposition()}.
	 */
	@Test
	public void testGetModelDistribution() {
		assertEquals("Incorrect model distribution 1", modelDecomp3, subModel1
				.getModelDecomposition());
		assertEquals("Incorrect model distribution 2", modelDecomp3, subModel2
				.getModelDecomposition());
		assertEquals("Incorrect model distribution 3", modelDecomp3, subModel3
				.getModelDecomposition());
	}
	
	/**
	 * Test method for {@link ISubModel#getMachineRoot()}.
	 */
	@Test
	public void testGetMachineRoot() {
		assertEquals("Incorrect machine root 1", mch1_3, subModel1
				.getMachineRoot());
		assertEquals("Incorrect machine root 2", mch1_3, subModel2
				.getMachineRoot());
		assertEquals("Incorrect machine root 3", mch1_3, subModel3
				.getMachineRoot());
	}
	
	/**
	 * Test method for {@link ISubModel#setProjectName(String)} and
	 * {@link ISubModel#getProjectName()}.
	 */
	@Test
	public void testSetAndGetProjectName() {
		ISubModel subModel = new DefaultSubModel(modelDecomp3);
		testSetAndGetProjectName(subModel, "ProjectA");
		testSetAndGetProjectName(subModel, "ProjectB");
		testSetAndGetProjectName(subModel, "ProjectC");
	}
	
	/**
	 * Utility method to test
	 * {@link ISubModel#setProjectName(String)} and
	 * {@link ISubModel#getProjectName()}.
	 * 
	 * @param subModel an element distribution.
	 * @param prjName the name of the project.
	 */
	private void testSetAndGetProjectName(ISubModel subModel,
			String prjName) {
		subModel.setProjectName(prjName);
		assertEquals("Incorrect project name", prjName, subModel
				.getProjectName());	
	}

	/**
	 * Test method for {@link ISubModel#setEvents(String...)}
	 * and {@link ISubModel#getEvents()}.
	 */
	@Test
	public void testSetAndGetEventLabels() {
		ISubModel subModel = new DefaultSubModel(modelDecomp3);
		
		testSetAndGetElements(subModel, evt1_3_1, evt1_3_2);
		testSetAndGetElements(subModel, evt1_3_1);
		testSetAndGetElements(subModel, evt1_3_2, evt1_3_3);
		testSetAndGetElements(subModel, evt1_3_1, evt1_3_5);
		testSetAndGetElements(subModel, evt1_3_4, evt1_3_2);
		testSetAndGetElements(subModel, evt1_3_1, evt1_3_2, evt1_3_3);		
	}

	/**
	 * Utility method to test
	 * {@link ISubModel#setEvents(String...)} and
	 * {@link ISubModel#getEvents()}.
	 * 
	 * @param subModel
	 *            an element distribution.
	 * @param evts
	 *            an array of events.
	 */
	private void testSetAndGetElements(ISubModel subModel,
			IEvent... evts) {
		subModel.setElements(evts);
		IRodinElement[] elements = subModel.getElements();
		assertEquals("Incorrect event labels", evts, elements);
	}
}
