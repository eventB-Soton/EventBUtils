/*****************************************************************************
 * Copyright (c) 2009 Systerel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     Systerel - initial API and implementation
 ****************************************************************************/

package ch.ethz.eventb.internal.decomposition.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.decomposition.ISubModel;

/**
 * The content provider for the elements used in the decomposition.
 */
public class DecompositionContentProvider implements ITreeContentProvider {

	public final Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof IModelDecomposition) {
			return ((IModelDecomposition) parentElement).getSubModels();
		}

		if (parentElement instanceof ISubModel) {
			return ((ISubModel) parentElement).getElements();
		}

		return new Object[0];
	}

	public final Object getParent(final Object element) {
		return null;
	}

	public final boolean hasChildren(final Object element) {
		return getChildren(element).length != 0;
	}

	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	public final void dispose() {
		// do nothing
	}

	public final void inputChanged(final Viewer viewer, final Object oldInput,
			final Object newInput) {
		// do nothing
	}

}
