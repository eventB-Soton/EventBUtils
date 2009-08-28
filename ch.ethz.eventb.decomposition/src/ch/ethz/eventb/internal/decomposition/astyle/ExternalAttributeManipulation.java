/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 ******************************************************************************/

package ch.ethz.eventb.internal.decomposition.astyle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.internal.ui.eventbeditor.manipulation.IAttributeManipulation;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.decomposition.utils.Messages;

/**
 * An implementation of {@link IAttributeManipulation} providing the factory
 * methods for external attribute of events.
 */
public class ExternalAttributeManipulation implements IAttributeManipulation {

	/**
	 * Constant string for TRUE (i.e. external).
	 */
	private static final String TRUE = Messages.attributeManipulation_external_true;

	/**
	 * Constant string for FALSE (i.e. internal).
	 */
	private static final String FALSE = Messages.attributeManipulation_external_false;

	private IExternalElement asEvent(IRodinElement element) {
		return (IExternalElement) element.getAdapter(IExternalElement.class);
	}

	public String getValue(IRodinElement element, IProgressMonitor monitor)
			throws RodinDBException {
		final IExternalElement event = asEvent(element);
		return (event.hasExternal() && event.isExternal()) ? TRUE : FALSE;
	}

	public void setValue(IRodinElement element, String newValue,
			IProgressMonitor monitor) throws RodinDBException {
		// Do nothing.
	}

	public String[] getPossibleValues(IRodinElement element,
			IProgressMonitor monitor) {
		return new String[0];
	}

	public void removeAttribute(IRodinElement element, IProgressMonitor monitor)
			throws RodinDBException {
		// Do nothing.
	}

	public void setDefaultValue(IRodinElement element, IProgressMonitor monitor)
			throws RodinDBException {
		asEvent(element).setExternal(false, monitor);
	}

	public boolean hasValue(IRodinElement element, IProgressMonitor monitor)
			throws RodinDBException {
		return asEvent(element).hasExternal();
	}

}
