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
import org.eventb.internal.ui.eventbeditor.manipulation.IAttributeManipulation;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.astyle.INatureElement;
import ch.ethz.eventb.decomposition.astyle.INatureElement.Nature;
import ch.ethz.eventb.decomposition.utils.Messages;

public class NatureAttributeManipulation implements IAttributeManipulation {

	/** The private attribute. */
	public final static String PRIVATE = Messages.attributeManipulation_nature_private;

	/** The shared attribute. */
	public final static String SHARED = Messages.attributeManipulation_nature_shared;

	private INatureElement asNature(IRodinElement element) {
		return (INatureElement) element.getAdapter(INatureElement.class);
	}

	public String getValue(IRodinElement element, IProgressMonitor monitor)
			throws RodinDBException {
		final Nature nature = asNature(element).getNature();
		if (nature == Nature.PRIVATE)
			return PRIVATE;
		if (nature == Nature.SHARED)
			return SHARED;
		return PRIVATE;
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
		asNature(element).setNature(Nature.PRIVATE, monitor);
	}

	public boolean hasValue(IRodinElement element, IProgressMonitor monitor)
			throws RodinDBException {
		return asNature(element).hasNature();
	}

}
