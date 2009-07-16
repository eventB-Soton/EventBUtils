/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     ETH Zurich - initial API and implementation
 ****************************************************************************/

package ch.ethz.eventb.internal.decomposition.wizards;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.ILabeledElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         A label provider for Rodin element {@link IRodinElement}.
 *         </p>
 */
public class RodinElementLabelProvider extends LabelProvider implements
		IBaseLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		return super.getImage(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		
		// For labelled elements, return the labels. 
		if (element instanceof ILabeledElement) {
			try {
				return ((ILabeledElement) element).getLabel();
			} catch (RodinDBException e) {
				return super.getText(element);
			}
		}
		
		// For identifier elements, return the identifiers.
		if (element instanceof IIdentifierElement) {
			try {
				return ((IIdentifierElement) element).getIdentifierString();
			} catch (RodinDBException e) {
				return super.getText(element);
			}
	    }
		
		return super.getText(element);
	}

}
