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

package ch.ethz.eventb.internal.decomposition.ui.wizards;

import java.util.Collection;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rodinp.core.IElementType;
import org.rodinp.core.IRodinElement;

import ch.ethz.eventb.decomposition.ISubModel;
import ch.ethz.eventb.internal.decomposition.ui.RodinElementSelectionViewer;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         The dialog used to partition the events.
 *         </p>
 */
public class ElementPartitionDialog<T extends IRodinElement> extends Dialog {

	/** The input sub-model. */
	private final ISubModel subModel;

	/** The text widget for the project name. */
	private Text prjText;

	/** The name of the project. */
	private String prjName;

	/** A list of Rodin elements. */
	private IRodinElement[] elements;

	/** The type of the Rodin elements. */
	private final IElementType<T> type;

	/** A viewer to choose the list of events. */
	private RodinElementSelectionViewer<T> viewer;

	/**
	 * Constructor.
	 * 
	 * @param parentShell
	 *            the parent shell.
	 * @param subModel
	 *            the input sub-model.
	 * @param type
	 *            the type of the input elements.
	 * 
	 */
	protected ElementPartitionDialog(final Shell parentShell,
			final ISubModel subModel, final IElementType<T> type) {
		super(parentShell);
		this.subModel = subModel;
		this.type = type;
	}

	@Override
	protected final Control createDialogArea(final Composite parent) {
		Control control = super.createDialogArea(parent);
		control.setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		// Area to choose the project name.
		Composite prjNameComp = new Composite((Composite) control, SWT.BORDER | SWT.MULTI);
		prjNameComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		prjNameComp.setLayout(layout);

		Label prjLabel = new Label(prjNameComp, SWT.CENTER);
		prjLabel.setText(Messages.wizard_component);
		prjLabel.setLayoutData(new GridData());

		prjText = new Text(prjNameComp, SWT.BORDER | SWT.SINGLE);
		prjText.setText(subModel.getComponentName());
		prjText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the viewer to choose the list of events/variables
		viewer = new RodinElementSelectionViewer<T>(prjNameComp, type,Messages.wizard_elements);
		viewer.setInput(subModel);

		return control;
	}

	@Override
	protected final void okPressed() {
		// Set the project name to be returned.
		prjName = prjText.getText();

		// Set the list of event to be returned.
		final Collection<T> selected = viewer.getSelected();
		elements = selected.toArray(new IRodinElement[selected.size()]);
		super.okPressed();
	}

	/**
	 * Returns the project name chosen through the dialog.
	 * 
	 * @return the chosen project name.
	 */
	public final String getProjectName() {
		return prjName;
	}

	/**
	 * Return the list of Rodin elements chosen through the dialog.
	 * 
	 * @return the elements.
	 */
	public final IRodinElement[] getElements() {
		return elements;
	}

}
