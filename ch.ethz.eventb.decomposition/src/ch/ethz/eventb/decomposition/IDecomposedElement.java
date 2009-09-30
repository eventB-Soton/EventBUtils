/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.decomposition;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rodinp.core.RodinDBException;

/**
 * Common protocol for Event-B machines and contexts.
 */
public interface IDecomposedElement {

	/**
	 * Checks whether the decomposed value is defined or not.
	 * 
	 * @return whether the decomposed value is defined or not
	 * @throws RodinDBException
	 *             if there was a problem accessing when the database
	 */
	boolean hasDecomposed() throws RodinDBException;

	/**
	 * Returns whether the context/machine is decomposed or not.
	 * 
	 * @return <code>true</code> if and only if the context/machine is decomposed.
	 * @throws RodinDBException
	 *             if there was a problem when accessing the database
	 */
	boolean isDecomposed() throws RodinDBException;

	/**
	 * Sets the context/machine as decomposed and tags it as generated.
	 * 
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @throws RodinDBException
	 *             if there was a problem when accessing the database
	 */
	void setDecomposed(IProgressMonitor monitor)
			throws RodinDBException;
}
