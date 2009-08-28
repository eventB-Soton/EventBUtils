/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     ETH Zurich - initial API and implementation
 ****************************************************************************/

package ch.ethz.eventb.internal.decomposition.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;

import ch.ethz.eventb.decomposition.utils.EventBUtils;
import ch.ethz.eventb.decomposition.utils.Messages;
import ch.ethz.eventb.internal.decomposition.IModelDecomposition;

/**
 * This wizard decomposes an Event-B model in several sub-models.
 */
public class DecompositionWizard extends Wizard implements INewWizard {
	// The first page: decomposition style.
	private InitialWizardPage initialPage;

	// The second page
	private WizardPage decompPage;

	// The current selection of the workspace used to initialize
	// the wizard.
	private ISelection selection;

	/**
	 * Constructor for <tt>DecompositionWizard</tt>.
	 */
	public DecompositionWizard() {
		super();
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
	}

	/**
	 * Adds the pages to the wizard.
	 */
	public void addPages() {
		super.addPages();
		initialPage = new InitialWizardPage();
		addPage(initialPage);
	}

	public IWizardPage getNextPage(IWizardPage page) {
		if (page == initialPage) {
			final IModelDecomposition modelDecomp = initialPage
					.getModelDecomposition();
			if (modelDecomp.getStyle().equals(IModelDecomposition.A_STYLE)) {
				decompPage = new ElementPartitionWizardPage<IEvent>(
						modelDecomp, selection, IEvent.ELEMENT_TYPE);
			} else if (modelDecomp.getStyle().equals(
					IModelDecomposition.B_STYLE)) {
				decompPage = new ElementPartitionWizardPage<IVariable>(
						modelDecomp, selection, IVariable.ELEMENT_TYPE);
			} else {
				return null;
			}
			addPage(decompPage);
			return decompPage;
		}
		return null;
	}

	public boolean canFinish() {
		return (decompPage != null && decompPage.isPageComplete());
	}

	public boolean performFinish() {
		final IModelDecomposition decomp = initialPage.getModelDecomposition();

		IRunnableWithProgress op = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					decomp.perform(monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					IMachineRoot mch = decomp.getMachineRoot();
					if (mch != null)
						EventBUtils.cleanUp(mch, monitor);
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), Messages.wizard_error,
					realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Stores the current selection in the workbench. This selection will be
	 * used for further initialization.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

}