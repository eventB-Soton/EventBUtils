/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     ETH Zurich - initial API and implementation
 ****************************************************************************/

package ch.ethz.eventb.internal.decomposition;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eventb.core.IEvent;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IConvergenceElement.Convergence;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.ISubModel;
import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         Class containing useful methods to perform decomposition (A-style or
 *         B-style).
 *         </p>
 */
public class DecompositionUtils {

	/**
	 * Returns the set of variables accessed by a sub-model.
	 * 
	 * @param subModel
	 *            the sub-model to be considered
	 * @return the labels of the accessed variables.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static Set<String> getAccessedVariables(final ISubModel subModel)
			throws RodinDBException {
		IMachineRoot mch = subModel.getMachineRoot();
		Set<String> vars = new HashSet<String>();
		// Adds the free identifiers from the events.
		for (IRodinElement element : subModel.getElements()) {
			for (IEvent event : mch.getEvents()) {
				if (event.getLabel().equals(((IEvent) element).getLabel())) {
					vars.addAll(EventBUtils.getFreeIdentifiers(event));
				}
			}
		}
		// Removes the constants and sets.
		vars.removeAll(EventBUtils.getSeenCarrierSetsAndConstants(mch));
		return vars;
	}

	/**
	 * Utility method to create invariants in an input machine for a given
	 * sub-model. This is done by first creating the typing theorems for the
	 * accessed variables, and then copying the "relevant" invariants from the
	 * source model (recursively).
	 * 
	 * @param mch
	 *            a machine.
	 * @param subModel
	 *            a sub-model.
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public static void decomposeInvariants(final IMachineRoot mch,
			final ISubModel subModel, final IProgressMonitor monitor)
			throws RodinDBException {
		IMachineRoot src = subModel.getMachineRoot();
		Set<String> vars = getAccessedVariables(subModel);
		
		// Create the typing theorems.
		createTypingTheorems(mch, src, vars, new SubProgressMonitor(monitor, 1));

		// Copy relevant invariants.
		EventBUtils.copyInvariants(mch, src, vars, new SubProgressMonitor(
				monitor, 1));

		monitor.done();
	}

	/**
	 * Utility method to create typing theorems in an input machine, given the
	 * set of variables and the source machine containing these variables.
	 * 
	 * @param mch
	 *            a machine.
	 * @param src
	 *            the source machine containing the variables.
	 * @param vars
	 *            the set of variables (in {@link String}).
	 * @param monitor
	 *            the progress monitor.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private static void createTypingTheorems(final IMachineRoot mch,
			final IMachineRoot src, final Set<String> vars,
			final IProgressMonitor monitor) throws RodinDBException {
		monitor.beginTask(Messages.decomposition_typingtheorems, vars.size());
		for (String var : vars) {
			IInvariant newInv = mch.createChild(IInvariant.ELEMENT_TYPE, null,
					monitor);
			newInv.setLabel(Messages.decomposition_typing + "_" + var, monitor);
			newInv.setTheorem(true, monitor);
			newInv.setPredicateString(EventBUtils.getTypingTheorem(src, var),
					monitor);
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * Sets the status of an event in a sub-machine from the status of the
	 * associated event in the non-decomposed machine:
	 * <ul>
	 * <li>An <i>internal</i> event of a sub-machine is tagged as
	 * <i>ordinary</i> if and only if this event was declared <i>ordinary</i> or
	 * <i>convergent</i> in the non-decomposed machine.
	 * <li>An <i>internal</i> event of a sub-machine is tagged as
	 * <i>anticipated</i> if and only if this event was declared
	 * <i>anticipated</i> in the non-decomposed machine.
	 * <li>An <i>internal</i> event of a sub-machine is never tagged as
	 * <i>convergent</i>.
	 * <li>An <i>external</i> event of a sub-machine is always tagged as
	 * <i>ordinary</i>.
	 * <li>An event of a sub-machine, <i>external</i> or <i>internal</i>, is
	 * always tagged as <i>non-extended</i>.
	 * <li>An event tagged as <i>external</i> in the non-decomposed machine
	 * (<i>i.e.</i>resulting from a previous decomposition) remains
	 * <i>external</i> in the sub-machine.
	 * </ul>
	 * 
	 * @param srcEvt
	 *            the source event in the non-decomposed machine
	 * @param destEvt
	 *            the destination event in a sub-machine
	 * @param monitor
	 *            the progress monitor
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	public static void setEventStatus(final IEvent srcEvt,
			final IEvent destEvt, final IProgressMonitor monitor)
			throws RodinDBException {
		// Gets the external status of the source event
		IExternalElement srcElt = (IExternalElement) srcEvt
				.getAdapter(IExternalElement.class);
		// Gets the external status of the destination event
		IExternalElement destElt = (IExternalElement) destEvt
				.getAdapter(IExternalElement.class);

		// Sets the convergence
		if (destElt.isExternal()) {
			destEvt.setConvergence(Convergence.ORDINARY, monitor);
		} else {
			Convergence convergence = srcEvt.getConvergence();
			if (convergence.equals(Convergence.ORDINARY)
					|| convergence.equals(Convergence.CONVERGENT)) {
				destEvt.setConvergence(Convergence.ORDINARY, monitor);
			} else if (convergence.equals(Convergence.ANTICIPATED)) {
				destEvt.setConvergence(Convergence.ANTICIPATED, monitor);
			}
		}

		// Sets the extended status
		destEvt.setExtended(false, monitor);

		// Sets the external status
		if (srcElt.isExternal()) {
			destElt.setExternal(true, monitor);
		}
	}
}
