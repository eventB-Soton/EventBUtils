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

package ch.ethz.eventb.internal.decomposition.astyle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.astyle.INatureElement;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

import static ch.ethz.eventb.decomposition.astyle.EventBAttributes.NATURE_ATTRIBUTE;

/**
 * Implementation of Event-B variables as an extension of the Rodin database.
 */
public class Variable implements INatureElement {

	/** The underlying variable. */
	private IInternalElement variable;

	/**
	 * Builds a new variable with the private/shared attribute.
	 * 
	 * @param variable
	 *            the underlying variable
	 */
	public Variable(final IInternalElement variable) {
		this.variable = variable;
	}

	public final boolean hasNature() throws RodinDBException {
		return true;
	}

	public final void setNature(Nature value, IProgressMonitor monitor)
			throws RodinDBException {
		if (value.equals(Nature.PRIVATE)) {
			variable.removeAttribute(NATURE_ATTRIBUTE, monitor);
		} else {
			int nature = value.getCode();
			variable.setAttributeValue(NATURE_ATTRIBUTE, nature, monitor);
		}
	}

	public final Nature getNature() throws RodinDBException {
		if (variable.hasAttribute(NATURE_ATTRIBUTE)) {
			int nature = variable.getAttributeValue(NATURE_ATTRIBUTE);
			try {
				return Nature.valueOf(nature);
			} catch (IllegalArgumentException e) {
				throw EventBUtils.newRodinDBException(
						Messages.database_VariableInvalidNatureFailure, this);
			}
		}
		return Nature.PRIVATE;
	}

}
