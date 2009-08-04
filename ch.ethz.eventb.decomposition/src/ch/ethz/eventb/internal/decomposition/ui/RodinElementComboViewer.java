/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.decomposition.ui;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.rodinp.core.IElementType;
import org.rodinp.core.IRodinElement;


/**
 * @author htson
 *         <p>
 *         An extension of {@link ComboViewer} to provide a way to choose a
 *         Rodin element from a combo list. The list of objects in the combo are of
 *         the same type (set by the constructor), and belong to the same parent
 *         (which can be dynamically set after initialized).
 *         {@link #setInput(Object)}). This is copied from the pattern plug-in
 *         {@link ch.ethz.eventb.internal.patterns.wizards.ElementChooserViewer}
 *         </p>
 * @param <T>
 *            a type which extends <tt>IRodinElement</tt>
 */
public class RodinElementComboViewer<T extends IRodinElement> extends ComboViewer {

	/**
	 * The constructor. Creates the viewer by calling super and then setting 
	 * the content provider and label provider automatically.
	 * 
	 * @param container
	 *            the parent composite to create the widget.
	 * @param type
	 *            the type of elements to store in the combo list.
	 */
	public RodinElementComboViewer(Composite container, IElementType<T> type) {
		super(container);
		this.setContentProvider(new RodinElementContentProvider<T>(type));
		this.setLabelProvider(new RodinElementLabelProvider());
	}

	/**
	 * Returns the current selected element in the combo list or
	 * <code>null</code> if none is selected.
	 * 
	 * @return the current selected element or <code>null</code> if none is
	 *         selected.
	 */
	@SuppressWarnings("unchecked")
	public T getElement() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		return (T) selection.getFirstElement();
	}

}
