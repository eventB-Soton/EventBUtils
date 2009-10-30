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

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IAction;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IGuard;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ISeesContext;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.junit.Test;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.IDecomposedElement;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;

/**
 * @author htson
 *         <p>
 *         A test class for utility methods in {@link #EventBUtils}.
 *         </p>
 */
public class EventBUtilsTests extends EventBTests {

	/**
	 * Test method for
	 * {@link EventBUtils#createProject(String, org.eclipse.core.runtime.IProgressMonitor)}
	 */
	@Test
	public void testCreateProject() {
		IEventBProject project;

		// Test create project when the project already exists.
		try {
			project = EventBUtils
					.createProject("P1", new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception throws");
			return;
		}
		assertNotNull("Creating of the project named P1 should be succesful",
				project);
		assertTrue("The project must exist", project.getRodinProject().exists());
		final IRodinDB rodinDB = RodinCore.getRodinDB();
		final IRodinProject rodinProject = rodinDB.getRodinProject("P1");
		assertEquals("Project should be named P1", project.getRodinProject(),
				rodinProject);

		// Test create project successfully.
		try {
			project = EventBUtils.createProject("P100",
					new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception throws");
			return;
		}
		assertNotNull("Creating of the project named P100 should be succesful",
				project);
		assertTrue("The project must exist", project.getRodinProject().exists());
		assertEquals("Project should be named P100", project.getRodinProject()
				.getElementName(), "P100");
	}

	/**
	 * Test method for
	 * {@link EventBUtils#copyContexts(IEventBProject, IEventBProject, org.eclipse.core.runtime.IProgressMonitor)}
	 */
	@Test
	public void testCopyContexts() {
		try {
			EventBUtils.copyContexts(P1, P2, new NullProgressMonitor());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}

		IContextRoot[] originalContexts;
		try {
			originalContexts = P1.getRodinProject().getRootElementsOfType(
					IContextRoot.ELEMENT_TYPE);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}

		// Checking for each of the original context, there exists a
		// corresponding one in the target project.
		for (IContextRoot context : originalContexts) {
			IContextRoot testContext = P2.getContextRoot(context
					.getElementName());
			IDecomposedElement elt = (IDecomposedElement) testContext
					.getAdapter(IDecomposedElement.class);
			assertTrue("The context should exist", testContext.exists());
			try {
				assertTrue("The context should be tagged as generated",
						testContext.isGenerated());
				assertTrue("The context should be tagged as decomposed", elt
						.isDecomposed());
			} catch (RodinDBException e) {
				e.printStackTrace();
				fail("There should be no exception");
				return;
			}
		}
	}

	/**
	 * Test method for
	 * {@link EventBUtils#createMachine(IEventBProject, String, org.eclipse.core.runtime.IProgressMonitor)}
	 */
	@Test
	public void testCreateMachine() {
		IMachineRoot machine;
		String bareName = "test0001";
		boolean generated = false;
		boolean decomposed = false;
		try {
			machine = EventBUtils.createMachine(P1, EventBPlugin
					.getMachineFileName(bareName), new NullProgressMonitor());
			IDecomposedElement elt = (IDecomposedElement) machine
					.getAdapter(IDecomposedElement.class);
			decomposed = elt.isDecomposed();
			generated = machine.isGenerated();
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
		assertTrue("The machine should exist", machine.exists());
		assertTrue("The machine should be tagged as generated", generated);
		assertTrue("The machine should be tagged as decomposed", decomposed);
		assertEquals("The machine should belong to project P1", machine
				.getEventBProject(), P1);
		assertEquals("The name of the file must be consistent", EventBPlugin
				.getComponentName(machine.getElementName()), bareName);
	}

	/**
	 * Test method for
	 * {@link EventBUtils#copySeesClauses(IMachineRoot, IMachineRoot, org.eclipse.core.runtime.IProgressMonitor)}
	 */
	@Test
	public void testCopySeesClause() {
		testCopySeesClause(mch1_1, mch2_1);
		testCopySeesClause(mch1_2, mch3_1);
		testCopySeesClause(mch1_2, mch2_1);
		testCopySeesClause(mch1_1, mch3_1);
	}

	/**
	 * Utility method to test
	 * {@link EventBUtils#copySeesClauses(IMachineRoot, IMachineRoot, org.eclipse.core.runtime.IProgressMonitor)}
	 * by copying the SEES clauses from a source machine to a destination
	 * machine.
	 * 
	 * @param src
	 *            source machine.
	 * @param des
	 *            destination machine.
	 */
	private static void testCopySeesClause(IMachineRoot src, IMachineRoot des) {
		try {
			EventBUtils.copySeesClauses(src, des, new NullProgressMonitor());
			ISeesContext[] original = src.getSeesClauses();
			ISeesContext[] copy = des.getSeesClauses();
			for (ISeesContext originalSeesContext : original) {
				String seenContextName = originalSeesContext
						.getSeenContextName();
				boolean found = false;
				for (ISeesContext copySeesContext : copy) {
					if (copySeesContext.getSeenContextName().equals(
							seenContextName)) {
						found = true;
						break;
					}
				}
				if (!found)
					fail("Cannot find the see context for " + seenContextName
							+ " when copying from " + src + " to " + des);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for {@link EventBUtils#getDisplayedText(IRodinElement)}.
	 */
	@Test
	public void testGetDisplayText() {
		// Test guards.
		testGetDisplayText("Get guard display text 1", grd1_1_2_1, "y ≠ 0");
		testGetDisplayText("Get guard display text 2", grd1_1_3_1, "s ∈ S");
		testGetDisplayText("Get guard display text 3", grd1_1_3_2, "y ≥ 5");
		testGetDisplayText("Get guard display text 4", grd1_2_1_1, "y = 0");
		testGetDisplayText("Get guard display text 5", grd1_2_2_1, "u = f");
		testGetDisplayText("Get guard display text 6", grd1_2_3_1, "y > v");
		testGetDisplayText("Get guard display text 7", grd1_2_4_1, "v ≥ 3");
		testGetDisplayText("Get guard display text 8", grd1_3_1_1, "z = 0");
		testGetDisplayText("Get guard display text 9", grd1_3_2_1, "y ≠ 0");
		testGetDisplayText("Get guard display text 10", grd1_3_2_2, "u = f");
		testGetDisplayText("Get guard display text 11", grd1_3_3_1, "t ≠ a");
		testGetDisplayText("Get guard display text 12", grd1_3_3_2, "y ≥ 5");
		testGetDisplayText("Get guard display text 13", grd1_3_3_3, "y > v");
		testGetDisplayText("Get guard display text 14", grd1_3_4_1, "r ∈ ℕ");
		testGetDisplayText("Get guard display text 15", grd1_3_4_2, "p(r) = g");
		testGetDisplayText("Get guard display text 16", grd1_3_5_1, "z ≥ 2");

		// Test actions.
		testGetDisplayText("Get action display text 1", act1_1_1_1,
				"x :∈ {a, b}");
		testGetDisplayText("Get action display text 2", act1_1_1_2, "y ≔ 0");
		testGetDisplayText("Get action display text 3", act1_1_2_1,
				"x, y :∣ x' = b ∧ y' = y + 2");
		testGetDisplayText("Get action display text 4", act1_1_3_1,
				"x :∈ S ∖ {s}");
		testGetDisplayText("Get action display text 5", act1_1_3_2,
				"y :∣ y' = y − 4");
		testGetDisplayText("Get action display text 6", act1_2_1_1,
				"x :∈ {a, b}");
		testGetDisplayText("Get action display text 7", act1_2_1_2, "v ≔ y + 2");
		testGetDisplayText("Get action display text 8", act1_2_2_1, "u ≔ e");
		testGetDisplayText("Get action display text 9", act1_2_2_2, "v ≔ v + 1");
		testGetDisplayText("Get action display text 10", act1_2_3_1,
				"v ≔ v + 1");
		testGetDisplayText("Get action display text 11", act1_2_4_1,
				"v, u ≔ v − 1, e");
		testGetDisplayText("Get action display text 12", act1_3_1_1, "v ≔ 2");
		testGetDisplayText("Get action display text 13", act1_3_2_1,
				"y ≔ y + 2");
		testGetDisplayText("Get action display text 14", act1_3_2_2, "u ≔ e");
		testGetDisplayText("Get action display text 15", act1_3_2_3,
				"v ≔ v + 1");
		testGetDisplayText("Get action display text 16", act1_3_3_1,
				"y :∣ y' = y − 4");
		testGetDisplayText("Get action display text 17", act1_3_3_2,
				"v ≔ v + 1");
		testGetDisplayText("Get action display text 18", act1_3_4_1, "p(r) ≔ h");
		testGetDisplayText("Get action display text 19", act1_3_5_1,
				"z ≔ z − 1");

		// Test invariants.
		testGetDisplayText("Get invariant display text 1", inv1_1_1, "x ∈ S");
		testGetDisplayText("Get invariant display text 2", inv1_1_2, "y ∈ ℕ");
		testGetDisplayText("Get invariant display text 3", inv1_1_3, "y ≥ 0");
		testGetDisplayText("Get invariant display text 4", inv1_2_1, "u ∈ U");
		testGetDisplayText("Get invariant display text 5", inv1_2_2, "v ∈ ℕ");
		testGetDisplayText("Get invariant display text 6", inv1_2_3, "v ≥ 0");
		testGetDisplayText("Get invariant display text 7", inv1_3_1,
				"z = 0 ⇒ y = 0");
		testGetDisplayText("Get invariant display text 8", inv1_3_2,
				"z = 0 ⇒ x = a");
		testGetDisplayText("Get invariant display text 9", inv1_3_3,
				"z ≠ 0 ⇒ x = b");
		testGetDisplayText("Get invariant display text 10", inv1_3_4, "x ≠ c");
		testGetDisplayText("Get invariant display text 11", inv1_3_5,
				"p ∈ ℕ → V");

		// Test labeled elements.
		testGetDisplayText("Get labeled element display text 1", evt1_1_1,
				"evt1_1_1");
		testGetDisplayText("Get labeled element display text 2", evt1_1_2,
				"evt1_1_2");
		testGetDisplayText("Get labeled element display text 3", evt1_1_3,
				"evt1_1_3");
		testGetDisplayText("Get labeled element display text 4", evt1_2_1,
				"evt1_2_1");
		testGetDisplayText("Get labeled element display text 5", evt1_2_2,
				"evt1_2_2");
		testGetDisplayText("Get labeled element display text 6", evt1_2_3,
				"evt1_2_3");
		testGetDisplayText("Get labeled element display text 7", evt1_2_4,
				"evt1_2_4");
		testGetDisplayText("Get labeled element display text 8", evt1_3_1,
				"evt1_3_1");
		testGetDisplayText("Get labeled element display text 9", evt1_3_2,
				"evt1_3_2");
		testGetDisplayText("Get labeled element display text 10", evt1_3_3,
				"evt1_3_3");
		testGetDisplayText("Get labeled element display text 11", evt1_3_4,
				"evt1_3_4");
		testGetDisplayText("Get labeled element display text 12", evt1_3_5,
				"evt1_3_5");

		// Test identifier elements.
		testGetDisplayText("Get identifier element display text 1", x, "x");
		testGetDisplayText("Get identifier element display text 1", y, "y");
		testGetDisplayText("Get identifier element display text 1", u, "u");
		testGetDisplayText("Get identifier element display text 1", v, "v");
		testGetDisplayText("Get identifier element display text 1", z, "z");
	}

	/**
	 * Utility method to test
	 * {@link EventBUtils#getDisplayedText(IRodinElement)}.
	 * 
	 * @param message
	 *            a message.
	 * @param elem
	 *            a RODIN element.
	 * @param expected
	 *            the expected display text.
	 */
	private static void testGetDisplayText(String message, IRodinElement elem,
			String expected) {
		String actual = EventBUtils.getDisplayedText(elem);
		assertEquals(message + ": Incorrect display text", expected, actual);
		return;
	}

	/**
	 * Test method for {@link EventBUtils#getFreeIdentifiers(IEvent)}.
	 */
	@Test
	public void testGetEventFreeIdentifiers() {
		testGetEventFreeIdentifiers("Event free identifiers 1", evt1_1_1, "x",
				"a", "b", "y");
		testGetEventFreeIdentifiers("Event free identifiers 2", evt1_1_2, "y",
				"x", "b");
		testGetEventFreeIdentifiers("Event free identifiers 3", evt1_1_3, "S",
				"y", "x");
		testGetEventFreeIdentifiers("Event free identifiers 4", evt1_2_1, "y",
				"x", "a", "b", "v");
		testGetEventFreeIdentifiers("Event free identifiers 5", evt1_2_2, "y",
				"u", "f", "x", "b", "e", "v");
		testGetEventFreeIdentifiers("Event free identifiers 6", evt1_2_3, "S",
				"y", "v", "x");
		testGetEventFreeIdentifiers("Event free identifiers 7", evt1_2_4, "v",
				"u", "e");
		testGetEventFreeIdentifiers("Event free identifiers 8", evt1_3_1, "z",
				"v");
		testGetEventFreeIdentifiers("Event free identifiers 9", evt1_3_2, "y",
				"u", "f", "e", "v");
		testGetEventFreeIdentifiers("Event free identifiers 10", evt1_3_3, "a",
				"y", "v");
		testGetEventFreeIdentifiers("Event free identifiers 11", evt1_3_4, "v",
				"p", "g", "u", "e", "h");
		testGetEventFreeIdentifiers("Event free identifiers 12", evt1_3_5, "z");
	}

	/**
	 * Utility method to test {@link EventBUtils#getFreeIdentifiers(IEvent)}.
	 * 
	 * @param message
	 *            a message.
	 * @param evt
	 *            an event.
	 * @param expected
	 *            an array of expected free identifiers (in {@link String}).
	 */
	private static void testGetEventFreeIdentifiers(String message, IEvent evt,
			String... expected) {
		try {
			List<String> freeIdents = EventBUtils.getFreeIdentifiers(evt);
			testFreeIdentifiers(message, freeIdents, expected);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method to compare two lists of free identifiers.
	 * 
	 * @param message
	 *            a message.
	 * @param actual
	 *            a set of of actual free identifiers.
	 * @param expected
	 *            an array of expected free identifiers (in {@link String}).
	 */
	private static void testFreeIdentifiers(String message, List<String> actual,
			String... expected) {
		assertEquals(message
				+ ": Incorrect number of expected free identfiers ",
				expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(message, expected[i], actual.get(i));
		}
	}

	/**
	 * Test method for {@link EventBUtils#flatten(IEvent)}.
	 */
	@Test
	public void testFlattenEvent() {
		try {
			IEvent evt;

			evt = EventBUtils.flatten(evt1_1_1);
			testEventSignature("Flatten event signature 1", evt, "evt1_1_1",
					false, Convergence.ORDINARY);
			testEventParameters("Flatten event parameters 1", evt);
			testEventGuards("Flatten event guards 1", evt);
			testEventWitnesses("Flatten event witnesses 1", evt);
			testEventActions("Flatten event actions 1", evt,
					"act1_1_1_1: x :∈ {a, b}", "act1_1_1_2: y ≔ 0");

			evt = EventBUtils.flatten(evt1_1_2);
			testEventSignature("Flatten event signature 2", evt, "evt1_1_2",
					false, Convergence.ORDINARY);
			testEventParameters("Flatten event parameters 2", evt);
			testEventGuards("Flatten event guards 2", evt,
					"grd1_1_2_1: y ≠ 0: false");
			testEventWitnesses("Flatten event witnesses 2", evt);
			testEventActions("Flatten event actions 2", evt,
					"act1_1_2_1: x, y :∣ x' = b ∧ y' = y + 2");

			evt = EventBUtils.flatten(evt1_1_3);
			testEventSignature("Flatten event signature 3", evt, "evt1_1_3",
					false, Convergence.ORDINARY);
			testEventParameters("Flatten event parameters 3", evt, "s");
			testEventGuards("Flatten event guards 3", evt,
					"grd1_1_3_1: s ∈ S: false", "grd1_1_3_2: y ≥ 5: false");
			testEventWitnesses("Flatten event witnesses 3", evt);
			testEventActions("Flatten event actions 3", evt,
					"act1_1_3_1: x :∈ S ∖ {s}", "act1_1_3_2: y :∣ y' = y − 4");

			evt = EventBUtils.flatten(evt1_2_1);
			testEventSignature("Flatten event signature 4", evt, "evt1_2_1",
					false, Convergence.ORDINARY, "evt1_1_1");
			testEventParameters("Flatten event parameters 4", evt);
			testEventGuards("Flatten event guards 4", evt,
					"grd1_2_1_1: y = 0: false");
			testEventWitnesses("Flatten event witnesses 4", evt);
			testEventActions("Flatten event actions 4", evt,
					"act1_2_1_1: x :∈ {a, b}", "act1_2_1_2: v ≔ y + 2");

			evt = EventBUtils.flatten(evt1_2_2);
			testEventSignature("Flatten event signature 5", evt, "evt1_2_2",
					false, Convergence.ORDINARY, "evt1_1_2");
			testEventParameters("Flatten event parameters 5", evt);
			testEventGuards("Flatten event guards 5", evt,
					"grd1_1_2_1: y ≠ 0: false", "grd1_2_2_1: u = f: false");
			testEventWitnesses("Flatten event witnesses 5", evt);
			testEventActions("Flatten event actions 5", evt,
					"act1_1_2_1: x, y :∣ x' = b ∧ y' = y + 2",
					"act1_2_2_1: u ≔ e", "act1_2_2_2: v ≔ v + 1");

			evt = EventBUtils.flatten(evt1_2_3);
			testEventSignature("Flatten event signature 6", evt, "evt1_2_3",
					false, Convergence.ORDINARY, "evt1_1_3");
			testEventParameters("Flatten event parameters 6", evt, "s");
			testEventGuards("Flatten event guards 6", evt,
					"grd1_1_3_1: s ∈ S: false", "grd1_1_3_2: y ≥ 5: false",
					"grd1_2_3_1: y > v: false");
			testEventWitnesses("Flatten event witnesses 6", evt);
			testEventActions("Flatten event actions 6", evt,
					"act1_1_3_1: x :∈ S ∖ {s}", "act1_1_3_2: y :∣ y' = y − 4",
					"act1_2_3_1: v ≔ v + 1");

			evt = EventBUtils.flatten(evt1_2_4);
			testEventSignature("Flatten event signature 7", evt, "evt1_2_4",
					false, Convergence.ORDINARY);
			testEventParameters("Flatten event parameters 7", evt);
			testEventGuards("Flatten event guards 7", evt,
					"grd1_2_4_1: v ≥ 3: false");
			testEventWitnesses("Flatten event witnesses 7", evt);
			testEventActions("Flatten event actions 7", evt,
					"act1_2_4_1: v, u ≔ v − 1, e");

			evt = EventBUtils.flatten(evt1_3_1);
			testEventSignature("Flatten event signature 8", evt, "evt1_3_1",
					false, Convergence.ORDINARY, "evt1_2_1");
			testEventParameters("Flatten event parameters 8", evt);
			testEventGuards("Flatten event guards 8", evt,
					"grd1_3_1_1: z = 0: false");
			testEventWitnesses("Flatten event witnesses 8", evt, "x': x' = x");
			testEventActions("Flatten event actions 8", evt,
					"act1_3_1_1: v ≔ 2");

			evt = EventBUtils.flatten(evt1_3_2);
			testEventSignature("Flatten event signature 9", evt, "evt1_3_2",
					false, Convergence.ORDINARY, "evt1_2_2");
			testEventParameters("Flatten event parameters 9", evt);
			testEventGuards("Flatten event guards 9", evt,
					"grd1_3_2_1: y ≠ 0: false", "grd1_3_2_2: u = f: false");
			testEventWitnesses("Flatten event witnesses 9", evt, "x': x' = x");
			testEventActions("Flatten event actions 9", evt,
					"act1_3_2_1: y ≔ y + 2", "act1_3_2_2: u ≔ e",
					"act1_3_2_3: v ≔ v + 1");

			evt = EventBUtils.flatten(evt1_3_3);
			testEventSignature("Flatten event signature 10", evt, "evt1_3_3",
					false, Convergence.ORDINARY, "evt1_2_3");
			testEventParameters("Flatten event parameters 10", evt, "t");
			testEventGuards("Flatten event guards 10", evt,
					"grd1_3_3_1: t ≠ a: false", "grd1_3_3_2: y ≥ 5: false",
					"grd1_3_3_3: y > v: false");
			testEventWitnesses("Flatten event witnesses 10", evt, "s: s = a",
					"x': x' = x");
			testEventActions("Flatten event actions 10", evt,
					"act1_3_3_1: y :∣ y' = y − 4", "act1_3_3_2: v ≔ v + 1");

			evt = EventBUtils.flatten(evt1_3_4);
			testEventSignature("Flatten event signature 11", evt, "evt1_3_4",
					false, Convergence.ORDINARY, "evt1_2_4");
			testEventParameters("Flatten event parameters 11", evt, "r");
			testEventGuards("Flatten event guards 11", evt,
					"grd1_2_4_1: v ≥ 3: false", "grd1_3_4_1: r ∈ ℕ: false",
					"grd1_3_4_2: p(r) = g: false");
			testEventWitnesses("Flatten event witnesses 11", evt);
			testEventActions("Flatten event actions 11", evt,
					"act1_2_4_1: v, u ≔ v − 1, e", "act1_3_4_1: p(r) ≔ h");

			evt = EventBUtils.flatten(evt1_3_5);
			testEventSignature("Flatten event signature 12", evt, "evt1_3_5",
					false, Convergence.ORDINARY);
			testEventParameters("Flatten event parameters 12", evt);
			testEventGuards("Flatten event guards 12", evt,
					"grd1_3_5_1: z ≥ 2: false");
			testEventWitnesses("Flatten event witnesses 12", evt);
			testEventActions("Flatten event actions 12", evt,
					"act1_3_5_1: z ≔ z − 1");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}

	}

	/**
	 * Test method for {@link EventBUtils#getFreeIdentifiers(IGuard)}.
	 */
	@Test
	public void testGetGuardFreeIdentifiers() {
		testGetGuardFreeIdentifiers("Guard free identifiers 1", grd1_1_2_1, "y");
		testGetGuardFreeIdentifiers("Guard free identifiers 2", grd1_1_3_1,
				"s", "S");
		testGetGuardFreeIdentifiers("Guard free identifiers 3", grd1_1_3_2, "y");
		testGetGuardFreeIdentifiers("Guard free identifiers 4", grd1_2_1_1, "y");
		testGetGuardFreeIdentifiers("Guard free identifiers 5", grd1_2_2_1,
				"u", "f");
		testGetGuardFreeIdentifiers("Guard free identifiers 6", grd1_2_3_1,
				"y", "v");
		testGetGuardFreeIdentifiers("Guard free identifiers 7", grd1_2_4_1, "v");
		testGetGuardFreeIdentifiers("Guard free identifiers 8", grd1_3_1_1, "z");
		testGetGuardFreeIdentifiers("Guard free identifiers 9", grd1_3_2_1, "y");
		testGetGuardFreeIdentifiers("Guard free identifiers 10", grd1_3_2_2,
				"u", "f");
		testGetGuardFreeIdentifiers("Guard free identifiers 11", grd1_3_3_1,
				"t", "a");
		testGetGuardFreeIdentifiers("Guard free identifiers 12", grd1_3_3_2,
				"y");
		testGetGuardFreeIdentifiers("Guard free identifiers 13", grd1_3_3_3,
				"y", "v");
		testGetGuardFreeIdentifiers("Guard free identifiers 14", grd1_3_4_1,
				"r");
		testGetGuardFreeIdentifiers("Guard free identifiers 15", grd1_3_4_2,
				"p", "r", "g");
		testGetGuardFreeIdentifiers("Guard free identifiers 16", grd1_3_5_1,
				"z");
	}

	/**
	 * Utility method to test {@link EventBUtils#getFreeIdentifiers(IGuard)} .
	 * 
	 * @param message
	 *            a message.
	 * @param grd
	 *            a guard.
	 * @param expected
	 *            expected set of free identifiers.
	 */
	private static void testGetGuardFreeIdentifiers(String message, IGuard grd,
			String... expected) {
		try {
			List<String> freeIdents = EventBUtils.getFreeIdentifiers(grd);
			testFreeIdentifiers(message, freeIdents, expected);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for {@link EventBUtils#getPredicateFreeIdentifiers(String)}.
	 */
	@Test
	public void testGetPredicateFreeIdentifiers() {
		testGetPredicateFreeIdentifiers("Predicate free identifiers 1",
				"x ∈ ℕ", "x");
		testGetPredicateFreeIdentifiers("Predicate free identifiers 2",
				"x ∈ ℕ ∧ y ∈ ℕ ∧ x = y + 1", "x", "y");
		testGetPredicateFreeIdentifiers("Predicate free identifiers 3",
				"x ∈ ℕ ∧ y ∈ ℕ ∧ x = y + 1 ∧ (∃z·z = y − 1)", "x", "y");
	}

	/**
	 * Utility method to test
	 * {@link EventBUtils#getPredicateFreeIdentifiers(String)}.
	 * 
	 * @param message
	 *            a message.
	 * @param predicateString
	 *            a predicate string.
	 * @param expected
	 *            expected set of free identifiers.
	 */
	private static void testGetPredicateFreeIdentifiers(String message,
			String predicateString, String... expected) {
		List<String> freeIdents = EventBUtils
				.getPredicateFreeIdentifiers(predicateString);
		testFreeIdentifiers(message, freeIdents, expected);
	}

	/**
	 * Test method for {@link EventBUtils#getFreeIdentifiers(IAction)}.
	 */
	@Test
	public void testGetActionFreeIdentifiers() {
		testGetActionFreeIdentifiers("Action free identifiers 1", act1_1_1_1,
				"x", "a", "b");
		testGetActionFreeIdentifiers("Action free identifiers 2", act1_1_1_2,
				"y");
		testGetActionFreeIdentifiers("Action free identifiers 3", act1_1_2_1,
				"x", "y", "b");
		testGetActionFreeIdentifiers("Action free identifiers 4", act1_1_3_1,
				"x", "S", "s");
		testGetActionFreeIdentifiers("Action free identifiers 5", act1_1_3_2,
				"y");
		testGetActionFreeIdentifiers("Action free identifiers 6", act1_2_1_1,
				"x", "a", "b");
		testGetActionFreeIdentifiers("Action free identifiers 7", act1_2_1_2,
				"v", "y");
		testGetActionFreeIdentifiers("Action free identifiers 8", act1_2_2_1,
				"u", "e");
		testGetActionFreeIdentifiers("Action free identifiers 9", act1_2_2_2,
				"v");
		testGetActionFreeIdentifiers("Action free identifiers 10", act1_2_3_1,
				"v");
		testGetActionFreeIdentifiers("Action free identifiers 11", act1_2_4_1,
				"v", "u", "e");
		testGetActionFreeIdentifiers("Action free identifiers 13", act1_3_1_1,
				"v");
		testGetActionFreeIdentifiers("Action free identifiers 14", act1_3_2_1,
				"y");
		testGetActionFreeIdentifiers("Action free identifiers 15", act1_3_2_2,
				"u", "e");
		testGetActionFreeIdentifiers("Action free identifiers 16", act1_3_2_3,
				"v");
		testGetActionFreeIdentifiers("Action free identifiers 17", act1_3_3_1,
				"y");
		testGetActionFreeIdentifiers("Action free identifiers 18", act1_3_3_2,
				"v");
		testGetActionFreeIdentifiers("Action free identifiers 19", act1_3_4_1,
				"p", "r", "h");
		testGetActionFreeIdentifiers("Action free identifiers 20", act1_3_5_1,
				"z");
	}

	/**
	 * Utility method to test {@link EventBUtils#getFreeIdentifiers(IAction)}.
	 * 
	 * @param message
	 *            a message.
	 * @param act
	 *            an action.
	 * @param expected
	 *            expected set of identifiers.
	 */
	private static void testGetActionFreeIdentifiers(String message, IAction act,
			String... expected) {
		try {
			List<String> freeIdents = EventBUtils.getFreeIdentifiers(act);
			testFreeIdentifiers(message, freeIdents, expected);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for {@link EventBUtils#getAssignmentFreeIdentifiers(String)}.
	 */
	@Test
	public void testGetAssignmentFreeIdentifiers() {
		testGetAssignmentFreeIdentifiers("Assignment free identifiers 1",
				"x ≔ x + 1", "x");
		testGetAssignmentFreeIdentifiers("Assignment free identifiers 2",
				"y ≔ x + 1", "y", "x");
		testGetAssignmentFreeIdentifiers("Assignment free identifiers 3",
				"z :∈ {t + 1, y}", "z", "t", "y");
		testGetAssignmentFreeIdentifiers("Assignment free identifiers 4",
				"v, w :∣ v' = t + 1 ∨ (∃z·z = w' + 1)", "v", "w", "t");
	}

	/**
	 * Utility method to test
	 * {@link EventBUtils#getAssignmentFreeIdentifiers(String)}.
	 * 
	 * @param message
	 *            a message.
	 * @param assignmentString
	 *            an assignment string.
	 * @param expected
	 *            expected set of free identifiers.
	 */
	private static void testGetAssignmentFreeIdentifiers(String message,
			String assignmentString, String... expected) {
		List<String> freeIdents = EventBUtils
				.getAssignmentFreeIdentifiers(assignmentString);
		testFreeIdentifiers(message, freeIdents, expected);
	}

	private static void assertSeenSetsAndConstants(Set<String> idents,
			String... expected) {
		assertSameStrings("Seen sets and constants", "identifier", idents, expected);
	}

	/**
	 * Test method for
	 * {@link EventBUtils#getSeenCarrierSetsAndConstants(IMachineRoot)}.
	 */
	@Test
	public void testGetMachineSeenCarrierSetsAndConstants() {
		testGetMachineSeenCarrierSetsAndConstants(
				"Context constants and sets 1", mch1_1, "S", "T", "a", "b",
				"c", "d");
		testGetMachineSeenCarrierSetsAndConstants(
				"Context constants and sets 2", mch1_2, "S", "T", "a", "b",
				"c", "d", "U", "e", "f");
		testGetMachineSeenCarrierSetsAndConstants(
				"Context constants and sets 3", mch1_3, "S", "T", "U", "V",
				"a", "b", "c", "d", "e", "f", "g", "h");
	}

	/**
	 * Utility method to test
	 * {@link EventBUtils#getSeenCarrierSetsAndConstants(IMachineRoot)}.
	 * 
	 * @param message
	 *            a machine.
	 * @param mch
	 *            a machine.
	 * @param expected
	 *            expected set of carrier sets and constants (in {@link String}
	 *            ).
	 */
	private static void testGetMachineSeenCarrierSetsAndConstants(String message,
			IMachineRoot mch, String... expected) {
		try {
			Set<String> idents = EventBUtils
					.getSeenCarrierSetsAndConstants(mch);
			assertSeenSetsAndConstants(idents, expected);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}

	}

	/**
	 * Test method for
	 * {@link EventBUtils#getCarrierSetsAndConstants(IContextRoot)}.
	 */
	@Test
	public void testGetContextCarrierSetsAndConstants() {
		testGetContextCarrierSetsAndConstants("Context constants and sets 1",
				ctx1_1, "S", "T", "a", "b", "c", "d");
		testGetContextCarrierSetsAndConstants("Context constants and sets 2",
				ctx1_2, "U", "e", "f");
		testGetContextCarrierSetsAndConstants("Context constants and sets 3",
				ctx1_3, "S", "T", "U", "V", "a", "b", "c", "d", "e", "f", "g",
				"h");
	}

	/**
	 * Utility method to test
	 * {@link EventBUtils#getCarrierSetsAndConstants(IContextRoot)}.
	 * 
	 * @param message
	 *            a message.
	 * @param ctx
	 *            a context.
	 * @param expected
	 *            expected set of carrier sets and constants.
	 */
	private static void testGetContextCarrierSetsAndConstants(String message,
			IContextRoot ctx, String... expected) {
		try {
			Set<String> idents = EventBUtils.getCarrierSetsAndConstants(ctx);
			assertSeenSetsAndConstants(idents, expected);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}

	}

	/**
	 * Test method for {@link EventBUtils#getFreeIdentifiers(IInvariant)}.
	 */
	@Test
	public void testGetInvariantFreeIdentifiers() {
		testGetInvariantFreeIdentifiers("Invariant free identifiers 1",
				inv1_1_1, "x", "S");
		testGetInvariantFreeIdentifiers("Invariant free identifiers 2",
				inv1_1_2, "y");
		testGetInvariantFreeIdentifiers("Invariant free identifiers 3",
				inv1_1_3, "y");
		testGetInvariantFreeIdentifiers("Invariant free identifiers 4",
				inv1_2_1, "u", "U");
		testGetInvariantFreeIdentifiers("Invariant free identifiers 5",
				inv1_2_2, "v");
		testGetInvariantFreeIdentifiers("Invariant free identifiers 6",
				inv1_2_3, "v");
		testGetInvariantFreeIdentifiers("Invariant free identifiers 7",
				inv1_3_1, "z", "y");
		testGetInvariantFreeIdentifiers("Invariant free identifiers 8",
				inv1_3_2, "z", "x", "a");
		testGetInvariantFreeIdentifiers("Invariant free identifiers 9",
				inv1_3_3, "z", "x", "b");
		testGetInvariantFreeIdentifiers("Invariant free identifiers 10",
				inv1_3_4, "x", "c");
		testGetInvariantFreeIdentifiers("Invariant free identifiers 11",
				inv1_3_5, "p", "V");
	}

	/**
	 * Utility method to test {@link EventBUtils#getFreeIdentifiers(IInvariant)}
	 * .
	 * 
	 * @param message
	 *            a message.
	 * @param inv
	 *            an invariant
	 * @param expected
	 *            expected set of free identifiers.
	 */
	private static void testGetInvariantFreeIdentifiers(String message,
			IInvariant inv, String... expected) {
		try {
			List<String> freeIdents = EventBUtils.getFreeIdentifiers(inv);
			testFreeIdentifiers(message, freeIdents, expected);
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Test method for
	 * {@link EventBUtils#identsToCSVString(String, FreeIdentifier[])}.
	 */
	@Test
	public void testIdentsToCSVString() {
		String srcStr;
		try {
			srcStr = act1_1_1_1.getAssignmentString();
			Assignment parseAssignment = Lib.parseAssignment(srcStr);
			testIdentsToCSVString("Idents to CSV string 1", srcStr,
					parseAssignment, "a, b, x");

			srcStr = grd1_1_2_1.getPredicateString();
			Predicate parsePredicate = Lib.parsePredicate(srcStr);
			testIdentsToCSVString("Idents to CSV string 2", srcStr,
					parsePredicate, "y");

			srcStr = act1_1_2_1.getAssignmentString();
			parseAssignment = Lib.parseAssignment(srcStr);
			testIdentsToCSVString("Idents to CSV string 3", srcStr,
					parseAssignment, "b, x, y");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method to test
	 * {@link EventBUtils#identsToCSVString(String, FreeIdentifier[])}.
	 * 
	 * @param message
	 *            a message.
	 * @param srcStr
	 *            the source string.
	 * @param formula
	 *            a formula corresponding to the source string.
	 * @param expected
	 *            expected CSV string.
	 */
	private static void testIdentsToCSVString(String message, String srcStr,
			Formula<? extends Formula<?>> formula, String expected) {
		FreeIdentifier[] idents = formula.getFreeIdentifiers();
		assertEquals(message + ": Incorrect CSV string", expected, EventBUtils
				.identsToCSVString(srcStr, idents));
	}

	/**
	 * Test method for
	 * {@link EventBUtils#identsToPrimedCSVString(String, FreeIdentifier[])}.
	 */
	@Test
	public void testIdentsToPrimedCSVString() {
		String srcStr;
		try {
			srcStr = act1_1_1_1.getAssignmentString();
			Assignment parseAssignment = Lib.parseAssignment(srcStr);
			testIdentsToPrimedCSVString("Idents to CSV string 1", srcStr,
					parseAssignment, "a', b', x'");

			srcStr = grd1_1_2_1.getPredicateString();
			Predicate parsePredicate = Lib.parsePredicate(srcStr);
			testIdentsToPrimedCSVString("Idents to CSV string 2", srcStr,
					parsePredicate, "y'");

			srcStr = act1_1_2_1.getAssignmentString();
			parseAssignment = Lib.parseAssignment(srcStr);
			testIdentsToPrimedCSVString("Idents to CSV string 3", srcStr,
					parseAssignment, "b', x', y'");
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method to test
	 * {@link EventBUtils#identsToPrimedCSVString(String, FreeIdentifier[])}.
	 * 
	 * @param message
	 *            a message.
	 * @param srcStr
	 *            the source string.
	 * @param formula
	 *            a formula corresponding to the source string.
	 * @param expected
	 *            expected CSV string.
	 */
	private static void testIdentsToPrimedCSVString(String message, String srcStr,
			Formula<? extends Formula<?>> formula, String expected) {
		FreeIdentifier[] idents = formula.getFreeIdentifiers();
		assertEquals(message + ": Incorrect CSV string", expected, EventBUtils
				.identsToPrimedCSVString(srcStr, idents));
	}

	/**
	 * Test method for {@link EventBUtils#getInitialisation(IMachineRoot)}.
	 */
	@Test
	public void testGetInitialisation() {
		try {
			assertEquals("Incorrect get initialisation 1", init1_1, EventBUtils
					.getInitialisation(mch1_1));
			assertEquals("Incorrect get initialisation 2", init1_2, EventBUtils
					.getInitialisation(mch1_2));
			assertEquals("Incorrect get initialisation 3", init1_3, EventBUtils
					.getInitialisation(mch1_3));
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	@Test
	public void testGetEventWithLabel() {
		try {
			assertNull("Get event with incorrect label 1", EventBUtils
					.getEventWithLabel(mch1_1, "evt2_1_1"));
			assertNull("Get event with incorrect label 2", EventBUtils
					.getEventWithLabel(mch1_2, "evt3_1_1"));
			assertNull("Get event with incorrect label 3", EventBUtils
					.getEventWithLabel(mch1_3, "evt1_1_1"));

			assertEquals("Get event with correct label 1", evt1_1_1,
					EventBUtils.getEventWithLabel(mch1_1, "evt1_1_1"));
			assertEquals("Get event with correct label 2", evt1_1_2,
					EventBUtils.getEventWithLabel(mch1_1, "evt1_1_2"));
			assertEquals("Get event with correct label 3", evt1_1_3,
					EventBUtils.getEventWithLabel(mch1_1, "evt1_1_3"));
			assertEquals("Get event with correct label 4", evt1_2_1,
					EventBUtils.getEventWithLabel(mch1_2, "evt1_2_1"));
			assertEquals("Get event with correct label 5", evt1_2_2,
					EventBUtils.getEventWithLabel(mch1_2, "evt1_2_2"));
			assertEquals("Get event with correct label 6", evt1_2_3,
					EventBUtils.getEventWithLabel(mch1_2, "evt1_2_3"));
			assertEquals("Get event with correct label 7", evt1_2_4,
					EventBUtils.getEventWithLabel(mch1_2, "evt1_2_4"));
			assertEquals("Get event with correct label 8", evt1_3_1,
					EventBUtils.getEventWithLabel(mch1_3, "evt1_3_1"));
			assertEquals("Get event with correct label 8", evt1_3_1,
					EventBUtils.getEventWithLabel(mch1_3, "evt1_3_1"));
			assertEquals("Get event with correct label 9", evt1_3_2,
					EventBUtils.getEventWithLabel(mch1_3, "evt1_3_2"));
			assertEquals("Get event with correct label 10", evt1_3_3,
					EventBUtils.getEventWithLabel(mch1_3, "evt1_3_3"));
			assertEquals("Get event with correct label 11", evt1_3_4,
					EventBUtils.getEventWithLabel(mch1_3, "evt1_3_4"));
			assertEquals("Get event with correct label 12", evt1_3_5,
					EventBUtils.getEventWithLabel(mch1_3, "evt1_3_5"));
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

}
