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

package ch.ethz.eventb.internal.decomposition.sc.astyle;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IRefinesMachine;
import org.eventb.core.ISCAction;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ISCVariable;
import org.eventb.core.IVariable;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.LanguageVersion;
import org.eventb.core.sc.SCCore;
import org.eventb.core.sc.SCProcessorModule;
import org.eventb.core.sc.state.ISCStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.DecompositionPlugin;
import ch.ethz.eventb.decomposition.astyle.AStyleAttributes;
import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement.Nature;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;

import static org.eventb.core.EventBAttributes.EXTENDED_ATTRIBUTE;
import static ch.ethz.eventb.decomposition.astyle.AStyleAttributes.NATURE_ATTRIBUTE;
import static ch.ethz.eventb.decomposition.astyle.AStyleAttributes.EXTERNAL_ATTRIBUTE;

public class MachineRefinesModule extends SCProcessorModule {

	private IMachineRoot machineRoot;
	private IRefinesMachine abstractMachine;
	private ISCMachineRoot scAbstractMachineRoot;
	private ITypeEnvironment typenv;
	private Map<String, IVariable> varMap;
	private Map<String, IEvent> evtMap;

	public static final IModuleType<MachineVariableModule> MODULE_TYPE = SCCore
			.getModuleType(DecompositionPlugin.PLUGIN_ID + ".refinesModule"); //$NON-NLS-1$

	public void process(IRodinElement element, IInternalElement target,
			ISCStateRepository repository, IProgressMonitor monitor)
			throws CoreException {
		if (scAbstractMachineRoot == null) {
			return;
		}
		fetchSCVariables();
		fetchSCEvents();
		fetchInit();
		fetchSCInit();
	}

	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

	@Override
	public void initModule(final IRodinElement element,
			final ISCStateRepository repository, final IProgressMonitor monitor)
			throws CoreException {
		final IRodinFile machineFile = (IRodinFile) element;
		machineRoot = (IMachineRoot) machineFile.getRoot();
		final IRefinesMachine[] refinesMachines = machineRoot
				.getRefinesClauses();
		scAbstractMachineRoot = null;
		typenv = repository.getTypeEnvironment();

		if (refinesMachines.length > 0) {
			abstractMachine = refinesMachines[0];
			if (abstractMachine.hasAbstractMachineName()) {
				scAbstractMachineRoot = (ISCMachineRoot) abstractMachine
						.getAbstractSCMachineRoot();
			}
		}

		final IVariable[] concreteVariables = machineRoot.getVariables();
		varMap = new HashMap<String, IVariable>();
		for (IVariable variable : concreteVariables) {
			varMap.put(variable.getIdentifierString(), variable);
		}
		final IEvent[] concreteEvents = machineRoot.getEvents();
		evtMap = new HashMap<String, IEvent>();
		for (IEvent event : concreteEvents) {
			evtMap.put(event.getLabel(), event);
		}
	}

	/**
	 * Check that a variable tagged as <i>shared</i> in the abstract machine is
	 * always present in the concrete machine, and still has the <i>shared</i>
	 * attribute.
	 * 
	 * @throws CoreException
	 *             if a problem occurs when checking the variables
	 */
	private void fetchSCVariables() throws CoreException {
		final ISCVariable[] variables = scAbstractMachineRoot.getSCVariables();
		if (variables.length == 0)
			return;
		for (final ISCVariable variable : variables) {
			final String name = variable.getIdentifierString();
			final INatureElement elt = (INatureElement) variable
					.getAdapter(INatureElement.class);
			if (elt.getNature() == Nature.SHARED) {
				IVariable concreteVariable = varMap.get(name);
				if (concreteVariable == null) {
					createProblemMarker(machineRoot,
							DecompositionProblem.VariableHasDisappearedError,
							name);
				} else {
					final INatureElement concreteElt = (INatureElement) concreteVariable
							.getAdapter(INatureElement.class);
					if (concreteElt.getNature() != Nature.SHARED) {
						createProblemMarker(
								concreteVariable,
								NATURE_ATTRIBUTE,
								DecompositionProblem.VariableInvalidNatureError,
								name);
					}

				}
			}

		}
	}

