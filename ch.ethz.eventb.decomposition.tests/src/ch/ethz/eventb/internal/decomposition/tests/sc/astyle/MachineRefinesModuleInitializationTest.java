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

package ch.ethz.eventb.internal.decomposition.tests.sc.astyle;

import org.eventb.core.IAction;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.eventb.decomposition.astyle.INatureElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement.Nature;
import ch.ethz.eventb.internal.decomposition.sc.astyle.DecompositionProblem;
import ch.ethz.eventb.internal.decomposition.tests.AbstractSCTests;

/**
 * The class used to test the behavior of the static checker on refinements.
 */
public class MachineRefinesModuleInitializationTest extends
		AbstractSCTests {

	private IEventBProject prj;
	private IMachineRoot abstractMachine;
	private IMachineRoot concreteMachine;
	private IEvent abstractInit;
	private IEvent concreteInit;
	private IVariable v0;
	private IVariable v1;

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		prj = createRodinProject("P");
		abstractMachine = createMachine(prj, "m0");
		abstractInit = createEvent(abstractMachine, IEvent.INITIALISATION);
		abstractMachine.getRodinFile().save(monitor, false);

		concreteMachine = createMachine(prj, "m1");
		createRefinesMachineClause(concreteMachine, "m0");
		v0 = createVariable(concreteMachine, "v0");
		v1 = createVariable(concreteMachine, "v1");
		createInvariant(concreteMachine, "inv0", "v0 ∈ ℕ", false);
		createInvariant(concreteMachine, "inv1", "v1 ∈ ℕ", false);
		concreteInit = createEvent(concreteMachine, IEvent.INITIALISATION);
		concreteMachine.getRodinFile().save(monitor, false);
	}

	/**
	 * Checks that an error is detected if the initialization event of a
	 * concrete machine defines an action modifying a "private" variable and a
	 * "shared" variable.
	 */
	@Test
	public void testPrivateAndSharedInInit() throws Exception {
		INatureElement elt = (INatureElement) v1
				.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, monitor);
		IAction action = createAction(concreteInit, "act0",
				"v0, v1 :∣ v0' = 0 ∧ v1' = 1");
		concreteMachine.getRodinFile().save(monitor, false);
		
		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(action, null,
				DecompositionProblem.ActionOnPrivateAndSharedError, action
						.getLabel());
	}

	/**
	 * Checks that no error is detected if the initialization event of a
	 * concrete machine defines an action only modifying "private" variables.
	 */
	@Test
	public void testOnlyPrivateInInit() throws Exception {
		IAction action = createAction(concreteInit, "act0",
				"v0, v1 :∣ v0' = 0 ∧ v1' = 1");
		concreteMachine.getRodinFile().save(monitor, false);
		
		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		containsMarkers(action, false);
	}

	/**
	 * Checks that no error is detected if the initialization event of a
	 * concrete machine defines an action only modifying "shared" variables.
	 */
	@Test
	public void testOnlySharedInInit() throws Exception {
		INatureElement elt = (INatureElement) v0
				.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, monitor);
		elt = (INatureElement) v1.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, monitor);
		IAction action = createAction(concreteInit, "act0",
				"v0, v1 :∣ v0' = 0 ∧ v1' = 1");
		concreteMachine.getRodinFile().save(monitor, false);
		
		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		containsMarkers(action, false);
	}

	/**
	 * Checks that an error is detected if the initialization event of an
	 * abstract machine defines an action modifying a "shared" variable which is
	 * not present any longer in the concrete machine.
	 */
	@Test
	public void testSharedNotPresentInInit() throws Exception {
		IVariable abstractVariable = createVariable(abstractMachine, "v0");
		INatureElement elt = (INatureElement) abstractVariable
				.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, monitor);
		createInvariant(abstractMachine, "inv0", "v0 ∈ ℕ", false);
		IAction action = createAction(abstractInit, "act0", "v0 ≔ 0");
		abstractMachine.getRodinFile().save(monitor, false);
		
		elt = (INatureElement) v0.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, monitor);
		concreteMachine.getRodinFile().save(monitor, false);
		
		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(concreteInit, null, DecompositionProblem.ActionOnSharedError,
				action.getLabel());
	}

	/**
	 * Checks that an error is detected if the initialization event of an
	 * abstract machine defines an action modifying a "shared" variable which is
	 * syntactically different in the concrete machine.
	 */
	@Test
	public void testSharedNotSameInInit() throws Exception {
		IVariable abstractVariable = createVariable(abstractMachine, "v0");
		INatureElement elt = (INatureElement) abstractVariable
				.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, monitor);
		createInvariant(abstractMachine, "inv0", "v0 ∈ ℕ", false);
		IAction action = createAction(abstractInit, "act0", "v0 ≔ 0");
		abstractMachine.getRodinFile().save(monitor, false);
		
		elt = (INatureElement) v0.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, monitor);
		createAction(concreteInit, "act0","v0, v1 :∣ v0' = 0 ∧ v1' = 1");
		concreteMachine.getRodinFile().save(monitor, false);
		
		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(concreteInit, null, DecompositionProblem.ActionOnSharedError,
				action.getLabel());
	}

}
