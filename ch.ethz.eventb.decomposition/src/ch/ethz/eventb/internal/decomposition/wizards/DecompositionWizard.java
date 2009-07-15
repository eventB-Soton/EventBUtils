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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eventb.core.IEventBProject;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

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
		// TODO Initial setup
	}

	/**
	 * Adding the pages to the wizard.
	 */
	public void addPages() {
		evtDistPage = new EventDistributionWizardPage(selection);
		addPage(evtDistPage);

		// TODO Create extra pages
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {

		final IMachineRoot machine = evtDistPage.getDecomposingMachine();
		final IModelDistribution elemDist = evtDistPage
				.getElementDistribution();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					// TODO Put all the argument here
					doFinish(machine, elemDist, monitor);

				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.subTask("Cleanup");

					// TODO Clean-up actions here
					monitor.worked(1);
					monitor.done();
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

	@Override
	public boolean performCancel() {
		// TODO What do I need to do when user cancels the process?
		return super.performCancel();
	}

	protected void doFinish(IMachineRoot src, IModelDistribution elemDist,
			IProgressMonitor monitor) throws CoreException {
		IElementDistribution[] distributions = elemDist.getElementDistributions();

		// The number of work is the number of distributions.
		monitor.beginTask("Generating sub-models", distributions.length);
		for (IElementDistribution dist : distributions) {
			// For each distribution, create the corresponding model.
			monitor.subTask("Create sub-model");
			createSubModel(src, dist, new SubProgressMonitor(monitor, 1));
		}
		monitor.done();
		return;
	}

	private void createSubModel(IMachineRoot src, IElementDistribution dist,
			SubProgressMonitor monitor) throws RodinDBException {
		// Monitor has 8 works.
		monitor.beginTask("Create sub-model", 8);
		src = (IMachineRoot) src.getSnapshot();
		
		// 1: Create project
		monitor.subTask("Creating new projects");
		IEventBProject prj = EventBUtils.createProject(dist.getProjectName(),
				new NullProgressMonitor());
		monitor.worked(1);

		// 2: Copy contexts from the original project
		monitor.subTask("Copying contexts");
		EventBUtils.copyContexts(src.getEventBProject(), prj,
				new NullProgressMonitor());
		monitor.worked(1);

		// 3: Create machine.
		monitor.subTask("Create machine");
		IMachineRoot dest = EventBUtils.createMachine(prj, src.getElementName(),
				new NullProgressMonitor());
		monitor.worked(1);

		// 4: Copy SEES clause.
		monitor.subTask("Copy SEES clause");
		EventBUtils.copySeesClauses(src, dest, new NullProgressMonitor());
		monitor.worked(1);

		// 5: Create variables.
		monitor.subTask("Create common variables");
		DecompositionUtils.createVariables(dest, dist, new SubProgressMonitor(
				monitor, 1));

		// 6: Create invariants.
		monitor.subTask("Create invariants");
		DecompositionUtils.createInvariants(dest, dist, new SubProgressMonitor(
				monitor, 1));

		// 7: Create events.
		monitor.subTask("Create external events");
		DecompositionUtils.createEvents(dest, dist, new SubProgressMonitor(
				monitor, 1));

		// 8: Save the resulting sub-model.
		dest.getRodinFile().save(new SubProgressMonitor(monitor, 1), false);
		monitor.done();
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