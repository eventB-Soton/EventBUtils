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

package ch.ethz.eventb.internal.decomposition.sc.astyle;

import static org.eclipse.core.resources.IMarker.SEVERITY_ERROR;

import java.text.MessageFormat;

import org.rodinp.core.IRodinProblem;

import ch.ethz.eventb.decomposition.DecompositionPlugin;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * A problem detected by the static checker, which is related to the
 * decomposition.
 */
public enum DecompositionProblem implements IRodinProblem {
	VariableHasDisappearedError(SEVERITY_ERROR,
			Messages.scuser_VariableHasDisappearedError), VariableInvalidNatureError(
			SEVERITY_ERROR, Messages.scuser_VariableInvalidNatureError), EventHasDisappearedError(
			SEVERITY_ERROR, Messages.scuser_EventHasDisappearedError), EventInvalidStatusError(
			SEVERITY_ERROR, Messages.scuser_EventInvalidStatusError), ParametersInExternalEventError(
			SEVERITY_ERROR, Messages.scuser_ParametersInExternalEventError), GuardsInExternalEventError(
			SEVERITY_ERROR, Messages.scuser_ParametersInExternalEventError), ActionsInExternalEventError(
			SEVERITY_ERROR, Messages.scuser_ActionsInExternalEventError), ActionOnPrivateAndSharedError(
			SEVERITY_ERROR, Messages.scuser_ActionOnPrivateAndSharedError), ActionOnSharedError(
			SEVERITY_ERROR, Messages.scuser_ActionOnSharedError);

	private final String errorCode;
	private final String message;
	private final int severity;
	private int arity;

	private DecompositionProblem(String message) {
		this(SEVERITY_ERROR, message);
	}

	private DecompositionProblem(int severity, String message) {
		this.severity = severity;
		this.message = message;
		this.errorCode = DecompositionPlugin.PLUGIN_ID + "." + name(); //$NON-NLS-1$
		this.arity = -1;
	}

	public int getSeverity() {
		return severity;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getLocalizedMessage(Object[] args) {
		return MessageFormat.format(message, args);
	}

	/**
	 * Returns the number of parameters needed by the message of this problem,
	 * <i>i.e.</i> the length of the object array to be passed to
	 * <code>getLocalizedMessage()</code>.
	 * 
	 * @return the number of parameters needed by the message of this problem
	 */
	public int getArity() {
		if (arity == -1) {
			MessageFormat mf = new MessageFormat(message);
			arity = mf.getFormatsByArgumentIndex().length;
		}
		return arity;
	}

	/**
	 * Gets the problem associated to the specified error code.
	 * 
	 * @param errorCode
	 *            the error code to be considered
	 * @return the problem with the specified error code
	 */
	public static DecompositionProblem valueOfErrorCode(String errorCode) {
		String instName = errorCode.substring(errorCode.lastIndexOf('.') + 1);
		return valueOf(instName);
	}

}
