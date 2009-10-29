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

package ch.ethz.eventb.internal.decomposition;

import java.util.ArrayList;
import java.util.List;

import org.eventb.core.IMachineRoot;

import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.decomposition.ISubModel;

/**
 * @author htson
 *         <p>
 *         A default implementation of {@link IModelDecomposition} for
 *         decomposition.
 *         </p>
 */
public abstract class DefaultModelDecomposition implements IModelDecomposition {

	/** The machine to be decomposed. */
	private IMachineRoot mch;

	/** A list of sub-models. */
	private List<ISubModel> subModels;

	private ContextDecomposition contextDecomposition;

	/**
	 * Constructor. Creates a model decomposition.
	 */
	public DefaultModelDecomposition() {
		subModels = new ArrayList<ISubModel>();
		contextDecomposition = ContextDecomposition.MINIMAL_FLATTENED_CONTEXT;
	}

	/**
	 * Constructor. Creates a model decomposition.
	 */
	public DefaultModelDecomposition(final IMachineRoot mch) {
		this();
		setMachineRoot(mch);
	}

	public final void setMachineRoot(final IMachineRoot mch) {
		this.mch = mch;
	}

	public final IMachineRoot getMachineRoot() {
		return mch;
	}

	public final ISubModel addSubModel() {
		ISubModel model = createSubModel();
		subModels.add(model);
		return model;
	}

	/**
	 * Creates a new sub-model.
	 * 
	 * @return the newly created model.
	 */
	public abstract ISubModel createSubModel();

	public final ISubModel[] getSubModels() {
		return subModels.toArray(new ISubModel[subModels.size()]);
	}

	public final void removeSubModel(final ISubModel model) {
		subModels.remove(model);
		return;
	}
	
	public ContextDecomposition getContextDecomposition() {
		return contextDecomposition;
	}
	
	public void setContextDecomposition(
			ContextDecomposition contextDecomposition) {
		this.contextDecomposition = contextDecomposition;
	}
}
