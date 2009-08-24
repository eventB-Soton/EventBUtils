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

package ch.ethz.eventb.internal.decomposition.bstyle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.DefaultModelDecomposition;
import ch.ethz.eventb.internal.decomposition.DefaultSubModel;
import ch.ethz.eventb.internal.decomposition.IModelDecomposition;
import ch.ethz.eventb.internal.decomposition.ISubModel;

/**
 * @author htson
 *         <p>
 *         An implementation of {@link IModelDecomposition} for B-style
 *         decomposition.
 *         </p>
 */
public class ModelDecomposition extends
		DefaultModelDecomposition {

	public ModelDecomposition() {
		super();
	}

	public ModelDecomposition(IMachineRoot mch) {
		super(mch);
	}

	public ISubModel createSubModel() {
		return new DefaultSubModel(this);
	}

	public String getStyle() {
		return IModelDecomposition.B_STYLE;
	}

	/**
	 * @TODO To be implemented.
	 */
	public void perform(IProgressMonitor monitor) throws RodinDBException {
	}
}
