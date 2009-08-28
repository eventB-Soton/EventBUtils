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

package ch.ethz.eventb.decomposition.astyle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IVariable;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.decomposition.utils.EventBUtils;
import ch.ethz.eventb.decomposition.utils.Messages;

import static ch.ethz.eventb.decomposition.astyle.EventBAttributes.NATURE_ATTRIBUTE;

/**
 * Implementation of Event-B variables as an extension of the Rodin database.
 */
public class Variable implements INatureElement {

	private IVariable variable;

	public Variable(IVariable variable) {
		this.variable = variable;
	}

	public boolean hasNature() throws RodinDBException {
		return variable.hasAttribute(NATURE_ATTRIBUTE);
	}

	public void setNature(Nature value, IProgressMonitor monitor)
			throws RodinDBException {
		int intValue = value.getCode();
		variable.setAttributeValue(NATURE_ATTRIBUTE, intValue, monitor);
	}

	public Nature getNature() throws RodinDBException {
		int intValue = variable.getAttributeValue(NATURE_ATTRIBUTE);
		try {
			return Nature.valueOf(intValue);
		} catch (IllegalArgumentException e) {
			throw EventBUtils.newRodinDBException(
					Messages.database_VariableInvalidNatureFailure, this);
		}
	}

}
