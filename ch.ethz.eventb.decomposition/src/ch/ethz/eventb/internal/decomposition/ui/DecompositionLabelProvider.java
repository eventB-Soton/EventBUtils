/*****************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Systerel - initial API and implementation
 ****************************************************************************/
package ch.ethz.eventb.internal.decomposition.ui;

import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.decomposition.ISubModel;

/**
 * A label provider for the elements used in the decomposition.
 */
public class DecompositionLabelProvider extends RodinElementLabelProvider {

	@Override
	public final String getText(final Object element) {
		if (element instanceof IModelDecomposition) {
			return ((IModelDecomposition) element).getStyle();
		}
		
		if (element instanceof ISubModel) {
			return ((ISubModel) element).getProjectName();
		}
		
		return super.getText(element);
	}

}
