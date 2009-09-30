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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eventb.core.IEventBRoot;

import ch.ethz.eventb.decomposition.IDecomposedElement;

@SuppressWarnings("unchecked")
public class RootAdapterFactory implements IAdapterFactory {
	
	private static final Class[] ADAPTERS = new Class[] {
		IDecomposedElement.class,
	};

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IDecomposedElement.class.equals(adapterType)) {
			return new Root((IEventBRoot) adaptableObject);
		}
		return null;
	}

	public Class[] getAdapterList() {
		return ADAPTERS;
	}

}
