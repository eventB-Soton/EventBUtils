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
package ch.ethz.eventb.internal.decomposition.ui.wizards;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.decomposition.IModelDecomposition.ContextDecomposition;
import ch.ethz.eventb.internal.decomposition.ui.DecompositionComboViewer;
import ch.ethz.eventb.internal.decomposition.ui.StyleSelectionGroup;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * The initial wizard page. It allows in particular to specify the decomposition
 * style.
 */
public class InitialWizardPage extends WizardPage {

	private static final IModelDecomposition A_DECOMP = new ch.ethz.eventb.internal.decomposition.astyle.ModelDecomposition();
	private static final IModelDecomposition B_DECOMP = new ch.ethz.eventb.internal.decomposition.bstyle.ModelDecomposition();

	private static enum Option {

		A(true, true), B(false, false), ;

		public final boolean decomposeContexts;
		public final boolean createNewProjects;

		private Option(boolean decomposeContexts, boolean createNewProjects) {
			this.decomposeContexts = decomposeContexts;
			this.createNewProjects = createNewProjects;
		}

	}
	
	/** The model decomposition. */
	private IModelDecomposition modelDecomp;

	/** A style chooser group to specify the decomposition style. */
	private StyleSelectionGroup styleGroup;
	
	
	private Button decompContextsCheckBox;
	private Button newProjectCheckBox;

	/**
	 * The constructor.
	 */
	public InitialWizardPage() {
		super(Messages.wizard_name);
		setTitle(Messages.wizard_title);
		setDescription(Messages.wizard_description);
	}

	public final void createControl(final Composite parent) {
		// Create the main composite.
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 9;
		layout.numColumns = 2;
		container.setLayout(layout);

		// Create the machine chooser group.
		createSelectionGroup(container);

		final DecompositionComboViewer<IModelDecomposition> styleChooser = styleGroup
				.getStyleChooser();
		styleChooser
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(
							final SelectionChangedEvent event) {
						modelDecomp = styleChooser.getElement();
						setDefaultOptions();
					}

				});

		styleChooser.setInput(A_DECOMP);
		styleChooser.setSelection(new StructuredSelection(A_DECOMP), true);
		styleChooser.add(B_DECOMP);
		
		createNewProjectCheckBox(container);

		createDecompContextsCheckbox(container);
		
		// Set default options
		setDefaultOptions();
		
		// Update the status.
		updateStatus(null);

		// Set the main control of the wizard.
		setControl(container);

	}

	private void createSelectionGroup(final Composite container) {
		styleGroup = new StyleSelectionGroup(container);
		styleGroup.getGroup().setText(Messages.wizard_style);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		styleGroup.getGroup().setLayoutData(gridData);
	}

	private void createDecompContextsCheckbox(Composite container) {
		decompContextsCheckBox = new Button(container, SWT.CHECK);
		GridData gd_addToWorkingSetButton = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 3, 1);
		decompContextsCheckBox.setLayoutData(gd_addToWorkingSetButton);
		decompContextsCheckBox.setData("name", "decompContextsButton"); //$NON-NLS-1$ //$NON-NLS-2$
		decompContextsCheckBox.setText(Messages.wizard_decomposeContextsLabel);
		decompContextsCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selected = decompContextsCheckBox.getSelection();
				setContextDecomposition(selected);
			}

		});
	}

	private void setContextDecomposition(boolean selected) {
		if (!selected) {
			modelDecomp.setContextDecomposition(ContextDecomposition.NO_DECOMPOSITION);
		} else {
			modelDecomp.setContextDecomposition(ContextDecomposition.MINIMAL_FLATTENED_CONTEXT);
		}
	}
	
	private void createNewProjectCheckBox(Composite container) {
		newProjectCheckBox = new Button(container, SWT.CHECK);
		GridData gd_addToWorkingSetButton = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 3, 1);
		newProjectCheckBox.setLayoutData(gd_addToWorkingSetButton);
		newProjectCheckBox.setData("name", "createNewProjectsSubComponents"); //$NON-NLS-1$ //$NON-NLS-2$
		newProjectCheckBox.setText(Messages.wizard_createNewProjectsSubComponentsLabel);
		newProjectCheckBox.setSelection(false);
		newProjectCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selected = newProjectCheckBox.getSelection();
				modelDecomp.setCreateNewProjectDecomposition(selected);
			}
		});
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

	private void setDefaultOptions() {
		if (modelDecomp == null) {
			return;
		}
		final Option currentStyle;
		if (modelDecomp == A_DECOMP) {
			currentStyle = Option.A;
		} else {
			currentStyle = Option.B;
		}
		setSelection(decompContextsCheckBox, currentStyle.decomposeContexts);
		setContextDecomposition(currentStyle.decomposeContexts);
		setSelection(newProjectCheckBox, currentStyle.createNewProjects);
		modelDecomp
				.setCreateNewProjectDecomposition(currentStyle.createNewProjects);
	}

	private static void setSelection(Button button, boolean selected) {
		if (button != null && !button.isDisposed()) {
			button.setSelection(selected);
		}
	}
}