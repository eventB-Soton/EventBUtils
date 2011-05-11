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
 * A listener which is notified when a wizard page's information changes.
 * </p>
 * 
 * @see IWizardPageChangedProvider
 * 
 * @author htson
 */
public interface IWizardPageChangedListener {

	/**
	 * Notifies that the wizard page has changed.
	 */
	public void pageChanged();
	
}
