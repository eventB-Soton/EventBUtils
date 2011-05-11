package ch.ethz.eventb.inst.ui;

import org.eclipse.core.runtime.Platform;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.ethz.eventb.internal.inst.ui.utils.Utils;
import ch.ethz.eventb.internal.inst.wizard.utils.UIUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class GenInstUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.ethz.eventb.inst.ui"; //$NON-NLS-1$

	// The shared instance
	private static GenInstUIPlugin plugin;
	
	// Trace Options
	private static final String GLOBAL_TRACE = PLUGIN_ID + "/debug"; //$NON-NLS-1$

	private static final String GLOBAL_TRACE_UI = PLUGIN_ID + "/debug/ui"; //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public GenInstUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		configureDebugOptions();

		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GenInstUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Process debugging/tracing options coming from Eclipse.
	 */
	private void configureDebugOptions() {
		if (isDebugging()) {
			String option = Platform.getDebugOption(GLOBAL_TRACE);
			if (option != null)
				Utils.DEBUG = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			
			option = Platform.getDebugOption(GLOBAL_TRACE_UI);
			if (option != null)
				UIUtils.DEBUG = option.equalsIgnoreCase("true"); //$NON-NLS-1$

		}
	}

}
