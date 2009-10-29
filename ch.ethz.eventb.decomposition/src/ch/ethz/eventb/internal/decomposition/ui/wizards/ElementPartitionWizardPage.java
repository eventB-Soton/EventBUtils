/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.decomposition.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eventb.core.IContextRoot;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.IElementType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;

import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.decomposition.ISubModel;
import ch.ethz.eventb.internal.decomposition.ui.DecompositionContentProvider;
import ch.ethz.eventb.internal.decomposition.ui.DecompositionLabelProvider;
import ch.ethz.eventb.internal.decomposition.ui.MachineSelectionGroup;
import ch.ethz.eventb.internal.decomposition.ui.RodinElementComboViewer;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         The wizard page used to partition the Rodin elements.
 *         </p>
 */
public class ElementPartitionWizardPage<T extends IRodinElement> extends
		WizardPage {

	/** The current selection. */
	private ISelection selection;

	/** The model decomposition. */
	IModelDecomposition modelDecomp;

	/** The type of the elements to be partitioned. */
	private IElementType<T> type;

	/** The tree viewer to display the sub-models. */
	TreeViewer viewer;

	/** A machine chooser group to identify the machine to be decomposed. */
	private MachineSelectionGroup machineGroup;

	/** The "Add" button. */
	private Button addButton;

	/** The "Edit" button. */
	private Button editButton;

	/** The "Remove" button. */
	private Button removeButton;

	/**
	 * The constructor. Stores the current selection, to later perform the
	 * initialization.
	 * 
	 * @param modelDecomp
	 *            the model decomposition.
	 * @param selection
	 *            the current selection.
	 * @param type
	 *            the type of the elements to be partitioned.
	 */
	public ElementPartitionWizardPage(final IModelDecomposition modelDecomp,
			final ISelection selection, final IElementType<T> type) {
		super(Messages.wizard_name);
		setTitle(Messages.wizard_title);
		setDescription(Messages.wizard_description);
		this.modelDecomp = modelDecomp;
		this.selection = selection;
		this.type = type;
	}

	public final void createControl(final Composite parent) {
		// Create the main composite.
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 9;
		layout.numColumns = 2;
		container.setLayout(layout);

		// Create the machine chooser group.
		createMachineGroup(container);

		// Create events' partition chooser group.
		createViewer(container);

		// Create buttons
		Composite composite = new Composite(container, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		composite.setLayout(new GridLayout());
		createAddButton(composite);
		createEditButton(composite);
		createRemoveButton(composite);

		final RodinElementComboViewer<IMachineRoot> machineChooser = machineGroup
				.getMachineChooser();
		machineChooser
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(
							final SelectionChangedEvent event) {
						IMachineRoot mch = machineChooser.getElement();
						modelDecomp.setMachineRoot(mch);
						viewer.setInput(modelDecomp);
						updateButtons();
					}

				});

		// Initialize the widgets.
		initialize();

		// Update the status.
		updateStatus(null);

		// Set the main control of the wizard.
		setControl(container);

	}

	private void createRemoveButton(final Composite composite) {
		removeButton = new Button(composite, SWT.PUSH);
		removeButton.setText(Messages.button_remove);
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(final SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(final SelectionEvent e) {
				ISubModel subModel = getSelectedSubModel();
				if (subModel != null) {
					modelDecomp.removeSubModel(subModel);
					viewer.refresh();
					updateButtons();
				}
				return;
			}
		});
	}

	private void createEditButton(final Composite composite) {
		editButton = new Button(composite, SWT.PUSH);
		editButton.setText(Messages.button_edit);
		editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(final SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(final SelectionEvent e) {
				ISubModel subModel = getSelectedSubModel();
				if (subModel != null) {
					ElementPartitionDialog<T> dialog = new ElementPartitionDialog<T>(
							viewer.getControl().getShell(), subModel, type);
					dialog.open();
					if (dialog.getReturnCode() == Dialog.OK) {
						subModel.setProjectName(dialog.getProjectName());
						subModel.setElements(dialog.getElements());
						viewer.refresh();
						updateButtons();
					}
				}
			}
		});
	}

	private void createAddButton(final Composite composite) {
		addButton = new Button(composite, SWT.PUSH);
		addButton.setText(Messages.button_add);
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(final SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(final SelectionEvent e) {
				modelDecomp.addSubModel();
				viewer.refresh();
				updateButtons();
			}
		});
	}

	private void createMachineGroup(final Composite container) {
		machineGroup = new MachineSelectionGroup(container, SWT.DEFAULT);
		machineGroup.getGroup().setText(Messages.wizard_machine);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		machineGroup.getGroup().setLayoutData(gridData);
	}

	private void createViewer(final Composite container) {
		viewer = new TreeViewer(container, SWT.SINGLE | SWT.BORDER);
		viewer.setContentProvider(new DecompositionContentProvider());
		viewer.setLabelProvider(new DecompositionLabelProvider());
		viewer.setInput(modelDecomp);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(final SelectionChangedEvent event) {
				updateButtons();
			}

		});
	}

	/**
	 * Returns the current selected element distribution.
	 * 
	 * @return the current selected element distribution.
	 */
	protected final ISubModel getSelectedSubModel() {
		IStructuredSelection ssel = (IStructuredSelection) viewer
				.getSelection();

		Object element = ssel.getFirstElement();
		if (element instanceof ISubModel) {
			return (ISubModel) element;
		}

		return null;
	}

	/**
	 * Updates the status of the buttons.
	 */
	protected final void updateButtons() {
		if (modelDecomp.getMachineRoot() == null) {
			addButton.setEnabled(false);
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
			return;
		} else {
			addButton.setEnabled(true);
			if (viewer.getSelection().isEmpty()) {
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
			} else {
				editButton.setEnabled(true);
				removeButton.setEnabled(true);
			}
		}
	}

	/**
	 * Utility method to initialize the widgets according to the current
	 * selection. <br>
	 * The input to the machine chooser group is the Rodin database.
	 */
	private void initialize() {
		IRodinDB rodinDB = RodinCore.getRodinDB();
		updateButtons();
		machineGroup.getProjectChooser().setInput(rodinDB);
		updateButtons();
		if (selection != null && !selection.isEmpty()
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1) {
				return;
			}
			Object element = ssel.getFirstElement();
			if (element instanceof IMachineRoot) {
				IRodinProject project = ((IMachineRoot) element)
						.getRodinProject();
				machineGroup.getProjectChooser().setSelection(
						new StructuredSelection(project), true);
				machineGroup.getMachineChooser().setSelection(
						new StructuredSelection(element), true);
				return;
			}
			if (element instanceof IContextRoot) {
				IRodinProject project = ((IContextRoot) element)
						.getRodinProject();
				machineGroup.getProjectChooser().setSelection(
						new StructuredSelection(project), true);
				return;
			}
			if (element instanceof IProject) {
				IRodinProject project = rodinDB
						.getRodinProject(((IProject) element).getName());
				machineGroup.getProjectChooser().setSelection(
						new StructuredSelection(project), true);
				return;
			}
			if (element instanceof IFile) {
				IRodinProject project = rodinDB
						.getRodinProject(((IFile) element).getProject()
								.getName());
				machineGroup.getProjectChooser().setSelection(
						new StructuredSelection(project), true);
				if (project != null) {
					IRodinFile file = project.getRodinFile(((IFile) element)
							.getName());
					if (file != null) {
						IInternalElement root = file.getRoot();
						if (root instanceof IMachineRoot) {
							machineGroup.getMachineChooser().setSelection(
									new StructuredSelection(root), true);
						}
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
	private void updateStatus(final String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/**
	 * Returns the model distribution.
	 * 
	 * @return the model distribution.
	 */
	public final IModelDecomposition getModelDecomposition() {
		return modelDecomp;
	}

}