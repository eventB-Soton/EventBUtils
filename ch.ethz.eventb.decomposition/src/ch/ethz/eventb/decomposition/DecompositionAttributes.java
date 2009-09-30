/*****************************************************************************
 * Copyright (c) 2009 Systerel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     Systerel - initial API and implementation
 ****************************************************************************/

package ch.ethz.eventb.decomposition;

import org.rodinp.core.IAttributeType;
import org.rodinp.core.RodinCore;

import ch.ethz.eventb.decomposition.DecompositionPlugin;

/**
 * This interface lists all attribute names used by the A-style decomposition
 * plug-in.
 */
public interface DecompositionAttributes {

	/**
	 * The attribute used to tag the contexts / machines as decomposed or not.
	 */
	IAttributeType.Boolean DECOMPOSED_ATTRIBUTE = RodinCore
			.getBooleanAttrType(DecompositionPlugin.PLUGIN_ID + ".decomposed");
}
