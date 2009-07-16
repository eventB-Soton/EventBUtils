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

/**
 * @author htson
 *         <p>
 *         A dialog for editing an element distribution.
 *         </p>
 *         TODO To be implemented {@link #cancelPressed()}.
 */
public class EditElementDistributionDialog extends Dialog {

	// The input element distribution.
	private IElementDistribution elemDist;
	
	// The text widget for the project name.
	private Text prjText;
	
	// The name of the project.
	private String prjName;
	
	// A list of event's labels. 
	private String [] evts;
	
	// A viewer for choosing list of events.
	private ElementListChooserViewer<IEvent> viewer;

	/**
	 * Constructor.
	 * 
	 * @param parentShell
	 *            the parent shell for creating the dialog.
	 * @param elemDist
	 *            the input element distribution to be edited.
	 */
	protected EditElementDistributionDialog(Shell parentShell,
			IElementDistribution elemDist) {
		super(parentShell);
		this.elemDist = elemDist;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		
		// Area for choosing project name.
		Composite prjNameComp = new Composite((Composite) control, SWT.NONE);
		prjNameComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		prjNameComp.setLayout(layout);
		
		Label prjLabel = new Label(prjNameComp, SWT.CENTER);
		prjLabel.setText("Project name");
		prjLabel.setLayoutData(new GridData());
		
		prjText = new Text(prjNameComp, SWT.BORDER | SWT.SINGLE);
		prjText.setText(elemDist.getProjectName());
		prjText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Create the viewer for choosing a list of events.
		viewer = new ElementListChooserViewer<IEvent>(prjNameComp,
				IEvent.ELEMENT_TYPE, "Choosing events' distribution");
		viewer.setInput(elemDist.getMachineRoot());
		return control;
	}
	
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
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
			}
		}
		evts = result.toArray(new String[size]);
		super.okPressed();
	}

	/**
	 * Return the project name as chosen by the dialog.
	 * 
	 * @return the chosen project name.
	 */
	public String getProjectName() {
		return prjName;
	}

	/**
	 * Return the list of event labels as chosen by the dialog.
	 * 
	 * @return the chosen list of event labels.
	 */
	public String [] getEventLabels() {
		return evts;
	}

}
