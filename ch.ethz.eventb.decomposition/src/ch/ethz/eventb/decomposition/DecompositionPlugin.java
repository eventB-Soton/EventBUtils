/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     ETH Zurich - initial API and implementation
 ****************************************************************************/

package ch.ethz.eventb.decomposition;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class DecompositionPlugin extends AbstractUIPlugin {

	/** The plug-in ID. */
	public static final String PLUGIN_ID = "ch.ethz.eventb.decomposition"; //$NON-NLS-1$

	/** The shared instance. */
	private static DecompositionPlugin plugin;

	/**
	 * The constructor.
	 */
	public DecompositionPlugin() {
	}

	public final void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public final void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static DecompositionPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns the active workbench window.
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
}
