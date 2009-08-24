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

import java.util.ArrayList;
import java.util.List;

import org.eventb.core.IMachineRoot;

import ch.ethz.eventb.internal.decomposition.IModelDecomposition;
import ch.ethz.eventb.internal.decomposition.ISubModel;

/**
 * @author htson
 *         <p>
 *         A default implementation of {@link IModelDecomposition} for
 *         decomposition.
 *         </p>
 */
public abstract class DefaultModelDecomposition implements IModelDecomposition {

	// The machine to be decomposed.
	private IMachineRoot mch;

	// A list of sub-models.
	private List<ISubModel> subModels;

	/**
	 * Constructor. Creates a model decomposition.
	 */
	public DefaultModelDecomposition() {
		subModels = new ArrayList<ISubModel>();
	}

	/**
	 * Constructor. Creates a model decomposition.
	 */
	public DefaultModelDecomposition(IMachineRoot mch) {
		this();
		setMachineRoot(mch);
	}

	public void setMachineRoot(IMachineRoot mch) {
		this.mch = mch;
	}

	public IMachineRoot getMachineRoot() {
		return mch;
	}

	public ISubModel addSubModel() {
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

	public ISubModel[] getSubModels() {
		return subModels.toArray(new ISubModel[subModels.size()]);
	}

	public void removeSubModel(ISubModel model) {
		subModels.remove(model);
		return;
	}
}
