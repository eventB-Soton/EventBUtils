/*******************************************************************************
 * Copyright (c) 2010 ETH Zurich
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.inst.ui.utils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eventb.core.IAssignmentElement;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.ILabeledElement;
import org.eventb.core.IPredicateElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.inst.ui.GenInstUIPlugin;

/**
 * <p>
 * Utility class contain various static methods
 * </p>
 * 
 * @author htson
 */
public class Utils {

	/**
	 * Private constructor for utility class.
	 */
	private Utils() {
		// Utility classes shall not have a public or default constructor.
	}

	/**
	 * The debug flag. This is set by the option when the platform is launched.
	 * Client should not try to reset this flag.
	 */
	public static boolean DEBUG = false;

	/**
	 * The debug prefix that should be added to all debug message.
	 */
	public static String DEBUG_PREFIX = "Generic Instantiation (Global)";
	
	/**
	 * Utility method for printing a debug message. The debug prefix
	 * {@link #DEBUG_PREFIX} is added accordingly.
	 * 
	 * @param message
	 *            the debug message.
	 */
	public static void debug(String message) {
		System.out.println(DEBUG_PREFIX + "***" + message);
	}
	
	/**
	 * Logs the given exception with the given context message.
	 * 
	 * @param exc
	 *            a throwable or <code>null</code> if not applicable
	 * @param message
	 *            a context message or <code>null</code>
	 */
	public static void log(Throwable exc, String message) {
		if (exc instanceof RodinDBException) {
			final Throwable nestedExc = ((RodinDBException) exc).getException();
			if (nestedExc != null) {
				exc = nestedExc;
			}
		}
		if (message == null) {
			message = "Unknown context"; //$NON-NLS-1$
		}
		final IStatus status = makeErrorStatus(exc, message);
		GenInstUIPlugin.getDefault().getLog().log(status);
	}

	// returns a new error status with the given exception and message
	private static IStatus makeErrorStatus(Throwable exception, String message) {
		return new Status(IStatus.ERROR, GenInstUIPlugin.PLUGIN_ID,
				IStatus.OK, message, exception);
	}

	/**
	 * Return the display text of some object.
	 * 
	 * @param element
	 *            the input object.
	 * @return the display text of the object depending on its type.
	 *         <ul>
	 *         <li>If the input element is a predicate element then return the
	 *         predicate string.</li>
	 *         <li>If the input element is an assignment element then return the
	 *         assignment string.</li>
	 *         <li>If the input element is a labelled element then return the
	 *         label.</li>
	 *         <li>If the input element is an identifier element then return the
	 *         identifier string.</li>
	 *         <li>If the input element is a Rodin element then return the
	 *         (internal-)element name.</li>
	 *         <li>otherwise, return the toString() value of the input element.</li>
	 *         </ul>
	 */
	public static String getDisplayText(Object element) {

		if (element == null)
			return "NULL";
		
		// If the element has a predicate then return the predicate.
		if (element instanceof IPredicateElement) {
			try {
				return ((IPredicateElement) element).getPredicateString();
			} catch (RodinDBException e) {
				return "";
			}
		}
		
		// If the element has an assignment then return the assignment.
		if (element instanceof IAssignmentElement) {
			try {
				return ((IAssignmentElement) element).getAssignmentString();
			} catch (RodinDBException e) {
				return "";
			}
		}
		
		// If the element has label then return the label.
		if (element instanceof ILabeledElement) {
			try {
				return ((ILabeledElement) element).getLabel();
			} catch (RodinDBException e) {
				return "";
			}
		}

		// If the element has identifier string then return it.
		if (element instanceof IIdentifierElement) {
			try {
				return ((IIdentifierElement) element).getIdentifierString();
			} catch (RodinDBException e) {
				return "";
			}
		}

		// If the element is a Rodin element then return the name of the
		// element.
		if (element instanceof IRodinElement) {
			return ((IRodinElement) element).getElementName();
		}

		// Otherwise return the string corresponding to the element by
		// toString() method.
		return element.toString();
	}

}
