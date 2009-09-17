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

package ch.ethz.eventb.internal.decomposition.tests.astyle.sc;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.eventb.decomposition.astyle.EventBAttributes;
import ch.ethz.eventb.decomposition.astyle.INatureElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement.Nature;
import ch.ethz.eventb.internal.decomposition.astyle.sc.DecompositionProblem;

/**
 * The class used to test the behavior of the static checker on refinements.
 */
public class MachineRefinesModuleSharedTest extends MachineRefinesModuleTest {

	private final static IProgressMonitor monitor = new NullProgressMonitor();

	private IEventBProject prj;
	private IMachineRoot abstractMachine;
	private IVariable abstractVariable;
	private IMachineRoot concreteMachine;
	private IEvent init;

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		prj = createRodinProject("P");
		abstractMachine = createMachine(prj, "m0");
		abstractVariable = createVariable(abstractMachine, "v0");
		INatureElement elt = (INatureElement) abstractVariable
				.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, monitor);
		createInvariant(abstractMachine, "inv0", "v0 ∈ ℕ", false);
		final IEvent init0 = createEvent(abstractMachine, IEvent.INITIALISATION);
		createAction(init0, "act0", "v0 ≔ 0");
		abstractMachine.getRodinFile().save(monitor, false);

		concreteMachine = createMachine(prj, "m1");
		createRefinesMachineClause(concreteMachine, "m0");
		init = createEvent(concreteMachine, IEvent.INITIALISATION);
		concreteMachine.getRodinFile().save(monitor, false);
	}

	/**
	 * Checks that an error is detected if a variable tagged as "shared" in the
	 * abstract machine is not present in the concrete machine.
	 */
	@Test
	public void testSharedVariableDoesNotDisappear() throws Exception {
		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(concreteMachine, null,
				DecompositionProblem.VariableHasDisappearedError,
				abstractVariable.getIdentifierString());
	}

	/**
	 * Checks that an error is detected if a variable tagged as "shared" in the
	 * abstract machine is not "shared" in the concrete machine.
	 */
	@Test
	public void testSharedVariableRemainsShared() throws Exception {
		final IVariable concreteVariable = createVariable(concreteMachine, "v0");
		createInvariant(concreteMachine, "inv0", "v0 ∈ ℕ", false);
		createAction(init, "act0", "v0 ≔ 0");
		concreteMachine.getRodinFile().save(monitor, false);

		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(concreteVariable, EventBAttributes.NATURE_ATTRIBUTE,
				DecompositionProblem.VariableInvalidNatureError,
				concreteVariable.getIdentifierString());
	}

	/**
	 * Checks that no error is detected if a variable tagged as "shared" in the
	 * abstract machine is always present and "shared" in the concrete machine.
	 */
	@Test
	public void testSharedVariable() throws Exception {
		final IVariable concreteVariable = createVariable(concreteMachine, "v0");
		INatureElement elt = (INatureElement) concreteVariable
				.getAdapter(INatureElement.class);
		elt.setNature(Nature.SHARED, monitor);
		createInvariant(concreteMachine, "inv0", "v0 ∈ ℕ", false);
		createAction(init, "act0", "v0 ≔ 0");
		concreteMachine.getRodinFile().save(monitor, false);

		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		containsMarkers(concreteMachine, false);
	}
}
