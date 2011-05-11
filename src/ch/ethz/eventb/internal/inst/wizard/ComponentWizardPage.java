/*******************************************************************************
 * Copyright (c) 2010 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.inst.wizard;

import java.util.Collection;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEventBProject;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;

import ch.ethz.eventb.inst.GenInstPlugin;
import ch.ethz.eventb.inst.IGenInst;
import ch.ethz.eventb.internal.inst.ui.utils.Messages;
import ch.ethz.eventb.internal.inst.wizard.utils.AbstractWizardPage;
import ch.ethz.eventb.internal.inst.wizard.utils.RodinElementComboViewer;
import ch.ethz.eventb.internal.inst.wizard.utils.RodinElementListSelectionViewer;
import ch.ethz.eventb.internal.inst.wizard.utils.UIUtils;

/**
 * This wizard page allowing different instantiation components to be chosen.
 * <ul>
 * <li>The source machine.</li>
 * <li>The target project.</li>
 * <li>The seen contexts within the target project.</li>
 * </ul>
 * 
 * @author htson
 */
public class ComponentWizardPage extends AbstractWizardPage {	
	
	// Source machine chooser group.
	private MachineChooserGroup srcGroup; 

	// Seen contexts chooser group.
	private ContextsChooserGroup seenContextsGroup;
	
	// The generic instantiation information.
	private IGenInst genInst;

	// The initial source machine depending on the initial selection in the
	// workbench.
	private IMachineRoot initSrcMachine;
	
	/**
	 * The constructor.
	 */
	public ComponentWizardPage() {
		super(Messages.wizard_componentpage_name);
		setTitle(Messages.wizard_componentpage_title);
		setDescription(Messages.wizard_componentpage_description);
		genInst = GenInstPlugin.createNewGenInst();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public final void createControl(final Composite parent) {
		// Create the main composite.
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 9;
		layout.numColumns = 1;
		container.setLayout(layout);
	
		// Create the source machine chooser group.
		srcGroup = new MachineChooserGroup(container, SWT.DEFAULT);
		srcGroup.getGroup().setText("Based machine");
		srcGroup.getGroup().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		srcGroup.addSelectionChangedListener(this);
		final RodinElementComboViewer<IRodinProject> srcProjectChooser = srcGroup
				.getProjectChooser();
		srcProjectChooser
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						IRodinProject prj = srcProjectChooser.getElement();
						genInst.setSourceProjectName(prj.getElementName());
					}
				});

		final RodinElementComboViewer<IMachineRoot> machineChooser = srcGroup
				.getMachineChooser();
		machineChooser
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						IMachineRoot mch = machineChooser.getElement();
						genInst.setSourceMachineName(mch.getElementName());
					}
				});

		// Create the seen contexts chooser group.
		seenContextsGroup = new ContextsChooserGroup(container, SWT.DEFAULT);
		seenContextsGroup.getGroup().setText("Seen contexts");
		seenContextsGroup.getGroup().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		seenContextsGroup.addSelectionChangedListener(this);
		final RodinElementComboViewer<IRodinProject> targetProjectChooser = seenContextsGroup
				.getProjectChooser();
		targetProjectChooser
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						genInst
								.setTargetProject((IEventBProject) targetProjectChooser
										.getElement().getAdapter(
												IEventBProject.class));
					}
				});

		final RodinElementListSelectionViewer<IContextRoot> contextsChooser = seenContextsGroup
				.getContextsChooser();
		contextsChooser
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						Collection<IContextRoot> ctxs = contextsChooser
								.getSelected();
						String[] seenCtxNames = new String[ctxs.size()];
						int i = 0;
						for (IContextRoot ctx : ctxs) {
							seenCtxNames[i] = ctx.getComponentName();
							i++;
						}
						genInst.setSeenContextNames(seenCtxNames);
					}
				});
		
		// Initialise.
		initialise();
		
		// Adding the update status listener to different components AFTER initialsation.
		ISelectionChangedListener updateStatusListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateStatus();
			}
		};
		targetProjectChooser.addSelectionChangedListener(updateStatusListener);
		machineChooser.addSelectionChangedListener(updateStatusListener);
		srcProjectChooser.addSelectionChangedListener(updateStatusListener);
		contextsChooser.addSelectionChangedListener(updateStatusListener);
		
		// Set the page complete if there is no error.
		setPageComplete(getError() == null);

		// Set the main control of the wizard.
		setControl(container);
	}
	
	/**
	 * Utility method to update the status message and also set the completeness
	 * of the page.
	 * 
	 * @param message
	 *            the error message or <code>null</code>.
	 */
	private void updateStatus(final String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/**
	 * Utility method to update the status message by getting the error (if any)
	 * and also set the completeness of the page.
	 */
	private void updateStatus() {
		updateStatus(getError());
	}

	/**
	 * Utility method for getting the error string. If there is no error in the
	 * page, return <code>null</code>.
	 * 
	 * @return return the error string or <code>null</code>
	 */
	private String getError() {
		String basedPrj = genInst.getSourceProjectName();
		if (basedPrj == null)
			return "Undefined based project";
		String basedMch = genInst.getSourceMachineName();
		if (basedMch == null)
			return "Undefined based machine";
		if (genInst.getTargetProject() == null) {
			return "Undefined target project";
		}
		return null;
	}
	
	
	private void initialise() {
		IRodinDB rodinDB = RodinCore.getRodinDB();
		RodinElementComboViewer<IRodinProject> projectChooser = srcGroup
				.getProjectChooser();
		projectChooser.setInput(rodinDB);
		seenContextsGroup.getProjectChooser().setInput(rodinDB);
		if (initSrcMachine != null) {
			projectChooser.setElement(initSrcMachine.getRodinProject());
			srcGroup.getMachineChooser().setElement(initSrcMachine);
		}
	}

	public void init(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			Object firstElement = ssel.getFirstElement();
			if (firstElement != null && firstElement instanceof IMachineRoot) {
				initSrcMachine = (IMachineRoot) firstElement;
				if (UIUtils.DEBUG) {
					UIUtils.debug("Initial source machine:"
							+ initSrcMachine.getElementName());
				}
			}
			else {
				initSrcMachine = null;
			}
		}
	}

	public IMachineRoot getBasedMachine() {
		return srcGroup.getMachineChooser().getElement();
	}

	public void pageChanged() {
		// Do nothing since there is no previous page;
	}

	public IGenInst getGenInst() {
		return genInst;
	}

}