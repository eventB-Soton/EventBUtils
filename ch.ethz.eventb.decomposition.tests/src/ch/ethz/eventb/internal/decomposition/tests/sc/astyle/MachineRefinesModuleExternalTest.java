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
package ch.ethz.eventb.internal.decomposition.tests.sc.astyle;

import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IMachineRoot;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.eventb.decomposition.astyle.AStyleAttributes;
import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.internal.decomposition.sc.astyle.DecompositionProblem;
import ch.ethz.eventb.internal.decomposition.tests.AbstractSCTests;

import static org.eventb.core.EventBAttributes.EXTENDED_ATTRIBUTE;

/**
 * The class used to test the behavior of the static checker on refinements.
 */
public class MachineRefinesModuleExternalTest extends AbstractSCTests {

	private IEventBProject prj;
	private IMachineRoot abstractMachine;
	private IEvent abstractEvent;
	private IMachineRoot concreteMachine;
	private IEvent init;

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		prj = createRodinProject("P");
		abstractMachine = createMachine(prj, "m0");
		createEvent(abstractMachine, IEvent.INITIALISATION);
		abstractEvent = createEvent(abstractMachine, "evt0");
		IExternalElement elt = (IExternalElement) abstractEvent
				.getAdapter(IExternalElement.class);
		elt.setExternal(true, monitor);
		abstractMachine.getRodinFile().save(monitor, false);

		concreteMachine = createMachine(prj, "m1");
		createRefinesMachineClause(concreteMachine, "m0");
		init = createEvent(concreteMachine, IEvent.INITIALISATION);
		concreteMachine.getRodinFile().save(monitor, false);
	}

	/**
	 * Checks that an error is detected if an event tagged as "external" in the
	 * abstract machine is not present in the concrete machine.
	 */
	@Test
	public void testExternalEventDoesNotDisappear() throws Exception {
		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(concreteMachine, null,
				DecompositionProblem.EventHasDisappearedError, abstractEvent
						.getLabel());
	}

	/**
	 * Checks that an error is detected if an event tagged as "external" in the
	 * abstract machine is not "external" in the concrete machine.
	 */
	@Test
	public void testExternalEventRemainsExternal() throws Exception {
		final IEvent concreteEvent = createEvent(false, true);
		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(concreteEvent, AStyleAttributes.EXTERNAL_ATTRIBUTE,
				DecompositionProblem.EventInvalidStatusError, concreteEvent
						.getLabel(), AStyleAttributes.EXTERNAL_ATTRIBUTE
						.getName());
	}

	/**
	 * Checks that an error is detected if an event tagged as "external" in the
	 * abstract machine is "extended" in the concrete machine.
	 */
	@Test
	public void testExternalEventIsExtended() throws Exception {
		final IEvent concreteEvent = createEvent(true, false);
		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(concreteEvent, EXTENDED_ATTRIBUTE,
				DecompositionProblem.EventInvalidStatusError, concreteEvent
						.getLabel(), EXTENDED_ATTRIBUTE.getName());
	}

	/**
	 * Checks that an error is detected if an event tagged as "external" in the
	 * abstract machine is refined in the concrete machine by an event defining
	 * additional guards.
	 */
	@Test
	public void testAdditionalGuardsInExternalEvent() throws Exception {
		final IEvent concreteEvent = createEvent(true, true);
		createGuard(concreteEvent, "grd", "⊤");
		concreteMachine.getRodinFile().save(monitor, false);

		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(concreteEvent, null,
				DecompositionProblem.GuardsInExternalEventError);
	}

	/**
	 * Checks that an error is detected if an event tagged as "external" in the
	 * abstract machine is refined in the concrete machine by an event defining
	 * additional parameters.
	 */
	@Test
	public void testAdditionalParametersInExternalEvent() throws Exception {
		final IEvent concreteEvent = createEvent(true, true);
		createParameter(concreteEvent, "prm");
		createGuard(concreteEvent, "grd", "prm ∈ ℕ");
		concreteMachine.getRodinFile().save(monitor, false);

		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(concreteEvent, null,
				DecompositionProblem.ParametersInExternalEventError);
		hasMarker(concreteEvent, null,
				DecompositionProblem.GuardsInExternalEventError);
	}

	/**
	 * Checks that an error is detected if an event tagged as "external" in the
	 * abstract machine is refined in the concrete machine by an event defining
	 * additional actions.
	 */
	@Test
	public void testAdditionalActionsInExternalEvent() throws Exception {
		createVariable(concreteMachine, "v0");
		createInvariant(concreteMachine, "inv0", "v0 ∈ ℕ", false);
		createAction(init, "act0", "v0 ≔ 0");
		final IEvent concreteEvent = createEvent(true, true);
		createAction(concreteEvent, "act0", "v0 ≔ 1");
		concreteMachine.getRodinFile().save(monitor, false);

		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		hasMarker(concreteEvent, null,
				DecompositionProblem.ActionsInExternalEventError);
	}

	/**
	 * Checks that no error is detected if an event tagged as "external" in the
	 * abstract machine is not refined in the concrete machine, <i>i.e.</i>:
	 * <ul>
	 * <li>The concrete machine defines an event with the same name.
	 * <li>This event is tagged as "external".
	 * <li>This event is tagged as "extended".
	 * <li>This event has a REFINES clause pointing to the event itself.
	 * <li>This event does not declare any additional element (parameter, guard,
	 * witness, action).
	 * </ul>
	 */
	@Test
	public void testExternalEvent() throws Exception {
		createEvent(true, true);
		runBuilder(prj);

		containsMarkers(abstractMachine, false);
		containsMarkers(concreteMachine, false);
	}

	private IEvent createEvent(boolean isExternal, boolean isExtended)
			throws Exception {
		final IEvent event = createEvent(concreteMachine, "evt0");
		createRefinesEventClause(event, "evt0");
		event.setExtended(isExtended, monitor);
		IExternalElement elt = (IExternalElement) event
				.getAdapter(IExternalElement.class);
		elt.setExternal(isExternal, monitor);
		concreteMachine.getRodinFile().save(monitor, false);
		return event;
	}
}
