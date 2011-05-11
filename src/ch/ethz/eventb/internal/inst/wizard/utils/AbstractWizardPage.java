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

import java.util.ArrayList;

import java.util.Collection;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;

import ch.ethz.eventb.internal.inst.ui.utils.Utils;

/**
 * <p>
 * Abstract class for wizard page with could be act as changed provider and
 * listener. A wizard page can listen to changes from the other wizard pages.
 * Moreover, this class also is a selection changed listener with the default
 * response is to fire change to the other wizard page listeners.
 * </p>
 * 
 * @author htson
 */
public abstract class AbstractWizardPage extends WizardPage implements
		IWizardPageChangedProvider, IWizardPageChangedListener,
		ISelectionChangedListener {

	// The list of wizard page listeners. 
	private Collection<IWizardPageChangedListener> listeners;
	
	// A flag for if changes are being fired.
	private boolean firing = false;

	/**
	 * Constructor.
	 * 
	 * @param pageName
	 *            the name of the wizard page.
	 */
	protected AbstractWizardPage(String pageName) {
		super(pageName);
		listeners = new ArrayList<IWizardPageChangedListener>();
	}
	
	
	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.inst.wizard.utils.IWizardPageChangedProvider#addWizardPageChangedListener(ch.ethz.eventb.internal.inst.wizard.utils.IWizardPageChangedListener)
	 */
	public void addWizardPageChangedListener(IWizardPageChangedListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Utility method for firing changes.
	 */
	private void fireChanged() {
		if (firing) {
			// Don't fire again while already doing it.
			return;
		}

		if (listeners.size() == 0) {
			// Don't fire if there is no listeners.
			return;
		}

		// Save the list of listeners before notifying listeners about changes. 
		IWizardPageChangedListener[] savedListeners = new IWizardPageChangedListener[listeners
				.size()];
		listeners.toArray(savedListeners);

		try {
			// Set the flag and notify the listeners.
			firing = true;
			for (IWizardPageChangedListener listener : savedListeners)
				notifyListener(listener);
		} finally {
			// REmember to reset the flag when finish.
			firing = false;
		}
	}

	/**
	 * Utility method for notify listener.
	 * 
	 * @param listener
	 *            the listener to be notified.
	 */
	private void notifyListener(final IWizardPageChangedListener listener) {

		// Wrap callback with Safe runnable for subsequent listeners
		// to be called when some are causing grief
		SafeRunner.run(new ISafeRunnable() {
			public void handleException(Throwable exception) {
				Utils.log(exception,
						"Exception within page change notification"); //$NON-NLS-1$
			}

			public void run() throws Exception {
				listener.pageChanged();
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		// When the selection change then fire changes.
		fireChanged();
	}
	
}