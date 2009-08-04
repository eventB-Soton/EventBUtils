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

package ch.ethz.eventb.internal.decomposition;

import java.util.Set;

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

	/**
	 * The A-style decomposition.
	 */
	public final static String A_STYLE = Messages.decomposition_astyle;

	/**
	 * The B-style decomposition.
	 */
	public final static String B_STYLE = Messages.decomposition_bstyle;
	
	/**
	 * Sets the machine root for this decomposition.
	 * 
	 * @param mch
	 *            the machine root.
	 */
	public void setMachineRoot(IMachineRoot root);

	/**
	 * Returns the machine root for this decomposition.
	 * 
	 * @return the decomposed machine.
	 */
	public IMachineRoot getMachineRoot();

	/**
	 * Creates a new sub-model.
	 * 
	 * @return the newly created sub-model.
	 */
	public ISubModel createSubModel();

	/**
	 * Returns the set of sub-models.
	 * 
	 * @return the sub-models.
	 */
	public ISubModel[] getSubModels();

	/**
	 * Removes a sub-model.
	 * 
	 * @param model
	 *            the model to be removed.
	 */
	public void removeSubModel(ISubModel model);

	/**
	 * Gets the set of variables shared between the sub-models of the
	 * decomposition (in {@link String}).
	 * 
	 * @return the labels of the shared variables.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public Set<String> getSharedVariables() throws RodinDBException;

	/**
	 * Gets the decomposition style.
	 * 
	 * @return the decomposition style.
	 */
	public String getStyle();
	
	/**
	 * Performs the decomposition.
	 * 
	 * @param monitor
	 *            a progress monitor.
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	public void perform(IProgressMonitor monitor) throws RodinDBException;
}