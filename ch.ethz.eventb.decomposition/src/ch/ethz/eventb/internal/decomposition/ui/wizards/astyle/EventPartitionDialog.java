/*****************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     ETH Zurich - initial API and implementation
 ****************************************************************************/

package ch.ethz.eventb.internal.decomposition.ui.wizards.astyle;

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
import org.eventb.core.IEvent;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.ISubModel;
import ch.ethz.eventb.internal.decomposition.ui.RodinElementSelectionViewer;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         The dialog used to partition the events.
 *         </p>
 */
public class EventPartitionDialog extends Dialog {

	// The input sub-model.
	private ISubModel subModel;
	
	// The text widget for the project name.
	private Text prjText;
	
	// The name of the project.
	private String prjName;
	
	// A list of event's labels. 
	private String[] events;
	
	// A viewer to choose the list of events.
	private RodinElementSelectionViewer<IEvent> viewer;

	/**
	 * Constructor.
	 * 
	 * @param parentShell
	 *            the parent shell.
	 * @param subModel
	 *            the input sub-model.
	 */
	protected EventPartitionDialog(Shell parentShell,
			ISubModel subModel) {
		super(parentShell);
		this.subModel = subModel;
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
		viewer = new RodinElementSelectionViewer<IEvent>(prjNameComp,
				IEvent.ELEMENT_TYPE, Messages.wizard_events);
		viewer.setInput(subModel.getMachineRoot());
		
		return control;
	}

	@Override
	protected void okPressed() {
		// Set the project name to be returned.
		prjName = prjText.getText();
		
		// Set the list of event to be returned.
		Collection<IEvent> selected = viewer.getSelected();
		int size = selected.size();
		Collection<String> result = new ArrayList<String>(size);
		for (IEvent evt : selected) {
			try {
				result.add(evt.getLabel());
			} catch (RodinDBException e) {
				e.printStackTrace();
				super.cancelPressed();
			}
		}
		events = result.toArray(new String[size]);
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
	 * Return the list of events chosen through the dialog.
	 * 
	 * @return the event labels.
	 */
	public String[] getEvents() {
		return events;
	}

}
