/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Copied from ch.ethz.eventb.decomposition plugin
 ****************************************************************************/

package ch.ethz.eventb.internal.inst.wizard.utils;

import org.eclipse.jface.viewers.LabelProvider;

import org.rodinp.core.IRodinElement;

import ch.ethz.eventb.internal.inst.ui.utils.Utils;

/**
 * <p>
 * A label provider for Rodin elements {@link IRodinElement}.
 * </p>
 * 
 * @author htson
 */
public class RodinElementLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(final Object element) {
		if (element instanceof IRodinElement) {
			return Utils.getDisplayText(element);
		}

		return super.getText(element);
	}

}
