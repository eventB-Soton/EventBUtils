/*******************************************************************************
 * Copyright (c) 2015 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University of Southampton - initial API and implementation
 *******************************************************************************/

package ch.ethz.eventb.ui.utils;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eventb.core.IEventBRoot;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.ILabeledElement;
import org.rodinp.core.RodinDBException;

/**
 * <p>
 * An impmentation of {@link ILabelProvider} extending {@link LabelProvider} to
 * provide label information, i.e., text and images for Event-B elements.
 * </p>
 *
 * @author htson
 * @version 0.1
 * @since 0.2.2
 */
public class EventBLabelProvider extends LabelProvider implements
		ILabelProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see LabelProvider#getImage(Object)
	 */
	@Override
	public Image getImage(Object element) {
		return super.getImage(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see LabelProvider#getText(Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IEventBRoot) {
			return ((IEventBRoot) element).getComponentName();
		}

		if (element instanceof ILabeledElement) {
			try {
				return ((ILabeledElement) element).getLabel();
			} catch (RodinDBException e) {
				e.printStackTrace();
				return "Labeled Element";
			}
		}
		if (element instanceof IIdentifierElement) {
			try {
				return ((IIdentifierElement) element).getIdentifierString();
			} catch (RodinDBException e) {
				e.printStackTrace();
				return "Identifier Element";
			}
		}

		return super.getText(element);
	}

}
