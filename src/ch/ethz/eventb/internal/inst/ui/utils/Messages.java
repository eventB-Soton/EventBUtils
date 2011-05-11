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

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.ethz.eventb.internal.inst.utils.messages"; //$NON-NLS-1$
	
	// Wizard strings
	public static String wizard_componentpage_name;
	public static String wizard_componentpage_title;
	public static String wizard_componentpage_description;
	public static String wizard_instantiationpage_name;
	public static String wizard_instantiationpage_title;
	public static String wizard_instantiationpage_description;

	// Label strings
	public static String label_available;
	public static String label_selected;
	
	// Title strings
	public static String title_contextschooser;
	
	// Misc.
	public static String error;
	public static String cleaningUp;

	// Progress monitor strings
	public static String createContext;
	public static String createSeenClauses;
		
	// Generic instantiation.
	public static String genericinstantiation_description;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
