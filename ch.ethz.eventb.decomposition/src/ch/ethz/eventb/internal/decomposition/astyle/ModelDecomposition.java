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

package ch.ethz.eventb.internal.decomposition.astyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.IModelDecomposition;
import ch.ethz.eventb.internal.decomposition.ISubModel;

/**
 * @author htson
 *         <p>
 *         An implementation of {@link IModelDecomposition} for A-style
 *         decomposition.
 *         </p>
 */
public class ModelDecomposition implements IModelDecomposition {

	// The machine to be decomposed.
	private IMachineRoot mch;

	// A list of element sub-distributions.
	private List<ISubModel> subModels;

	// The set of shared variables. It needs to be reset to <code>null</code>
	// if the sub-models have changed.
	private Set<String> sharedVars;

	/**
	 * Constructor. Creates a model decomposition.
	 */
	public ModelDecomposition() {
		subModels = new ArrayList<ISubModel>();
		sharedVars = new HashSet<String>();
	}
	
	/**
	 * Constructor. Creates a model decomposition.
	 */
	public ModelDecomposition(IMachineRoot mch) {
		this();
		setMachineRoot(mch);
	}

	public void setMachineRoot(IMachineRoot mch) {
		this.mch = mch;
	}

	public IMachineRoot getMachineRoot() {
		return mch;
	}

	public ISubModel createSubModel() {
		ISubModel subModel = new SubModel(this);
		subModels.add(subModel);
		// Tag it as out-of-date
		setOutOfDate();
		return subModel;
	}

	public ISubModel[] getSubModels() {
		return subModels.toArray(new ISubModel[subModels.size()]);
	}

	public void removeSubModel(ISubModel elemDist) {
		subModels.remove(elemDist);
		// Tag it as out-of-date
		setOutOfDate();
		return;
	}

	public Set<String> getSharedVariables() throws RodinDBException {
		if (isOutOfDate())
			calculateSharedVariables();
		return sharedVars;
	}

	public String getStyle() {
		return IModelDecomposition.A_STYLE;
	}
	
	public void perform(IProgressMonitor monitor) throws RodinDBException {
		DecompositionUtils.decompose(this, monitor);
	}

	/**
	 * Utility method to build the set of shared variables according to the
	 * decomposition. A variable is shared if and only if it is accessed by
	 * several sub-models.
	 * 
	 * @throws RodinDBException
	 *             if a problem occurs when accessing the Rodin database.
	 */
	private void calculateSharedVariables() throws RodinDBException {
		if (!isOutOfDate())
			return;

		sharedVars = new HashSet<String>();
		for (IVariable var : mch.getVariables()) {
			int occurrence = 0;
			String ident = var.getIdentifierString();
			for (ISubModel subModel : subModels) {
				if (subModel.getAccessedVariables().contains(ident)) {
					occurrence++;
				}
				if (occurrence > 1)
					break;
			}
			if (occurrence > 1) {
				sharedVars.add(ident);
			}

		}

	}

	/**
	 * Utility method to set the out-of-date status.
	 */
	void setOutOfDate() {
		sharedVars = null;
	}

	/**
	 * Checks if the information is currently out-of-date.
	 * 
	 * @return <code>true</code> if and only if the current status is
	 *         out-of-date.
	 */
	private boolean isOutOfDate() {
		return sharedVars == null;
	}
}
