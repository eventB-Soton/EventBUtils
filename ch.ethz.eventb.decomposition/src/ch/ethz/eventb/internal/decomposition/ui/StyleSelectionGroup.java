/*******************************************************************************
 * Copyright (c) 2009 Systerel
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systsrel - initial API and implementation
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

import ch.ethz.eventb.internal.decomposition.IModelDecomposition;
import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 *  This class provides a chooser for the decomposition style.
 */
public class StyleSelectionGroup {

	// The decomposition style chooser.
	DecompositionComboViewer<IModelDecomposition> styleChooser;
	
	// The main group.
	private Group group;
		
	/**
	 * The constructor. Creates the main group widget, and then creates the
	 * element chooser.
	 * 
	 * @param parent
	 *            the composite parent for the group widget.
	 */
	public StyleSelectionGroup(Composite parent) {
		group = new Group(parent, SWT.DEFAULT);
		createContents();
	}

	/**
	 * Utility method to create the content of the group with an element chooser.
	 */
	private void createContents() {
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		group.setLayout(gl);

		// Project label
		Label label = new Label(group, SWT.NONE);
		label.setText(Messages.label_style); //$NON-NLS-1$
		
		// Project chooser
		createStyleChooser();
	}

	private void createStyleChooser() {
		styleChooser = new DecompositionComboViewer<IModelDecomposition>(
				group);
		styleChooser.getControl().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		styleChooser
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						// Extra actions here
					}
				});
	}

	/**
	 * Returns the style chooser viewer.
	 * 
	 * @return the style chooser viewer.
	 */
	public DecompositionComboViewer<IModelDecomposition> getStyleChooser() {
		return styleChooser;
	}

	/**
	 * Returns the style chooser group.
	 * 
	 * @return the style chooser group.
	 */
	public Group getGroup() {
		return group;
	}
	
}
