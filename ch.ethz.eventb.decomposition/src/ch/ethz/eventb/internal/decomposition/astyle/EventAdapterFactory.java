/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 ******************************************************************************/

package ch.ethz.eventb.internal.decomposition.astyle;

import org.eclipse.core.runtime.IAdapterFactory;
import org.rodinp.core.IInternalElement;

import ch.ethz.eventb.decomposition.astyle.IExternalElement;


@SuppressWarnings("unchecked")
public class EventAdapterFactory implements IAdapterFactory {

	private static final Class[] ADAPTERS = new Class[] { IExternalElement.class, };

	public final IExternalElement getAdapter(final Object adaptableObject,
			final Class adapterType) {
		if (IExternalElement.class.equals(adapterType)) {
			return new Event((IInternalElement) adaptableObject);
		}
		return null;
	}
	
	public final Class[] getAdapterList() {
		return ADAPTERS;
	}

}
