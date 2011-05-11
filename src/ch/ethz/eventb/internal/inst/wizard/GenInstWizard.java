/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ETH Zurich - initial API and implementation
 ****************************************************************************/
package ch.ethz.eventb.internal.inst.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import ch.ethz.eventb.inst.IGenInst;
import ch.ethz.eventb.internal.inst.ui.utils.Messages;

/**
 * <p>
 * This wizard instantiate an Event-B model.
 * </p>
 * 
 * @author htson
 */
public class GenInstWizard extends Wizard implements INewWizard {

	// The first page. Selecting of various component for instantiation.
	private ComponentWizardPage componentPage;
	
	// The second page. Instantiation.
	InstantiationWizardPage instantiationPage;
	
	// The third page. Renaming.
	RenamingWizardPage renamingPage;
	
	// The current selection of the workspace used to initialize the wizard.
	private ISelection selection;

	/**
	 * Builds a new wizard for generic instantiation.
	 */
	public GenInstWizard() {
		super();
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public final void addPages() {
		super.addPages();

		// Adds the first page to the wizard.
		componentPage = new ComponentWizardPage();
		componentPage.init(selection);
		addPage(componentPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public final IWizardPage getNextPage(final IWizardPage page) {
		if (page instanceof ComponentWizardPage) {
			Assert.isNotNull(componentPage);
			InstantiationWizardPage instantiationPage = new InstantiationWizardPage(
					componentPage);
			componentPage.addWizardPageChangedListener(instantiationPage);
			addPage(instantiationPage);
			return instantiationPage;
		}
		else if (page instanceof InstantiationWizardPage) {
			Assert.isNotNull(componentPage);
			renamingPage = new RenamingWizardPage(
					componentPage);
			componentPage.addWizardPageChangedListener(renamingPage);
			addPage(renamingPage);
			return renamingPage;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	public final boolean canFinish() {
		return (renamingPage != null && renamingPage.isPageComplete());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public final boolean performFinish() {
		final IGenInst genInst = componentPage.getGenInst();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public void run(final IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					if (genInst.check(monitor)) {
						genInst.perform(monitor);
					}
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} 
				finally {
					// TODO
				}
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			MessageDialog.openError(getShell(), Messages.error,
					e.getMessage());
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), Messages.error,
					realException.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Stores the current selection in the workbench. This selection will be
	 * used for further initialization.
	 * 
	 * @param workbench
	 *            the workbench
	 * @param selection
	 *            the current selection
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public final void init(final IWorkbench workbench,
			final IStructuredSelection selection) {
		this.selection = selection;
	}

}