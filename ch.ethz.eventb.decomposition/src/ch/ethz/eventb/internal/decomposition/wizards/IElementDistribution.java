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

/**
 * @author htson
 *         <p>
 *         An interface for element distribution
 *         </p>
 */
public interface IElementDistribution {

	/**
	 * Default project name.
	 */
	String DEFAULT_PROJECT_NAME = "Default";

	/**
	 * Return the model distribution associated with the distribution.
	 * 
	 * @return the model distribution associated with the distribution.
	 */
	public IModelDistribution getModelDistribution();

	/**
	 * Return the machine associated with the distribution.
	 * 
	 * @return the machine associated with the distribution.
	 */
	public IMachineRoot getMachineRoot();

	/**
	 * Set the project name for the distribution.
	 * 
	 * @param prjName
	 *            the name of the project.
	 */
	public void setProjectName(String prjName);

	/**
	 * Return the project name associated with the distribution.
	 * 
	 * @return the name of the project associated with the distribution.
	 */
	public String getProjectName();

	/**
	 * Set the set of event labels for the distribution.
	 * 
	 * @param evtLabels
	 *            an array of event labels.
	 */
	public void setEventLabels(String... evtLabels);

	/**
	 * Return the set of event labels associated with the distribution.
	 * 
	 * @return the set of event labels.
	 */
	public String[] getEventLabels();

	/**
	 * Return the set of accessed variables of the distribution.
	 * 
	 * @return the set of accessed variables.
	 */
	public Set<String> getAccessedVariables();

}
