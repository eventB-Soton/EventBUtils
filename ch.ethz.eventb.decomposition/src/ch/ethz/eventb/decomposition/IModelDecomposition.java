/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - implemented context decomposition
 *******************************************************************************/

package ch.ethz.eventb.decomposition;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         Interface for a model decomposition. A model decomposition contains
 *         information on how a machine ({@link #getMachineRoot()}) is
 *         decomposed in sub-models {@link ISubModel}.
 *         </p>
 */
public interface IModelDecomposition {

	public static enum ContextDecomposition {
		NO_DECOMPOSITION, MINIMAL_FLATTENED_CONTEXT, CONTEXT_SELECTION,
	}

	/**
	 * The A-style decomposition.
	 */
	String A_STYLE = Messages.decomposition_astyle;

	/**
	 * The B-style decomposition.
	 */
	String B_STYLE = Messages.decomposition_bstyle;

	/**
	 * Sets the machine root for this decomposition.
	 * 
	 * @param root
	 *            the machine root
	 */
	void setMachineRoot(IMachineRoot root);

	/**
	 * Returns the machine root for this decomposition.
	 * 
	 * @return the non-decomposed machine
	 */
	IMachineRoot getMachineRoot();

	/**
	 * Creates a sub-model.
	 * 
	 * @return the newly created model
	 */
	ISubModel addSubModel();

	/**
	 * Returns the set of sub-models.
	 * 
	 * @return the sub-models
	 */
	ISubModel[] getSubModels();

	/**
	 * Removes a sub-model.
	 * 
	 * @param model
	 *            the model to be removed
	 */
	void removeSubModel(ISubModel model);

	/**
	 * Gets the decomposition style.
	 * 
	 * @return the decomposition style
	 */
	String getStyle();

	/**
	 * Returns the type of context decomposition.
	 * 
	 * @return the type of context decomposition
	 */
	ContextDecomposition getContextDecomposition();

	/**
	 * Sets the type of context decomposition.
	 * 
	 * @param contextDecomposition
	 *            the type of context decomposition
	 */
	void setContextDecomposition(ContextDecomposition contextDecomposition);

	/**
	 * Determines if the decomposition is authorized or not.
	 * 
	 * @param monitor
	 *            a progress monitor
	 * @return <tt>true</tt> if and only if the decomposition is authorized
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	boolean check(IProgressMonitor monitor) throws RodinDBException;

	/**
	 * Performs the decomposition.
	 * 
	 * @param monitor
	 *            a progress monitor
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database
	 */
	void perform(IProgressMonitor monitor) throws RodinDBException;
}