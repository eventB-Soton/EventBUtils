/*******************************************************************************
 * Copyright (c) 2009 Systerel
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.decomposition.ui;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;

import ch.ethz.eventb.internal.decomposition.IModelDecomposition;

/**
 * An extension of {@link ComboViewer} to provide a way to choose a
 * decomposition style from a combo list.
 * 
 * @param <T>
 *            a type which extends <tt>IModelDecomposition</tt>
 */
public class DecompositionComboViewer<T extends IModelDecomposition> extends
		ComboViewer {

	/**
	 * The constructor. Creates the viewer by calling super and then setting the
	 * content provider and label provider automatically.
	 * 
	 * @param container
	 *            the parent composite to create the widget.
	 */
	public DecompositionComboViewer(Composite container) {
		super(container);
		setContentProvider(new DecompositionContentProvider() {
			public Object[] getElements(Object inputElement) {
				return new Object[] { inputElement };
			}
		});
		setLabelProvider(new DecompositionLabelProvider());
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
