/*******************************************************************************
 * Copyright (c) 2010 ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.inst.wizard.utils;

/**
 * <p>
 * Utility class contains static methods.
 * <ul>
 * <li>Debugging flag and debugging method.</li>
 * </ul>
 * </p>
 * @author htson
 *
 */
public class UIUtils {

	private UIUtils() {
		// Utility classes shall not have a public or default constructor.
	}

	/**
	 * The debug flag. This is set by the option when the platform is launched.
	 * Client should not try to reset this flag.
	 */
	public static boolean DEBUG = false;

	/**
	 * The debug prefix that should be added to all UI debug message.
	 */
	public static String DEBUG_PREFIX = "Generic Instantiation (UI)";
	
	/**
	 * Utility method for printing a UI debug message. The debug prefix
	 * {@link #DEBUG_PREFIX} is added accordingly.
	 * 
	 * @param message
	 *            the debug message.
	 */
	public static void debug(String message) {
		System.out.println(DEBUG_PREFIX + "***" + message);
	}
}
