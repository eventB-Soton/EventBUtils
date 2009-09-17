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

import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.decomposition.ISubModel;
import ch.ethz.eventb.internal.decomposition.DefaultModelDecomposition;
import ch.ethz.eventb.internal.decomposition.DefaultSubModel;

/**
 * @author htson
 *         <p>
 *         An implementation of {@link IModelDecomposition} for B-style
 *         decomposition.
 *         </p>
 */
public class ModelDecomposition extends DefaultModelDecomposition {

	/**
	 * Builds a new instance of model decomposition.
	 */
	public ModelDecomposition() {
		super();
	}

	/**
	 * Builds a new instance of model decomposition to decompose the specified
	 * machine.
	 * 
	 * @param mch
	 *            the machine to be decomposed.
	 */
	public ModelDecomposition(IMachineRoot mch) {
		super(mch);
	}

	public final ISubModel createSubModel() {
		return new DefaultSubModel(this);
	}

	public final String getStyle() {
		return IModelDecomposition.B_STYLE;
	}

	/**
	 * @TODO To be implemented.
	 */
	public final void perform(final IProgressMonitor monitor)
			throws RodinDBException {
	}
}
