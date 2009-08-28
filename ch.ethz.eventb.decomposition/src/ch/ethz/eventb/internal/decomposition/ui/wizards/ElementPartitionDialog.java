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

import java.util.ArrayList;
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
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;

import ch.ethz.eventb.decomposition.utils.Messages;
import ch.ethz.eventb.internal.decomposition.ISubModel;
import ch.ethz.eventb.internal.decomposition.ui.RodinElementSelectionViewer;

/**
 * @author htson
 *         <p>
 *         The dialog used to partition the events.
 *         </p>
 */
public class ElementPartitionDialog<T extends IRodinElement> extends Dialog {

	// The input sub-model.
	private ISubModel subModel;

	// The text widget for the project name.
	private Text prjText;

	// The name of the project.
	private String prjName;

	// A list of Rodin elements.
	private IRodinElement[] elements;

	// The type of the Rodin elements.
	private IElementType<T> type;

	// A viewer to choose the list of events.
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
	protected ElementPartitionDialog(Shell parentShell, ISubModel subModel,
			IElementType<T> type) {
		super(parentShell);
		this.subModel = subModel;
		this.type = type;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);

		// Area to choose the project name.
		Composite prjNameComp = new Composite((Composite) control, SWT.NONE);
		prjNameComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		prjNameComp.setLayout(layout);

		Label prjLabel = new Label(prjNameComp, SWT.CENTER);
		prjLabel.setText(Messages.wizard_project);
		prjLabel.setLayoutData(new GridData());

		prjText = new Text(prjNameComp, SWT.BORDER | SWT.SINGLE);
		prjText.setText(subModel.getProjectName());
		prjText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the viewer to choose the list of events.
		viewer = new RodinElementSelectionViewer<T>(prjNameComp,
				type, Messages.wizard_elements);
		viewer.setInput(subModel.getMachineRoot());

		return control;
	}

	@Override
	protected void okPressed() {
		// Set the project name to be returned.
		prjName = prjText.getText();

		// Set the list of event to be returned.
		Collection<T> selected = viewer.getSelected();
		int size = selected.size();
		Collection<IInternalElement> result = new ArrayList<IInternalElement>(
				size);
		for (IRodinElement element : selected) {
			result.add((IInternalElement) element);
		}
		elements = result.toArray(new IInternalElement[size]);
		super.okPressed();
	}

	/**
	 * Returns the project name chosen through the dialog.
	 * 
	 * @return the chosen project name.
	 */
	public String getProjectName() {
		return prjName;
	}

	/**
	 * Return the list of Rodin elements chosen through the dialog.
	 * 
	 * @return the elements.
	 */
	public IRodinElement[] getElements() {
		return elements;
	}

}
