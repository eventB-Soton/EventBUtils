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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;

/**
 * <p>
 * An extension of {@link ViewerFilter} for selecting a certain Event-B internal
 * elements according to their type.
 * </p>
 *
 * @author htson
 * @version 0.1
 * @see IInternalElement
 * @since 0.2.2
 */
public class EventBViewerFilter extends ViewerFilter {

	// The acceptance Event-B internal element types.
	private Collection<IInternalElementType<? extends IInternalElement>> types;

	/**
	 * Public constructor to create a filter accepting only element with the
	 * given input types.
	 * 
	 * @param types
	 *            the acceptance internal element types.
	 */
	public EventBViewerFilter(
			IInternalElementType<? extends IInternalElement>... types) {
		this.types = new ArrayList<IInternalElementType<? extends IInternalElement>>();
		for (IInternalElementType<? extends IInternalElement> type : types) {
			this.types.add(type);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ViewerFilter#select(Viewer, Object, Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IInternalElement) {
			IInternalElementType<? extends IInternalElement> elementType = ((IInternalElement) element)
					.getElementType();

			return types.contains(elementType);
		}
		return true;
	}

}
