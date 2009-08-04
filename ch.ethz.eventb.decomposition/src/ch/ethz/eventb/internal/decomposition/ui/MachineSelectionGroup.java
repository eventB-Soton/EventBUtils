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
package ch.ethz.eventb.internal.decomposition.ui;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.IRodinProject;

import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         This class provides a machine chooser containing an element
 *         chooser {@link RodinElementComboViewer} for a Rodin project and an
 *         element chooser for a machine root within the selected project.
 *         </p>
 *         <p>
 *         When a new project is selected, the machine element chooser is reset,
 *         <i>i.e.</i> there is no selected machine root.
 *         </p>
 *         <p>
 *         The selected project is always the input of the machine element chooser.
 *         </p>
 */
public class MachineSelectionGroup {

	// The project element chooser.
	RodinElementComboViewer<IRodinProject> projectChooser;
	
	// The machine element chooser.
	RodinElementComboViewer<IMachineRoot> machineChooser;
	
	// The main group.
	private Group group;
		
	/**
	 * The constructor. Creates the main group widget, and then creates the two 
	 * element choosers.
	 * 
	 * @param parent
	 *            the composite parent for the group widget.
	 * @param style
	 *            the style to create the group widget.
	 */
	public MachineSelectionGroup(Composite parent, int style) {
		group = new Group(parent, style);
		createContents();
	}

	/**
	 * Utility method to create the content of the group with two element choosers.
	 */
	private void createContents() {
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		group.setLayout(gl);

		// Project label
		Label label = new Label(group, SWT.NONE);
		label.setText(Messages.label_project); //$NON-NLS-1$
		
		// Project chooser
		createProjectChooser();
		
		// Machine label
		label = new Label(group, SWT.NONE);
		label.setText(Messages.label_machine); //$NON-NLS-1$

		// Machine chooser
		createMachineChooser();
	}

	private void createMachineChooser() {
		machineChooser = new RodinElementComboViewer<IMachineRoot>(
			group, IMachineRoot.ELEMENT_TYPE);
		machineChooser.getControl().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		machineChooser.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
					// Extra actions here
			}
		});
	}

	private void createProjectChooser() {
		projectChooser = new RodinElementComboViewer<IRodinProject>(
				group, IRodinProject.ELEMENT_TYPE);
		projectChooser.getControl().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		projectChooser
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						machineChooser.setInput(projectChooser
								.getElement());
						// Extra actions here
					}
				});
	}

	/**
	 * Returns the project chooser viewer.
	 * 
	 * @return the project chooser viewer.
	 */
	public RodinElementComboViewer<IRodinProject> getProjectChooser() {
		return projectChooser;
	}


	/**
	 * Returns the machine chooser viewer.
	 * 
	 * @return the machine chooser viewer.
	 */
	public RodinElementComboViewer<IMachineRoot> getMachineChooser() {
		return machineChooser;
	}

	/**
	 * Returns the main group.
	 * 
	 * @return the main group.
	 */
	public Group getGroup() {
		return group;
	}
	
}
