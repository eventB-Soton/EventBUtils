/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.decomposition;

import org.eventb.core.IMachineRoot;
import org.rodinp.core.IRodinElement;

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
	IModelDecomposition getModelDecomposition();

	/**
	 * Returns the machine root for this decomposition.
	 * 
	 * @return the non-decomposed machine.
	 */
	IMachineRoot getMachineRoot();

	/**
	 * Sets the name of the component associated to this sub-model.
	 * 
	 * @param componentName
	 *            the component name.
	 */
	void setComponentName(String componentName);

	/**
	 * Returns the name of the component associated to this sub-model.
	 * 
	 * @return the component name.
	 */
	String getComponentName();

	/**
	 * Sets the elements chosen by the end-user for this sub-model.
	 * 
	 * @param elements
	 *            the chosen elements.
	 */
	void setElements(IRodinElement... elements);

	/**
	 * Returns the elements chosen by the end-user for this sub-model.
	 * 
	 * @return the chosen elements.
	 */
	IRodinElement[] getElements();
}
