/*******************************************************************************
 * Copyright (c) 2009-2010 ETH Zurich
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.inst.wizard;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eventb.core.IContextRoot;
import org.rodinp.core.IRodinProject;

import ch.ethz.eventb.internal.inst.ui.utils.Messages;
import ch.ethz.eventb.internal.inst.wizard.utils.RodinElementComboViewer;
import ch.ethz.eventb.internal.inst.wizard.utils.RodinElementListSelectionViewer;


/**
 * @author htson
 *         <p>
 *         This class provides a machine chooser group containing an element
 *         chooser {@link ElementChooserViewer} for a Rodin project and an
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
public class ContextsChooserGroup {

	// The project element chooser.
	private RodinElementComboViewer<IRodinProject> projectChooser;
	
	// The machine element chooser.
	private  RodinElementListSelectionViewer<IContextRoot> contextsChooser;
	
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
	public ContextsChooserGroup(Composite parent, int style) {
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
		projectChooser = new RodinElementComboViewer<IRodinProject>(
				group, IRodinProject.ELEMENT_TYPE);
		projectChooser.getControl().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		projectChooser
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						contextsChooser.setInput(projectChooser
								.getElement());
					}

				});
		
		// Seen context chooser
		contextsChooser = new RodinElementListSelectionViewer<IContextRoot>(
			group,
				IContextRoot.ELEMENT_TYPE, Messages.title_contextschooser);

	}

	/**
	 * Return the project chooser viewer.
	 * 
	 * @return the project chooser viewer.
	 */
	public RodinElementComboViewer<IRodinProject> getProjectChooser() {
		return projectChooser;
	}


	/**
	 * Return the contexts chooser viewer.
	 * 
	 * @return the contexts chooser viewer.
	 */
	public RodinElementListSelectionViewer<IContextRoot> getContextsChooser() {
		return contextsChooser;
	}

	/**
	 * Return the main Group widget.
	 * 
	 * @return the main Group widget.
	 */
	public Group getGroup() {
		return group;
	}
	
	/**
	 * Add a new selection changed listener. This will be added as a (selection
	 * changed) listener to the contexts chooser viewer.
	 * 
	 * @param listener
	 *            a selection changed listener.
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		contextsChooser.addSelectionChangedListener(listener);
	}

	/**
	 * Remove a new selection changed listener.
	 * 
	 * @param listener
	 *            a selection changed listener.
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		contextsChooser.removeSelectionChangedListener(listener);
	}

}
