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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

/**
 * <p>
 * An implementation of {@link ITreeContentProvider} to populate Event-B
 * elements for a Tree viewer.
 * </p>
 *
 * @author htson
 * @version 0.1
 * @since 0.2.2
 */
public class EventBContentProvider implements ITreeContentProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITreeContentProvider#getElements(Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IRodinProject) {
			try {
				IRodinFile[] rodinFiles = ((IRodinProject) parentElement)
						.getRodinFiles();
				Collection<IInternalElement> roots = new ArrayList<IInternalElement>(
						rodinFiles.length);
				for (IRodinFile rodinFile : rodinFiles) {
					IInternalElement root = rodinFile.getRoot();
					roots.add(root);
				}
				return roots.toArray(new IInternalElement[roots.size()]);
			} catch (RodinDBException e) {
				e.printStackTrace();
				return new Object[0];
			}
		}

		if (parentElement instanceof IParent) {
			try {
				return ((IParent) parentElement).getChildren();
			} catch (RodinDBException e) {
				e.printStackTrace();
				return new Object[0];
			}
		}

		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITreeContentProvider#getParent(Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof IRodinElement) {
			return ((IRodinElement) element).getParent();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
	 * Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length != 0;
	}

}
