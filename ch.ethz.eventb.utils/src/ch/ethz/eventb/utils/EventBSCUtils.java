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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eventb.core.IAxiom;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ISCAxiom;
import org.eventb.core.ISCCarrierSet;
import org.eventb.core.ISCConstant;
import org.eventb.core.ISCIdentifierElement;
import org.eventb.core.ISCInternalContext;
import org.eventb.core.ISCInvariant;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ISCVariable;
import org.eventb.core.ast.Type;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.utils.Messages;

/**
 * @author htson
 *         <p>
 *         Utility class containing some useful methods to handle Event-B
 *         statically-checked elements.
 *         </p>
 */
public final class EventBSCUtils {

	private EventBSCUtils() {
		// Utility classes shall not have a public or default constructor.
	}

	/**
	 * Returns a map from label (String) to predicate string (String)
	 * corresponding to the seen axioms of an EXISTING machine root. This is
	 * done by checking the statically checked version of the machine (as a
	 * result, the statically checked version must exist).
	 * 
	 * @param mchRoot
	 *            a machine root.
	 * @param isTheorem
	 *            the flag to indicate either only axioms or theorems can be
	 *            selected.
	 * @return the map of seen axioms of the input machine. Each axiom is a map
	 *         from a label to a predicate string. The label is composed of the
	 *         name of the context and the original label of the axiom separated
	 *         by <code>/</code>. The predicate string is the source string
	 *         presented in the unchecked version of the context. There is no
	 *         guarantee on the order under which the axioms are sorted.
	 * @throws RodinDBException
	 *             if a problem occurs while accessing the database.
	 */
	public static Map<String, String> getSCSeenAxioms(IMachineRoot mchRoot,
			boolean isTheorem) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(mchRoot, Messages.error_NullMachine);
		Assert.isTrue(mchRoot.exists(), Messages.bind(
				Messages.error_NonExistingMachine, mchRoot.getRodinFile()
						.getBareName()));
		ISCMachineRoot scMchRoot = mchRoot.getSCMachineRoot();
		Assert.isNotNull(scMchRoot, Messages.error_NullSCMachine);
		Assert.isTrue(scMchRoot.exists(), Messages.bind(
				Messages.error_NonExistingSCMachine, scMchRoot.getRodinFile()
						.getBareName()));

		// Empty result.
		Map<String, String> result = new HashMap<String, String>();

		// Get the list of seen contexts.
		ISCInternalContext[] scSeenContexts = scMchRoot.getSCSeenContexts();

		// Add the axioms from each seen context to the result.
		for (ISCInternalContext scSeenContext : scSeenContexts) {
			ISCAxiom[] scAxioms = scSeenContext.getSCAxioms();
			for (ISCAxiom scAxiom : scAxioms) {

				if (scAxiom.isTheorem() == isTheorem) {
					String key = scSeenContext.getElementName() + "/"
							+ scAxiom.getLabel();
					IRodinElement source = scAxiom.getSource();
					Assert.isTrue(
							source instanceof IAxiom,
							Messages.bind(Messages.error_NotAnAxiom,
									source.getElementName()));
					IAxiom axiom = (IAxiom) source;
					result.put(key, axiom.getPredicateString());
				}
			}
		}

