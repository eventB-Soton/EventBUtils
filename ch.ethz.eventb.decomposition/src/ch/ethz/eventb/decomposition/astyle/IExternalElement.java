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
package ch.ethz.eventb.decomposition.astyle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rodinp.core.RodinDBException;

/**
 * Common protocol for Event-B events.
 */
public interface IExternalElement {

	/**
	 * Checks whether the external value is defined or not.
	 * 
	 * @return whether the external value is defined or not
	 * @throws RodinDBException
	 *             if there was a problem accessing when the database
	 */
	boolean hasExternal() throws RodinDBException;

	/**
	 * Returns whether the event is external or not.
	 * 
	 * @return <code>true</code> if and only if the event is external.
	 * @throws RodinDBException
	 *             if there was a problem when accessing the database
	 */
	boolean isExternal() throws RodinDBException;

	/**
	 * Sets the event as external.
	 * 
	 * @param external
	 *            the external status
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @throws RodinDBException
	 *             if there was a problem when accessing the database
	 */
	void setExternal(boolean external, IProgressMonitor monitor)
			throws RodinDBException;
}
