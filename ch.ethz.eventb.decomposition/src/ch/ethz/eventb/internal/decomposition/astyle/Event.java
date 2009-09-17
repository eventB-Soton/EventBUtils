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

package ch.ethz.eventb.internal.decomposition.astyle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.astyle.IExternalElement;

import static ch.ethz.eventb.decomposition.astyle.EventBAttributes.EXTERNAL_ATTRIBUTE;

/**
 * Implementation of Event-B events as an extension of the Rodin database.
 */
public class Event implements IExternalElement {

	/** The underlying event. */
	private IInternalElement event;

	/**
	 * Builds a new event with the external/internal attribute.
	 * 
	 * @param event
	 *            the underlying event
	 */
	public Event(final IInternalElement event) {
		this.event = event;
	}

	public final boolean hasExternal() throws RodinDBException {
		return true;
	}

	public final boolean isExternal() throws RodinDBException {
		if (event.hasAttribute(EXTERNAL_ATTRIBUTE)
				&& event.getAttributeValue(EXTERNAL_ATTRIBUTE)) {
			return true;
		}
		return false;
	}

	public final void setExternal(final boolean external,
			final IProgressMonitor monitor) throws RodinDBException {
		if (external) {
			event.setAttributeValue(EXTERNAL_ATTRIBUTE, external,
							monitor);
		} else {
			event.removeAttribute(EXTERNAL_ATTRIBUTE, monitor);
		}
	}

}