	/**
	 * <ul>
	 * <li>Check that an event tagged as "external" in the abstract machine is
	 * always present in the concrete machine.
	 * <li>Check that this event has the "external" attribute.
	 * <li>Check that this event has the "extended" attribute. It implies that
	 * this event has a <i>REFINES</i> clause; otherwise an
	 * <tt>EventExtendedUnrefinedError</tt> is detected. It implies too that the
	 * <i>REFINES</i> clause points to the event itself; otherwise an
	 * <tt>InconsistentEventLabelWarning</tt> is detected.
	 * <li>Check that this event does not declare any additional element
	 * (parameter, guard, witness, action). Note that this event implicitly does
	 * not declare any additional guard because the event is <i>extended</i>.
	 * </ul>
	 * 
	 * @throws CoreException
	 *             if a problem occurs when checking the events
	 */
	private void fetchSCEvents() throws CoreException {
		final ISCEvent[] events = scAbstractMachineRoot.getSCEvents();
		if (events.length == 0)
			return;
		for (ISCEvent event : events) {
			String label = event.getLabel();
			IExternalElement elt = (IExternalElement) event
					.getAdapter(IExternalElement.class);
			if (elt.isExternal()) {
				IEvent concreteEvent = evtMap.get(label);
				if (concreteEvent == null) {
					createProblemMarker(machineRoot,
							DecompositionProblem.EventHasDisappearedError,
							label);
				} else {
					elt = (IExternalElement) concreteEvent
							.getAdapter(IExternalElement.class);
					if (!elt.isExternal()) {
						createProblemMarker(concreteEvent, EXTERNAL_ATTRIBUTE,
								DecompositionProblem.EventInvalidStatusError,
								label, AStyleAttributes.EXTERNAL_ATTRIBUTE
										.getName());
					}
					if (!concreteEvent.isExtended()) {
						createProblemMarker(concreteEvent, EXTENDED_ATTRIBUTE,
								DecompositionProblem.EventInvalidStatusError,
								label, EXTENDED_ATTRIBUTE.getName());
					}
					if (concreteEvent.getParameters().length != 0) {
						createProblemMarker(
								concreteEvent,
								DecompositionProblem.ParametersInExternalEventError);
					}
					if (concreteEvent.getGuards().length != 0) {
						createProblemMarker(concreteEvent,
								DecompositionProblem.GuardsInExternalEventError);
					}
					if (concreteEvent.getActions().length != 0) {
						createProblemMarker(
								concreteEvent,
								DecompositionProblem.ActionsInExternalEventError);
					}

				}
			}

		}
	}

	/**
	 * Check that the initialization event of the concrete machine does not
	 * contain an action modifying at the same time a private variable and a
	 * shared variable.
	 * 
	 * @throws CoreException
	 *             if a problem occurs when checking the initialization event
	 */
	private void fetchInit() throws CoreException {
		IEvent concreteInit = EventBUtils.getInitialisation(machineRoot);
		for (IAction action : concreteInit.getActions()) {
			boolean foundShared = false;
			boolean foundPrivate = false;
			String assignment = action.getAssignmentString();
			IParseResult result = FormulaFactory.getDefault().parseAssignment(
					assignment, LanguageVersion.LATEST, null);
			if (!result.hasProblem()) {
				for (FreeIdentifier identifier : result.getParsedAssignment()
						.getAssignedIdentifiers()) {
					IVariable concreteVariable = varMap.get(identifier
							.getName());
					if (concreteVariable != null) {
						INatureElement elt = (INatureElement) concreteVariable
								.getAdapter(INatureElement.class);
						if (elt.getNature() == Nature.SHARED) {
							foundShared = true;
						} else {
							foundPrivate = true;
						}
					}
				}
				if (foundShared && foundPrivate) {
					createProblemMarker(action,
							DecompositionProblem.ActionOnPrivateAndSharedError,
							action.getLabel());
				}
			}
		}
	}

	/**
	 * Check that the actions of the initialization event of the abstract
	 * machine related to the shared variables are always present and are
	 * syntactically equal in the initialization event of the concrete machine.
	 * 
	 * @throws CoreException
	 *             if a problem occurs when checking the initialization event
	 */
	private void fetchSCInit() throws CoreException {
		IEvent concreteInit = EventBUtils.getInitialisation(machineRoot);
		if (concreteInit.isExtended())
			return;
		IAction[] initActions = concreteInit.getActions();
		Map<String, ISCVariable> absVarMap = new HashMap<String, ISCVariable>();
		for (ISCVariable variable : scAbstractMachineRoot.getSCVariables()) {
			absVarMap.put(variable.getIdentifierString(), variable);
		}
		ISCEvent abstractInit = getInitialisation(scAbstractMachineRoot);
		for (ISCAction abstractAction : abstractInit.getSCActions()) {
			Assignment assignment = abstractAction.getAssignment(FormulaFactory
					.getDefault(), typenv);
			for (FreeIdentifier identifier : assignment
					.getAssignedIdentifiers()) {
				ISCVariable abstractVariable = absVarMap.get(identifier
						.getName());
				INatureElement elt = (INatureElement) abstractVariable
						.getAdapter(INatureElement.class);
				if (elt.getNature() == Nature.SHARED) {
					boolean foundAssignment = false;
					for (IAction initAction : initActions) {
						if (initAction.getAssignmentString().equals(
								assignment.toString())) {
							foundAssignment = true;
							break;
						}
					}
					if (!foundAssignment) {
						createProblemMarker(concreteInit,
								DecompositionProblem.ActionOnSharedError,
								abstractAction.getLabel());
					}
					break;
				}

			}
		}

	}

	private static ISCEvent getInitialisation(final ISCMachineRoot mch)
			throws RodinDBException {
		return getEventWithLabel(mch, IEvent.INITIALISATION);
	}

	private static ISCEvent getEventWithLabel(final ISCMachineRoot mch,
			final String label) throws RodinDBException {
		ISCEvent[] evts = mch.getSCEvents();
		for (ISCEvent evt : evts) {
			if (evt.getLabel().equals(label)) {
				return evt;
			}
		}
		return null;
	}

}
