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

package ch.ethz.eventb.internal.decomposition.wizards;

import java.util.Set;

import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         Interface for a model distribution. A model distribution contains
 *         information how a machine ({@link #getMachineRoot()}) is decomposed.
 *         The information is contains in a set of element distributions
 *         {@link IElementDistribution}.
 *         </p>
 */
public interface IModelDistribution {

	/**
	 * Return the machine root for this for the distribution.
	 * 
	 * @return the machine root of this distribution.
	 */
	public IMachineRoot getMachineRoot();

	/**
	 * Create a new element (sub-)distribution.
	 * 
	 * @return the newly create element distribution.
	 */
	public IElementDistribution createElementDistribution();

	/**
	 * Return the set of element sub-distributions.
	 * 
	 * @return the element sub-distributions.
	 */
	public IElementDistribution[] getElementDistributions();

	/**
	 * Remove the input element distribution.
	 * 
	 * @param element
	 *            an element distribution.
	 */
	public void removeElementDistribution(IElementDistribution element);

	/**
	 * Get the set of shared variables (in {@link String}).
	 * 
	 * @return the set of shared variables.
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	public Set<String> getSharedVariables() throws RodinDBException;

}
