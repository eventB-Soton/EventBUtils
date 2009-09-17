/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.decomposition.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eventb.ui.EventBUIPlugin;

import ch.ethz.eventb.internal.decomposition.ui.wizards.DecompositionWizard;

/**
 * The class used to handle the Decompose action. <br>
 * This action is contributed into the Event-B explorer's view and its popup
 * menu.
 */
public class Decompose implements IViewActionDelegate, IObjectActionDelegate {

	/**
	 * The current selection.
	 */
	private ISelection selection;

	public void init(final IViewPart viewPart) {
		// do nothing
	}

	public final void run(final IAction action) {
		DecompositionWizard wizard = new DecompositionWizard();
		if (selection instanceof IStructuredSelection) {
			wizard.init(EventBUIPlugin.getDefault().getWorkbench(),
					(IStructuredSelection) selection);
		}
		WizardDialog dialog = new WizardDialog(EventBUIPlugin
				.getActiveWorkbenchShell(), wizard);
		dialog.create();
		dialog.open();
	}

	public final void selectionChanged(final IAction action,
			final ISelection selection) {
		this.selection = selection;
	}

	public final void setActivePart(final IAction action,
			final IWorkbenchPart targetPart) {
		// do nothing
	}

}