		// Return result as an array of objects.
		return result;

	}

	/**
	 * Utility method for getting the collection of statically checked seen
	 * element (e.g. carrier set or constant) identifier strings of an EXISTING
	 * machine by checking the statically checked version of the machine.
	 * 
	 * @param mchRoot
	 *            the input machine root.
	 * 
	 * @param type
	 *            the statically checked element type
	 * 
	 * @return the collection of statically checked seen element identifier
	 *         strings. There is no guarantee on the order under which the
	 *         identifiers are sorted.
	 * @see #getSCSeenElementIdentifierStrings(ISCMachineRoot,
	 *      IInternalElementType).
	 * @throws RodinDBException
	 *             if there was a problem accessing the database.
	 */
	private static Collection<String> getSCSeenElementIdentifierStrings(
			IMachineRoot mchRoot,
			IInternalElementType<? extends ISCIdentifierElement> type)
			throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(mchRoot, Messages.error_NullMachine);
		Assert.isTrue(mchRoot.exists(), Messages.bind(
				Messages.error_NonExistingMachine, mchRoot.getRodinFile()
						.getBareName()));
		ISCMachineRoot scMchRoot = mchRoot.getSCMachineRoot();
		Assert.isNotNull(scMchRoot, Messages.error_NullSCMachine);
		Assert.isTrue(scMchRoot.exists(), Messages.bind(
				Messages.error_NonExistingSCMachine, scMchRoot.getRodinFile()
						.getBareName()));

		// Get the seen element identifier strings of the statically checked
		// version.
		return getSCSeenElementIdentifierStrings(scMchRoot, type);
	}

	/**
	 * Utility method for getting the collection of statically checked seen
	 * element (e.g. carrier set or constant) identifier strings of an EXISTING
	 * statically checked machine.
	 * 
	 * @param mchRoot
	 *            the input statically checked machine root.
	 * 
	 * @param type
	 *            the statically checked element type
	 * 
	 * @return the collection of statically checked seen element identifier
	 *         strings. There is no guarantee on the order under which the
	 *         identifiers are sorted.
	 * @throws RodinDBException
	 *             if there was a problem accessing the database.
	 */
	private static Collection<String> getSCSeenElementIdentifierStrings(
			ISCMachineRoot scMchRoot,
			IInternalElementType<? extends ISCIdentifierElement> type)
			throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(scMchRoot, Messages.error_NullSCMachine);
		Assert.isTrue(scMchRoot.exists(), Messages.bind(
				Messages.error_NonExistingSCMachine, scMchRoot.getRodinFile()
						.getBareName()));
		Collection<String> result = new ArrayList<String>();

		// Get the list of seen contexts.
		ISCInternalContext[] scSeenContexts = scMchRoot.getSCSeenContexts();

		// Add the constants and carrier sets from each seen context to the
		// result.
		for (ISCInternalContext scSeenContext : scSeenContexts) {
			ISCIdentifierElement[] seenElms = scSeenContext
					.getChildrenOfType(type);
			for (ISCIdentifierElement seenElm : seenElms) {
				result.add(seenElm.getIdentifierString());
			}
		}

		return result;
	}

	/**
	 * Utility method for getting statically checked invariants of a machine
	 * root. The invariants include those from the abstract machines. The result
	 * is a map between the invariants' labels (including the machine name) and
	 * the predicate string.
	 * 
	 * @param mchRoot
	 *            the input machine root
	 * @param theorem
	 *            indicating if theorems in invariants are included
	 * @return the map of invariants' labels and the corresponding predicate
	 *         string.
	 * @throws RodinDBException
	 */
	public static Map<String, String> getSCInvariants(IMachineRoot mchRoot,
			boolean isTheorem) throws RodinDBException {
		// Assert preconditions.
		Assert.isNotNull(mchRoot, Messages.error_NullMachine);
		Assert.isTrue(mchRoot.exists(), Messages.bind(
				Messages.error_NonExistingMachine, mchRoot.getRodinFile()
						.getBareName()));
		ISCMachineRoot scMchRoot = mchRoot.getSCMachineRoot();
		Assert.isNotNull(scMchRoot, Messages.error_NullSCMachine);
		Assert.isTrue(scMchRoot.exists(), Messages.bind(
				Messages.error_NonExistingSCMachine, scMchRoot.getRodinFile()
						.getBareName()));

		// Empty result.
		Map<String, String> result = new HashMap<String, String>();
		ISCInvariant[] scInvariants = scMchRoot.getSCInvariants();
		for (ISCInvariant scInvariant : scInvariants) {
			if (scInvariant.isTheorem() == isTheorem) {
				String key = scMchRoot.getElementName() + "/"
						+ scInvariant.getLabel();
				IRodinElement source = scInvariant.getSource();
//				Assert.isTrue(
//						source instanceof IInvariant,
//						Messages.bind(Messages.error_NotAnAxiom,
//								source.getElementName()));
				IInvariant invariant = (IInvariant) source;
				result.put(key, invariant.getPredicateString());
			}
		}
		return result;
	}

	/**
	 * Utility method for getting the collection of statically checked seen
	 * carrier set identifier strings of an EXISTING machine by looking at the
	 * statically checked version of the machine (as a result, the statically
	 * checked version must exist).
	 * 
	 * @param mch
	 *            the input machine root.
	 * 
	 * @return the collection of statically checked seen carrier set identifier
	 *         strings. There is no guarantee on the order under which the
	 *         identifiers are sorted.
	 * 
	 * @throws RodinDBException
	 *             if there was a problem accessing the database.
	 */
	public static Collection<String> getSCSeenCarrierSetIdentifierStrings(
			IMachineRoot mch) throws RodinDBException {
		return getSCSeenElementIdentifierStrings(mch,
				ISCCarrierSet.ELEMENT_TYPE);
	}

	/**
	 * Utility method for getting the collection of statically checked seen
	 * carrier set identifier strings of an EXISTING statically checked machine.
	 * 
	 * @param mch
	 *            the input machine root.
	 * 
	 * @return the collection of statically checked seen carrier set identifier
	 *         strings. There is no guarantee on the order under which the
	 *         identifiers are sorted.
	 * 
	 * @throws RodinDBException
	 *             if there was a problem accessing the database.
	 */
	public static Collection<String> getSCSeenCarrierSetIdentifierStrings(
			ISCMachineRoot mch) throws RodinDBException {
		return getSCSeenElementIdentifierStrings(mch,
				ISCCarrierSet.ELEMENT_TYPE);
	}

	/**
	 * Utility method for getting the collection of statically checked seen
	 * constant identifier strings of an EXISTING machine by looking at the
	 * statically checked version of the machine (as a result, the statically
	 * checked version must exist).
	 * 
	 * @param mch
	 *            the input machine root.
	 * 
	 * @return the collection of statically checked seen constant identifier
	 *         strings. There is no guarantee on the order under which the
	 *         identifiers are sorted.
	 * 
	 * @throws RodinDBException
	 *             if there was a problem accessing the database.
	 */
	public static Collection<String> getSCSeenConstantIdentifierStrings(
			IMachineRoot mch) throws RodinDBException {
		return getSCSeenElementIdentifierStrings(mch, ISCConstant.ELEMENT_TYPE);
	}

	/**
	 * Utility method for getting the collection of statically checked seen
	 * constant identifier strings of an EXISTING statically checked machine.
	 * 
	 * @param mch
	 *            the input machine root.
	 * 
	 * @return the collection of statically checked seen constant identifier
	 *         strings. There is no guarantee on the order under which the
	 *         identifiers are sorted.
	 * 
	 * @throws RodinDBException
	 *             if there was a problem accessing the database.
	 */
	public static Collection<String> getSCSeenConstantIdentifierStrings(
			ISCMachineRoot mch) throws RodinDBException {
		return getSCSeenElementIdentifierStrings(mch, ISCConstant.ELEMENT_TYPE);
	}

	/**
	 * @param mchRoot
	 * @param identifier
	 * @return 
	 * @throws CoreException 
	 */
	public static Type getVariableType(IMachineRoot mchRoot, String identifier)
			throws CoreException {
		// Assert preconditions.
		Assert.isNotNull(mchRoot, Messages.error_NullMachine);
		Assert.isTrue(mchRoot.exists(), Messages.bind(
				Messages.error_NonExistingMachine, mchRoot.getRodinFile()
						.getBareName()));
		ISCMachineRoot scMchRoot = mchRoot.getSCMachineRoot();
		Assert.isNotNull(scMchRoot, Messages.error_NullSCMachine);
		Assert.isTrue(scMchRoot.exists(), Messages.bind(
				Messages.error_NonExistingSCMachine, scMchRoot.getRodinFile()
						.getBareName()));

		ISCVariable[] scVariables = scMchRoot.getSCVariables();
		for (ISCVariable scVariable : scVariables) {
			if (scVariable.getIdentifierString().equals(identifier)) {
				return scVariable.getType(scMchRoot.getFormulaFactory());
			}
		}

		return null;
	}

}