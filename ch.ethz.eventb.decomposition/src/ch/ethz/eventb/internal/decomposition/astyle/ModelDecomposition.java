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

package ch.ethz.eventb.internal.decomposition.astyle;

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
 *         An implementation of {@link IModelDecomposition} for shared variables
 *         (A-style) decomposition.
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
	public ModelDecomposition(final IMachineRoot mch) {
		super(mch);
	}

	public final ISubModel createSubModel() {
		return new DefaultSubModel(this);
	}

	public final String getStyle() {
		return IModelDecomposition.A_STYLE;
	}

	public final void perform(final IProgressMonitor monitor)
			throws RodinDBException {
		AStyleUtils.decompose(this, monitor);
	}

	public final boolean check(final IProgressMonitor monitor)
			throws RodinDBException {
		if (!super.check(monitor))
			return false;

		return AStyleUtils.check(this, monitor);
	}
}
