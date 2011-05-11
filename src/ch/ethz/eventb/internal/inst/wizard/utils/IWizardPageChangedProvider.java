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
 * A common interface for wizard page having information which effect other
 * wizard page.
 * </p>
 * 
 * @see IWizardPageChangedListener
 * 
 * @author htson
 */
public interface IWizardPageChangedProvider {

	/**
	 * Adds a listener for wizard page changes in this wizard page change
	 * provider. Has no effect if an identical listener is already registered.
	 * 
	 * @param listener
	 *            a wizard page changed listener
	 */
	public void addWizardPageChangedListener(IWizardPageChangedListener listener);

}
