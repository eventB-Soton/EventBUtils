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

import ch.ethz.eventb.internal.decomposition.ISubModel;
import ch.ethz.eventb.internal.decomposition.astyle.SubModel;

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
		assertEquals("Incorrect model distribution 1", modelDist3, elemDist1
				.getModelDecomposition());
		assertEquals("Incorrect model distribution 2", modelDist3, elemDist2
				.getModelDecomposition());
		assertEquals("Incorrect model distribution 3", modelDist3, elemDist3
				.getModelDecomposition());
	}
	
	/**
	 * Test method for {@link ISubModel#getMachineRoot()}.
	 */
	@Test
	public void testGetMachineRoot() {
		assertEquals("Incorrect machine root 1", mch1_3, elemDist1
				.getMachineRoot());
		assertEquals("Incorrect machine root 2", mch1_3, elemDist2
				.getMachineRoot());
		assertEquals("Incorrect machine root 3", mch1_3, elemDist3
				.getMachineRoot());
	}
	
	/**
	 * Test method for {@link ISubModel#setProjectName(String)} and
	 * {@link ISubModel#getProjectName()}.
	 */
	@Test
	public void testSetAndGetProjectName() {
		ISubModel elemDist = new SubModel(modelDist3);
		testSetAndGetProjectName(elemDist, "ProjectA");
		testSetAndGetProjectName(elemDist, "ProjectB");
		testSetAndGetProjectName(elemDist, "ProjectC");
	}
	
	/**
	 * Utility method to test
	 * {@link ISubModel#setProjectName(String)} and
	 * {@link ISubModel#getProjectName()}.
	 * 
	 * @param elemDist an element distribution.
	 * @param prjName the name of the project.
	 */
	private void testSetAndGetProjectName(ISubModel elemDist,
			String prjName) {
		elemDist.setProjectName(prjName);
		assertEquals("Incorrect project name", prjName, elemDist
				.getProjectName());	
	}

	/**
	 * Test method for {@link ISubModel#setEvents(String...)}
	 * and {@link ISubModel#getEvents()}.
	 */
	@Test
	public void testSetAndGetEventLabels() {
		ISubModel elemDist = new SubModel(modelDist3);
		
		testSetAndGetEventLabels(elemDist, "evt1_3_1", "evt1_3_2");
		testSetAndGetEventLabels(elemDist, "evt1_3_1");
		testSetAndGetEventLabels(elemDist, "evt1_3_2", "evt1_3_3");
		testSetAndGetEventLabels(elemDist, "evt1_3_1", "evt1_3_5");
		testSetAndGetEventLabels(elemDist, "evt1_3_4", "evt1_3_2");
		testSetAndGetEventLabels(elemDist, "evt1_3_1", "evt1_3_2", "evt1_3_3");		
	}

	/**
	 * Utility method to test
	 * {@link ISubModel#setEvents(String...)} and
	 * {@link ISubModel#getEvents()}.
	 * 
	 * @param elemDist
	 *            an element distribution.
	 * @param evtLabels
	 *            an array of event labels.
	 */
	private void testSetAndGetEventLabels(ISubModel elemDist,
			String... evtLabels) {
		elemDist.setEvents(evtLabels);
		String[] actualEvtLabels = elemDist.getEvents();
		assertEquals("Incorrect event labels", evtLabels, actualEvtLabels);
	}

	/**
	 * Test method for {@link ISubModel#getAccessedVariables()}.
	 */
	@Test
	public void testGetAccessedVariables() {
		ISubModel elemDist;

		elemDist = modelDist1.createSubModel();
		elemDist.setEvents("evt1_1_1", "evt1_1_2");
		testGetAccessedVariables("Calculate accessed variables 1", elemDist,
				"x", "y");

		elemDist = modelDist1.createSubModel();
		elemDist.setEvents("evt1_1_3");
		testGetAccessedVariables("Calculate accessed variables 2", elemDist,
				"x", "y");

		elemDist = modelDist2.createSubModel();
		elemDist.setEvents("evt1_2_1", "evt1_2_3");
		testGetAccessedVariables("Calculate accessed variables 3", elemDist,
				"x", "y", "v");

		elemDist = modelDist2.createSubModel();
		elemDist.setEvents("evt1_2_2", "evt1_2_4");
		testGetAccessedVariables("Calculate accessed variables 4", elemDist,
				"x", "y", "u", "v");

		elemDist = modelDist3.createSubModel();
		elemDist.setEvents("evt1_3_1", "evt1_3_5");
		testGetAccessedVariables("Calculate accessed variables 5", elemDist,
				"z", "v");

		elemDist = modelDist3.createSubModel();
		elemDist.setEvents("evt1_3_2", "evt1_3_3");
		testGetAccessedVariables("Calculate accessed variables 6", elemDist,
				"u", "v", "y");

		elemDist = modelDist3.createSubModel();
		elemDist.setEvents("evt1_3_1");
		testGetAccessedVariables("Calculate accessed variables 7", elemDist,
				"z", "v");

		elemDist = modelDist3.createSubModel();
		elemDist.setEvents("evt1_3_2");
		testGetAccessedVariables("Calculate accessed variables 8", elemDist,
				"v", "y", "u");

		elemDist = modelDist3.createSubModel();
		elemDist.setEvents("evt1_3_3");
		testGetAccessedVariables("Calculate accessed variables 9", elemDist,
				"v", "y");

		elemDist = modelDist3.createSubModel();
		elemDist.setEvents("evt1_3_4");
		testGetAccessedVariables("Calculate accessed variables 10", elemDist,
				"u", "v", "p");

		elemDist = modelDist3.createSubModel();
		elemDist.setEvents("evt1_3_5");
		testGetAccessedVariables("Calculate accessed variables 11", elemDist,
				"z");

	}

	/**
	 * Utility method to test
	 * {@link ISubModel#getAccessedVariables()}.
	 * 
	 * @param message
	 *            a message.
	 * @param elemDist
	 *            an element distribution.
	 * @param expected
	 *            expected set of accessed variables (in {@link String}).
	 */
	private void testGetAccessedVariables(String message,
			ISubModel elemDist, String ... expected) {
		Set<String> vars;
		try {
			vars = elemDist.getAccessedVariables();
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
