/*******************************************************************************
 * Copyright (c) 2009 Systerel
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.ethz.eventb.decomposition.IModelDecomposition;
import ch.ethz.eventb.internal.decomposition.ui.DecompositionComboViewer;
import ch.ethz.eventb.internal.decomposition.ui.StyleSelectionGroup;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * The initial wizard page. It allows in particular to specify the decomposition
 * style.
 */
public class InitialWizardPage extends WizardPage {

	/** The model decomposition. */
	private IModelDecomposition modelDecomp;

	/** A style chooser group to specify the decomposition style. */
	private StyleSelectionGroup styleGroup;

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
					}

				});

		// Set the selection.
		IModelDecomposition aDecomp = new ch.ethz.eventb.internal.decomposition.astyle.ModelDecomposition();
		styleChooser.setInput(aDecomp);
		styleChooser.setSelection(new StructuredSelection(aDecomp), true);
		IModelDecomposition bDecomp = new ch.ethz.eventb.internal.decomposition.bstyle.ModelDecomposition();
		styleChooser.add(bDecomp);

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