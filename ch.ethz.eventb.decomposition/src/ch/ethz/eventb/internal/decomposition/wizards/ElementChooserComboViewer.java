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
package ch.ethz.eventb.internal.decomposition.wizards;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.IElementType;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         An extension of {@link ComboViewer} to provide a mean of choosing a
 *         Rodin Element from a Combo. The list of objects in the Combo are of
 *         the same type (set by the constructor), and belong to the same parent
 *         (which can be dynamically set after initialised
 *         {@link #setInput(Object)}). This is copied from the pattern plug-in
 *         {@link ch.ethz.eventb.internal.patterns.wizards.ElementChooserViewer}
 *         </p>
 * @param <T>
 *            any type which extends IRodinElement
 */
public class ElementChooserComboViewer<T extends IRodinElement> extends ComboViewer {

	/**
	 * @author htson
	 * <p>
	 *  Utility class to provide the content of the combo list.
	 * </p>
	 */
	private class ContentProvider implements IStructuredContentProvider {

		/**
		 * The type of the elements in the list.
		 */
		IElementType<T> type;

		/**
		 * The constructor. Stored the type of the elements in the list.
		 * 
		 * @param type
		 *            an element type
		 */
		public ContentProvider(IElementType<T> type) {
			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// Do nothing	
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
		 * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Do nothing
		}

		/**
		 * Method to return the list of objects for a given input.
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			// If the input element is a Rodin project 
			if (inputElement instanceof IRodinProject) {
				IRodinProject project = (IRodinProject) inputElement;
				// If the type is IMachineRoot then return the list of
				// IMachineRoot.
				if (type == IMachineRoot.ELEMENT_TYPE) {
					try {
						return project.getRootElementsOfType((IInternalElementType<?>) type);
					} catch (RodinDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
				}
			}
			// If the input element is a MachineRoot 
			if (inputElement instanceof IMachineRoot && type == IEvent.ELEMENT_TYPE) {
				try {
					Collection<IEvent> result = new ArrayList<IEvent>();
					for (IEvent evt : ((IMachineRoot) inputElement).getEvents())
						if (!evt.isInitialisation())
							result.add(evt);
					return result.toArray(new IEvent[result.size()]);
				} catch (RodinDBException e) {
					return null;
				}
			}	
			// If the input element is a parent, then return the list of children of the given type.
			if (inputElement instanceof IParent) {
				try {
					return ((IParent) inputElement).getChildrenOfType(type);
				} catch (RodinDBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
			
			// Otherwise return <code>null</code>
			return null;
		}
		
	}
	
	/**
	 * @author htson
	 * <p>
	 * Utility class provides labels for elements in the combo list.
	 * </p>
	 */
	private class LabelProvider implements ILabelProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			// No image
			return null;
		}

		/**
		 * Return the corresponding text for each object in the combo list.
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			assert element instanceof IRodinElement;
			return EventBUtils.getDisplayText((IRodinElement) element);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse
		 * .jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
			// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java
		 * .lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			// Ignore by always return <code>false</code>
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse
		 * .jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
			// Do nothing
		}
		
	}

	/**
	 * The constructor. Create the viewer by calling super then set the content
	 * provider and label provider automatically.
	 * 
	 * @param container
	 *            the parent composite to create the widget.
	 * @param type
	 *            the type of elements to store in the combo list.
	 */
	public ElementChooserComboViewer(Composite container, IElementType<T> type) {
		super(container);
		this.setContentProvider(new ContentProvider(type));
		this.setLabelProvider(new LabelProvider());
	}

	/**
	 * Return the current selected element in the combo list or
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
