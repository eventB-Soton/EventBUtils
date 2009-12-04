/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
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

import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.decomposition.ISubModel;

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

	/** The component name. */
	private String componentName;
	
	/** The set of Rodin elements. */
	private IRodinElement[] elements;
	
	/** The associated model distribution. */
	private IModelDecomposition modelDecomp;

	/**
	 * Constructor. Creates a sub-model. The name of the project, the set of 
	 * events and the accessed variables are initialized to default values.
	 * 
	 * @param modelDecomp
	 *            a model decomposition.
	 */
	public DefaultSubModel(final IModelDecomposition modelDecomp) {
		this.modelDecomp = modelDecomp;
		componentName = ISubModel.DEFAULT_PROJECT_NAME;
		elements = new IRodinElement[0];
	}
	
	public final IModelDecomposition getModelDecomposition() {
		return modelDecomp;
	}

	public final IMachineRoot getMachineRoot() {
		return modelDecomp.getMachineRoot();
	}

	public final void setComponentName(final String prjName) {
		this.componentName = prjName;
	}

	public final String getComponentName() {
		return componentName;
	}

	public final void setElements(final IRodinElement... elements) {
		this.elements = elements;
	}

	public final IRodinElement[] getElements() {
		return elements;
	}
	
}
