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

import org.eventb.core.IMachineRoot;
import org.rodinp.core.IRodinElement;

import ch.ethz.eventb.internal.decomposition.IModelDecomposition;
import ch.ethz.eventb.internal.decomposition.ISubModel;

/**
 * @author htson
 *         <p>
 *         Default implementation of {@link ISubModel}. 
 *         Each sub-model is issued from a model decomposition. A sub-model 
 *         relies on an array of event labels, which can be set by
 *         the method {@link #setEvents(String...)}.
 *         </p>
 */
public class DefaultSubModel implements ISubModel {

	// The project name.
	private String prjName;
	
	// The set of Rodin elements.
	private IRodinElement[] elements;
	
	// The associated model distribution.
	private IModelDecomposition modelDecomp;

	/**
	 * Constructor. Creates a sub-model. The name of the project, the set of 
	 * events and the accessed variables are initialized to default values.
	 * 
	 * @param modelDecomp
	 *            a model decomposition.
	 */
	public DefaultSubModel(IModelDecomposition modelDecomp) {
		this.modelDecomp = modelDecomp;
		prjName = ISubModel.DEFAULT_PROJECT_NAME;
		elements = new IRodinElement[0];
	}
	
	public IModelDecomposition getModelDecomposition() {
		return modelDecomp;
	}

	public IMachineRoot getMachineRoot() {
		return modelDecomp.getMachineRoot();
	}

	public void setProjectName(String prjName) {
		this.prjName = prjName;
	}

	public String getProjectName() {
		return prjName;
	}

	public void setElements(IRodinElement... elements) {
		this.elements = elements;
	}

	public IRodinElement[] getElements() {
		return elements;
	}
	
}
