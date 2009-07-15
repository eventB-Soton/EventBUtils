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


/**
 * @author htson
 *         <p>
 *         This class provides a machine chooser group containing an element
 *         chooser {@link ElementChooserComboViewer} for a Rodin project and an
 *         element chooser for a machine root within the selected project.
 *         </p>
 *         <p>
 *         When a new project is selected, the machine element chooser is reset,
 *         i.e. there is no selected machine root.
 *         </p>
 *         <p>
 *         The selected project is always the input of the machine element chooser.
 *         </p>
 */
public class MachineChooserGroup {

	// The project element chooser.
	private ElementChooserComboViewer<IRodinProject> projectChooser;
	
	// The machine element chooser.
	private  ElementChooserComboViewer<IMachineRoot> machineChooser;
	
	// The main Group widget.
	private Group group;
		
	/**
	 * The constructor. Create the main Group widget then create the two element
	 * choosers.
	 * 
	 * @param parent
	 *            the composite parent for the Group widget.
	 * @param style
	 *            the style to create the Group widget.
	 */
	public MachineChooserGroup(Composite parent, int style) {
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

		// Label
		Label label = new Label(group, SWT.NONE);
		label.setText("Project");
		
		// Problem project chooser
		projectChooser = new ElementChooserComboViewer<IRodinProject>(
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
		
		// Machine label
		label = new Label(group, SWT.NONE);
		label.setText("Machine");

		// Problem machine chooser
		machineChooser = new ElementChooserComboViewer<IMachineRoot>(
			group,
				IMachineRoot.ELEMENT_TYPE);
		machineChooser.getControl().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		machineChooser.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
					// Extra actions here
			}
		});

	}

	/**
	 * Return the project chooser viewer.
	 * 
	 * @return the project chooser viewer.
	 */
	public ElementChooserComboViewer<IRodinProject> getProjectChooser() {
		return projectChooser;
	}


	/**
	 * Return the machine chooser viewer.
	 * 
	 * @return the machine chooser viewer.
	 */
	public ElementChooserComboViewer<IMachineRoot> getMachineChooser() {
		return machineChooser;
	}

	/**
	 * Return the main Group widget.
	 * 
	 * @return the main Group widget.
	 */
	public Group getGroup() {
		return group;
	}
	
}
