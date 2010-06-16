/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - added context related methods, used symbol tables
 *     Systerel - implemented progress reporting and cancellation support
 *     Extracted from ch.ethz.eventb.decomposition plugin.
 *******************************************************************************/
package ch.ethz.eventb.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eventb.core.IMachineRoot;
import org.eventb.core.ISCAxiom;
import org.eventb.core.ISCCarrierSet;
import org.eventb.core.ISCConstant;
import org.eventb.core.ISCInternalContext;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         Utility class containing some useful methods to handle Event-B
 *         statically-checked elements.</p>
 */
public final class EventBSCUtils {


	private EventBSCUtils() {
		// Utility classes shall not have a public or default constructor.
	}

	/**
	 * Return the collection of seen constants and carrier sets (as strings) of
	 * a given machine by looking at the statically checked version of the
	 * machine.
	 * 
	 * @param mch
	 *            the input machine root.
	 * @return the collection of seen constants and carrier sets. Return an
	 *         empty collection if the statically checked version of the machine
	 *         does not exist.
	 * @throws RodinDBException
	 *             if some errors occurred when:
	 *             <ul>
	 *             <li>Getting the statically checked version of the machine.</li>
	 *             <li>Getting the internal elements, e.g. seen contexts, of the
	 *             statically checked version.</li>
	 *             </ul>
	 */
	public static Collection<IRodinElement> getSeenCarrierSetsAndConstants(
			IMachineRoot mch) throws RodinDBException {
		Collection<IRodinElement> result = new ArrayList<IRodinElement>();

		if (mch == null)
			return result;

		// Get the statically checked version of the machine root and test if it
		// exists.
		ISCMachineRoot scMachineRoot = mch.getSCMachineRoot();
		if (!scMachineRoot.exists())
			return result;

		// Get the list of seen contexts.
		ISCInternalContext[] scSeenContexts = scMachineRoot.getSCSeenContexts();

		// Add the constants and carrier sets from each seen context to the
		// result.
		for (ISCInternalContext scSeenContext : scSeenContexts) {
			ISCConstant[] scConstants = scSeenContext.getSCConstants();
			for (ISCConstant scConstant : scConstants) {
				result.add(scConstant.getSource());
			}
			ISCCarrierSet[] scCarrierSets = scSeenContext.getSCCarrierSets();
			for (ISCCarrierSet scCarrierSet : scCarrierSets) {
				result.add(scCarrierSet.getSource());
			}
		}

		// Return result as an array of objects.
		return result;
	}

	public final static int AXIOMS = 1;

	public final static int THEOREMS = 2;

	/**
	 * Returns a map from label (String) to predicate ({@link Predicate})
	 * corresponding to the seen axioms of a given machine root. This is done by
	 * looking at the statically checked version of the machine.
	 * 
	 * @param mch
	 *            a machine root.
	 * @param flag
	 *            the flag to indicate either only axioms or theorems or both
	 *            must be selected. See {@link #AXIOMS} and {@link #THEOREMS}.
	 * @return the map of seen axioms of the input machine. Each axiom is a map
	 *         from a label to a predicate. The label is composed of the name of
	 *         the machine and the original label of the axiom with
	 *         <code>/</code> in between.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static Map<String, Predicate> getSeenAxioms(IMachineRoot mch,
			int flag) throws RodinDBException {
		Map<String, Predicate> result = new HashMap<String, Predicate>();

		if (mch == null)
			return result;

		// Get the statically checked version of the machine root and test if it
		// exists.
		ISCMachineRoot scMachineRoot = mch.getSCMachineRoot();
		if (!scMachineRoot.exists())
			return result;

		// Get the list of seen contexts.
		ISCInternalContext[] scSeenContexts = scMachineRoot.getSCSeenContexts();

		// Add the axioms from each seen context to the result.
		for (ISCInternalContext scSeenContext : scSeenContexts) {
			ISCAxiom[] scAxioms = scSeenContext.getSCAxioms();
			for (ISCAxiom scAxiom : scAxioms) {
				int internalFlag = scAxiom.isTheorem() ? THEOREMS : AXIOMS;

				if ((internalFlag & flag) != 0) {
					// IMPORTANT: GET THE PREDICATE STRING AND PARSE IT TO
					// HAVE A CLEAR THE TYPE ENVIRONMENT. THIS ALLOWS THE
					// SUBTITUTIONS LATTER ON.
					String key = scSeenContext.getElementName() + "/"
							+ scAxiom.getLabel();
					Predicate value = Lib.parsePredicate(scAxiom
							.getPredicateString());
					result.put(key, value);
				}
			}
		}

		// Return result as an array of objects.
		return result;

	}

}