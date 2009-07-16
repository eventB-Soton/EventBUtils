/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     ETH Zurich - initial API and implementation
 ****************************************************************************/

package ch.ethz.eventb.internal.decomposition.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * This is a wizard for decompose an Event-B Model using A-Style. Its role is to
 * collect input from developers about the decomposition (e.g. the input models,
 * the partition of the variables), verifying about the validity of the
 * decomposition and create the corresponding decomposed models. The wizard
 * creates different projects for each decomposed models. If an Event-B machine
 * is selected in the workspace when the wizard is opened, it will accept it as
 * the input model.
 */

public class DecompositionWizard extends Wizard implements INewWizard {
	// The first page: Input models and variable distribution.
	private EventDistributionWizardPage evtDistPage;

	// The current selection of of the workspace. used to initialised
	// the wizard.
	private ISelection selection;

	/**
	 * Constructor for DecompositionWizard.
	 */
	public DecompositionWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the pages to the wizard.
	 */
	public void addPages() {
		evtDistPage = new EventDistributionWizardPage(selection);
		addPage(evtDistPage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {

		final IModelDistribution modelDist = evtDistPage
				.getModelDistribution();

		IRunnableWithProgress op = new IRunnableWithProgress() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					DecompositionUtils.decomposeModel(modelDist, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					DecompositionUtils.cleanUp(modelDist, monitor);
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException
					.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

}