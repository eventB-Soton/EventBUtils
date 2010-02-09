/*******************************************************************************
 * Copyright (c) 2009 ETH Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.utils;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.ethz.eventb.internal.utils.messages"; //$NON-NLS-1$
	
	// Progress monitor strings
	public static String createContext;
	
	// Error strings
	public static String error_existingcontext;
	public static String error_existingmachine;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
