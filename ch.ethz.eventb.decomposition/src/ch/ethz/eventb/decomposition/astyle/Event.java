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
import org.eventb.core.IEvent;
import org.rodinp.core.RodinDBException;

import static ch.ethz.eventb.decomposition.astyle.EventBAttributes.EXTERNAL_ATTRIBUTE;

/**
 * Implementation of Event-B events as an extension of the Rodin database.
 */
public class Event implements IExternalElement {

	private IEvent event;

	public Event(IEvent event) {
		this.event = event;
	}

	public boolean hasExternal() throws RodinDBException {
		return event.hasAttribute(EXTERNAL_ATTRIBUTE);
	}

	public boolean isExternal() throws RodinDBException {
		return event.getAttributeValue(EXTERNAL_ATTRIBUTE);
	}

	public void setExternal(boolean external, IProgressMonitor monitor)
			throws RodinDBException {
		event.setAttributeValue(EXTERNAL_ATTRIBUTE, external, monitor);
	}

}
