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

import java.util.ArrayList;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.IElementType;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.inst.ui.utils.Utils;

/**
 * <p>
 * The content provider for Rodin elements.
 * </p>
 * 
 * @param <T>
 *            the type of the children to be returned.
 * @author htson
 */
public class RodinElementContentProvider<T extends IRodinElement> implements
		IStructuredContentProvider {

	private static final Object[] NO_OBJECT = new Object[0];
	
	/** The element type of the children. */
	private IElementType<T> type;

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            the element type of the children.
	 */
	public RodinElementContentProvider(final IElementType<T> type) {
		this.type = type;
	}

	public final Object[] getElements(final Object inputElement) {
		try {
			// If the input element is a Rodin project
			if (inputElement instanceof IRodinProject) {
				IRodinProject project = (IRodinProject) inputElement;
				// If the type is IMachineRoot then return the list of
				// IMachineRoot.
				if (type == IMachineRoot.ELEMENT_TYPE) {
					return project.getRootElementsOfType((IInternalElementType<?>) type);
				}
				// If the type is IContextRoot then return the list of
				// IContextRoot.
				if (type == IContextRoot.ELEMENT_TYPE) {
					return project.getRootElementsOfType((IInternalElementType<?>) type);
				}
			}

			// If the input element is a machine
			if (inputElement instanceof IMachineRoot
					&& type == IEvent.ELEMENT_TYPE) {
				Collection<IEvent> result = new ArrayList<IEvent>();
				for (IEvent evt : ((IMachineRoot) inputElement).getEvents()) {
					if (!evt.isInitialisation()) {
						result.add(evt);
					}
				}
				return result.toArray(new IEvent[result.size()]);
			}

			// If the input element is a parent, then return the list of
			// children of the given type.
			if (inputElement instanceof IParent) {
				return ((IParent) inputElement).getChildrenOfType(type);
			}

		} catch (RodinDBException e) {
			Utils.log(e, "While querying elements of " + inputElement);
		}
		return NO_OBJECT;
	}

	public void dispose() {
		// Do nothing
	}

	public final void inputChanged(final Viewer viewer, final Object oldInput,
			final Object newInput) {
		// Do nothing
	}

}
