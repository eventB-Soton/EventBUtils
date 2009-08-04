/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/

package ch.ethz.eventb.internal.decomposition.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.rodinp.core.IElementType;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinElement;

import ch.ethz.eventb.internal.decomposition.utils.Messages;

/**
 * @author htson
 *         <p>
 *         A viewer to choose a list of elements which are children of an input
 *         element. The input element need to be set by
 *         {@link #setInput(IParent)}.
 *         </p>
 * @param <T>
 *            a type which extends {@link IRodinElement}.
 */
public class RodinElementSelectionViewer<T extends IRodinElement> {

	// A list viewer for available elements.
	private ListViewer availableViewer;
	
	// A list viewer for selected elements.
	private ListViewer selectedViewer;
	
	// The list of selected elements.
	List<T> selectedElement;
	
	// The element type of the chosen elements.
	private IElementType<T> type;
	
	// The title.
	private String title;

	// The "Add" button.
	private Button add;
	
	// The "Remove" button.
	private Button remove;
	
	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the parent composite.
	 * @param type
	 *            the type of the chosen elements.
	 * @param title
	 *            the title of the chooser.
	 */
	public RodinElementSelectionViewer(Composite parent, IElementType<T> type,
			String title) {
		this.type = type;
		selectedElement = new ArrayList<T>();
		this.title = title;
		
		// Create the main control of the dialog.
		createViewer(parent);
	}

	/**
	 * Creates the viewer.
	 * 
	 * @param comp
	 *            the parent composite.
	 */
	private void createViewer(Composite comp) {
		
		// Create the parent.
		Group parent = new Group(comp, SWT.DEFAULT);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalSpan = 3;
		parent.setLayoutData(layoutData);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		parent.setLayout(layout);
		parent.setText(title);
		
		// Create the labels.
		createLabels(parent);
		
		// Create the available elements list.
        availableViewer = createListViewer(parent);
		availableViewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				return !selectedElement.contains(element);
			}
			
		});
        
        // Create the buttons.
		createButtons(parent);
		
		// Create the selected elements list.
		selectedViewer = createListViewer(parent);
		selectedViewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				return selectedElement.contains(element);
			}
			
		});		
	}

	/**
	 * Utility method to create the labels on top of the two lists.
	 * 
	 * @param parent
	 *            the parent composite.
	 */
	private void createLabels(Composite parent) {
		Label availableLabel = new Label(parent, SWT.LEFT);
	    availableLabel
				.setText(Messages.label_available);
	    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	    availableLabel.setLayoutData(gd);
	
	    Label tmpLabel = new Label(parent, SWT.CENTER);
	    gd = new GridData();
	    gd.verticalAlignment = GridData.CENTER;
	    tmpLabel.setLayoutData(gd);
	    
	    Label selectedLabel = new Label(parent, SWT.LEFT);
	    selectedLabel
				.setText(Messages.label_selected);
	    gd = new GridData(GridData.FILL_HORIZONTAL);
	    selectedLabel.setLayoutData(gd);	
	}

	/**
	 * Creates the list viewer.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the list viewer
	 */
    private ListViewer createListViewer(Composite parent) {
		ListViewer viewer = new ListViewer(parent, SWT.BORDER | SWT.MULTI
				| SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setLabelProvider(new RodinElementLabelProvider());
		viewer
				.setContentProvider(new RodinElementContentProvider<T>(
						type));
		return viewer;
	}
    
	/**
	 * Creates the set of buttons.
	 * 
	 * @param parent
	 *            the parent composite used to create the buttons.
	 */
	private void createButtons(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		container.setLayout(layout);
		
		// Add button.
		add = new Button(container, SWT.PUSH);
		add.setText(">>");
		add.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				addElement();
			}
			
		});
		add.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Remove button.
		remove = new Button(container, SWT.PUSH);
		remove.setText("<<");
		remove.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				removeElements();
			}
			
		});
		remove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/**
	 * Utility method to add selected elements.
	 */
	protected void addElement() {
		selectedElement.addAll(selectionToList(availableViewer.getSelection()));
		refresh();
	}

	/**
	 * Utility method to remove selected elements.
	 */
	protected void removeElements() {
		selectedElement
				.removeAll(selectionToList(selectedViewer.getSelection()));
		refresh();
	}

	/**
	 * Utility method to refresh the two viewers.
	 */
	private void refresh() {
		availableViewer.refresh();
		selectedViewer.refresh();
	}

	/**
	 * Utility method to return the current selection as a list.
	 * 
	 * @param selection
	 *            the current selection.
	 * @return the elements in this selection as a list.
	 */
	@SuppressWarnings("unchecked")
	private Collection<T> selectionToList(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			return new ArrayList<T>(ssel.toList());
		}
		return Collections.emptyList();
	}

	/**
	 * Sets the input element of the viewer.
	 * 
	 * @param input
	 *            a parent input element.
	 */
	public void setInput(IParent input) {
		availableViewer.setInput(input);
		selectedViewer.setInput(input);
		selectedElement = new ArrayList<T>();
	}

	/**
	 * Returns the collection of selected elements.
	 * 
	 * @return the selected elements.
	 */
	public Collection<T> getSelected() {
		return selectedElement;
	}
	
}
