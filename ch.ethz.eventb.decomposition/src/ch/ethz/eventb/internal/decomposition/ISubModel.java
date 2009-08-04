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

import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.utils.Messages;


/**
 * @author htson
 *         <p>
 *         An interface for a sub-model issued from the decomposition.
 *         </p>
 */
public interface ISubModel {

	/**
	 * The default project name.
	 */
	String DEFAULT_PROJECT_NAME = Messages.decomposition_defaultproject;

	/**
	 * Returns the model decomposition from which this sub-model is issued.
	 * 
	 * @return the model decomposition associated with the sub-model.
	 */
	public IModelDecomposition getModelDecomposition();

	/**
	 * Returns the machine root for this decomposition.
	 * 
	 * @return the decomposed machine.
	 */
	public IMachineRoot getMachineRoot();

	/**
	 * Sets the name of the project associated to this sub-model.
	 * 
	 * @param prjName
	 *            the project name.
	 */
	public void setProjectName(String prjName);

	/**
	 * Returns the name of the project associated to this sub-model.
	 * 
	 * @return the project name.
	 */
	public String getProjectName();
	
	/**
	 * Sets the set of events contained in this sub-model.
	 * 
	 * @param events
	 *            the labels of the contained events.
	 */
	public void setEvents(String... events);

	/**
	 * Returns the set of events contained in this sub-model.
	 * 
	 * @return the labels of the contained events.
	 */
	public String[] getEvents();

	/**
	 * Returns the set of variables accessed by this sub-model.
	 * 
	 * @return the labels of the accessed variables.
	 * @throw RodinDBException if a problem occurs when accessing the Rodin
	 *        database.
	 */
	public Set<String> getAccessedVariables() throws RodinDBException;

}
