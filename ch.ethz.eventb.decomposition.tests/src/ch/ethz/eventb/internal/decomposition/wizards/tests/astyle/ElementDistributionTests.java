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

package ch.ethz.eventb.internal.decomposition.wizards.tests.astyle;

import java.util.Set;

import org.eventb.core.IEvent;
import org.junit.Test;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.ISubModel;
import ch.ethz.eventb.internal.decomposition.astyle.DecompositionUtils;
import ch.ethz.eventb.internal.decomposition.DefaultSubModel;

/**
 * @author htson
 *         <p>
 *         Test class for {@link ISubModel}.
 *         </p>
 */
public class ElementDistributionTests extends AbstractDecompositionTests {
	
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

	/**
	 * Test method for {@link ISubModel#getAccessedVariables()}.
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
	 * Utility method to test
	 * {@link ISubModel#getAccessedVariables()}.
	 * 
	 * @param message
	 *            a message.
	 * @param subModel
	 *            an element distribution.
	 * @param expected
	 *            expected set of accessed variables (in {@link String}).
	 */
	private void testGetAccessedVariables(String message,
			ISubModel subModel, String ... expected) {
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

}
