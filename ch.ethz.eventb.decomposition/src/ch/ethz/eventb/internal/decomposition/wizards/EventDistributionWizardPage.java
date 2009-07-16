/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.decomposition.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         The wizard page for choosing events' distribution.
 *         </p>
 */
public class EventDistributionWizardPage extends WizardPage {

	// The current selection. 
	private ISelection selection;
	
	// The model distribution.
	private IModelDistribution modelDist;
	
	// The tree viewer for displaying an choosing element distributions.
	private TreeViewer viewer;
	
	// A machine chooser group for choosing the machine to be decomposed.
	private MachineChooserGroup machineGroup;
	
	// "Add" element distribution.
	private Button addButton;
	
	// "Edit" element distribution.
	private Button editButton;
	
	// "Remove" element distribution.
	private Button removeButton;
	
	/**
	 * The constructor. Stored the current selection to set the initial value
	 * later.
	 * 
	 * @param selection
	 *            the current selection.
	 */
	public EventDistributionWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("Event-B Decomposition (A-Style). Step 1");
		setDescription("Choosing the events' distribution");
		this.selection = selection;
		modelDist = new ModelDistribution(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent) {
		// Create the main composite
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 9;
		layout.numColumns = 2;
		container.setLayout(layout);
		
		// Create the pattern machine chooser group.
		machineGroup = new MachineChooserGroup(container, SWT.DEFAULT);
		machineGroup.getGroup().setText("Pattern machine");
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		machineGroup.getGroup().setLayoutData(gridData);
		
		// Create events' distribution chooser group
		viewer = new TreeViewer(container, SWT.SINGLE | SWT.BORDER);
		viewer.setContentProvider(new ITreeContentProvider(){

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof IModelDistribution) {
					return ((IModelDistribution) parentElement)
							.getElementDistributions();
				}
				
				if (parentElement instanceof IElementDistribution) {
					return ((IElementDistribution) parentElement).getEventLabels();
				}
				
				return new Object[0];
			}

			public Object getParent(Object element) {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean hasChildren(Object element) {
				return this.getChildren(element).length != 0;
			}

			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			public void dispose() {
				// TODO Auto-generated method stub
				
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// TODO Auto-generated method stub
				
			}
			
		});

		viewer.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof IElementDistribution) {
					return ((IElementDistribution) element).getProjectName();
				}
				
				if (element instanceof IEvent) {
					try {
						return ((IEvent) element).getLabel();
					} catch (RodinDBException e) {
						e.printStackTrace();
						return super.getText(element);
					}
				}
				
				return super.getText(element);
			}
			
		});
		viewer.setInput(modelDist);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
			
		});
		
		// Create buttons
		Composite composite = new Composite(container, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		composite.setLayout(new GridLayout());
		addButton = new Button(composite, SWT.PUSH);
		addButton.setText("Add");
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				modelDist.createElementDistribution();
				viewer.refresh();
				updateButtons();
			}
			
		});
		
		editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
		editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				IElementDistribution elemDist = getSelectedElementDistribution();
				if (elemDist != null) {
					EditElementDistributionDialog dialog = new EditElementDistributionDialog(
							viewer.getControl().getShell(), elemDist);
					
					dialog.open();
					if (dialog.getReturnCode() == Dialog.OK) {
						elemDist.setProjectName(dialog.getProjectName());
						elemDist.setEventLabels(dialog.getEventLabels());
						viewer.refresh();
						updateButtons();
					}
				}
			}
			
		});
		
		
		removeButton = new Button(composite, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				IElementDistribution elemDist = getSelectedElementDistribution();
				if (elemDist != null) {
					modelDist.removeElementDistribution(elemDist);
					viewer.refresh();
					updateButtons();
				}
				return;
			}
			
		});
		
		final ElementChooserComboViewer<IMachineRoot> machineChooser = machineGroup
				.getMachineChooser();
		machineChooser
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						IMachineRoot mch = machineChooser.getElement();
						modelDist = new ModelDistribution(mch);
						viewer.setInput(modelDist);
						updateButtons();
					}

				});
		
		// Initialise the widgets
		initialize();

		updateStatus(null);
			
		// Set the main control of the wizard.
		setControl(container);
				
	}


	/**
	 * Return the current selected element distribution.
	 * 
	 * @return the current selected element distribution.
	 */
	protected IElementDistribution getSelectedElementDistribution() {
		IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();

		Object element = ssel.getFirstElement();
		if (element instanceof IElementDistribution) {
			return (IElementDistribution) element;
		}

		return null;
	}

	/**
	 * Update the status of the buttons.
	 */
	protected void updateButtons() {
		if (modelDist.getMachineRoot() == null) {
			addButton.setEnabled(false);
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
			return;
		}
		else {
			addButton.setEnabled(true);
			if (viewer.getSelection().isEmpty()) {
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
			}
			else {
				editButton.setEnabled(true);
				removeButton.setEnabled(true);
			}
		}
	}

	/**
	 * Utility method to initialise the widgets according to the current
	 * selection.
	 * <ul>
	 * <li>The input to the machine chooser group is the RodinDB.</li>
	 * </ul>
	 */
	private void initialize() {
		IRodinDB rodinDB = RodinCore.getRodinDB();
		updateButtons();
		machineGroup.getProjectChooser().setInput(rodinDB);
		updateButtons();
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1) {
				return;
			}
			Object element = ssel.getFirstElement();
			if (element instanceof IMachineRoot) {
				IRodinProject project = ((IMachineRoot) element).getRodinProject();
				machineGroup.getProjectChooser().setSelection(new StructuredSelection(project), true);
				machineGroup.getMachineChooser().setSelection(new StructuredSelection(element), true);
				return;
			}
			if (element instanceof IContextRoot) {
				IRodinProject project = ((IContextRoot) element).getRodinProject();
				machineGroup.getProjectChooser().setSelection(new StructuredSelection(project), true);
				return;
			}
			if (element instanceof IProject) {
				IRodinProject project = rodinDB.getRodinProject(((IProject) element).getName());
				machineGroup.getProjectChooser().setSelection(new StructuredSelection(project), true);
				return;
			}
			if (element instanceof IFile) {
				IRodinProject project = rodinDB.getRodinProject(((IFile) element).getProject().getName());
				machineGroup.getProjectChooser().setSelection(new StructuredSelection(project), true);
				if (project != null) {
					IRodinFile file = project.getRodinFile(((IFile)element).getName());
					if (file != null) {
						IInternalElement root = file.getRoot();
						if (root instanceof IMachineRoot)
							machineGroup.getMachineChooser().setSelection(new StructuredSelection(root), true);
					}
				}
				return;
			}
		}

	}

	/**
	 * Utility method to update the status message and also set the completeness
	 * of the page.
	 * 
	 * @param message
	 *            the error message or <code>null</code>.
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/**
	 * Return the model distribution.
	 * 
	 * @return the model distribution.
	 */
	public IModelDistribution getModelDistribution() {
		return modelDist;
	}

}