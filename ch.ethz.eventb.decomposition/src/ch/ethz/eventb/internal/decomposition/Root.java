/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/

package ch.ethz.eventb.internal.decomposition;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IEventBRoot;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.IDecomposedElement;

import static ch.ethz.eventb.decomposition.DecompositionAttributes.DECOMPOSED_ATTRIBUTE;

/**
 * Implementation of Event-B contexts / machines as an extension of the Rodin
 * database.
 */
public class Root implements IDecomposedElement {

	/** The underlying Event-B root. */
	private IEventBRoot root;

	/**
	 * Builds a new root with the decomposed attribute.
	 * 
	 * @param root
	 *            the underlying Event-B root
	 */
	public Root(final IEventBRoot root) {
		this.root = root;
	}

	public final boolean hasDecomposed() throws RodinDBException {
		return true;
	}

	public final boolean isDecomposed() throws RodinDBException {
		if (root.hasAttribute(DECOMPOSED_ATTRIBUTE)
				&& root.getAttributeValue(DECOMPOSED_ATTRIBUTE)) {
			return true;
		}
		return false;
	}

	public final void setDecomposed(IProgressMonitor monitor)
			throws RodinDBException {
		root.setAttributeValue(DECOMPOSED_ATTRIBUTE, true, monitor);
		root.setGenerated(true, monitor);
	}

}
