/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     ETH Zurich - initial API and implementation
 ****************************************************************************/

package ch.ethz.eventb.internal.decomposition.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.rodinp.core.IRodinElement;

import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;

/**
 * @author htson
 *         <p>
 *         A label provider for Rodin elements {@link IRodinElement}.
 *         </p>
 */
public class RodinElementLabelProvider extends LabelProvider {

	@Override
	public String getText(final Object element) {
		if (element instanceof IRodinElement) {
			return EventBUtils.getDisplayedText((IRodinElement) element);
		}

		return super.getText(element);
	}

}
