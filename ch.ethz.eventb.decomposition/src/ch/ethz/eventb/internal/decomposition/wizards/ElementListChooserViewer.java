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

package ch.ethz.eventb.internal.decomposition.wizards;

import java.util.ArrayList;
import java.util.Collection;
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

/**
 * @author htson
 *         <p>
 *         A viewer to choose a list of elements which are children of an input
 *         element. The input element need to be set by
 *         {@link #setInput(IParent)}.
 *         </p>
 * @param <T>
 *            a type which extends {@link IRodinElement}. TODO Implement update
 *            buttons status.
 */
public class ElementListChooserViewer<T extends IRodinElement> {

	// A list viewer for available elements.
	private ListViewer availableViewer;
	
	// A list viewer for selected elements.
	private ListViewer selectedViewer;
	
	// The list of selected elements.
	private List<T> selectedElement;
	
	// The element type of the chosen elements.
	private IElementType<T> type;
	
	// The title of the dialog.
	private String title;

	// "Add" button.
	private Button add;
	
	// "Remove" button.
	private Button remove;
	
	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the composite parent to create the viewer.
	 * @param type
	 *            the element type of the chosen elements.
	 * @param title
	 *            the title of the dialog.
	 */
	public ElementListChooserViewer(Composite parent, IElementType<T> type,
			String title) {
		this.type = type;
		selectedElement = new ArrayList<T>();
		this.title = title;
		
		// Create the main control of the dialog.
		createControl(parent);
	}

	/**
	 * Create the control of the dialog.
	 * 
	 * @param comp
	 *            the composite to create the control.
	 */
	private void createControl(Composite comp) {
		
		// Create the Group parent.
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
	 * Utility method for creating the labels on top of the two lists.
	 * 
	 * @param parent
	 *            the composite parent for creating the labels.
	 */
	private void createLabels(Composite parent) {
		Label availableLabel = new Label(parent, SWT.LEFT);
	    availableLabel
				.setText("Available");
	    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	    availableLabel.setLayoutData(gd);
	
	    Label tmpLabel = new Label(parent, SWT.CENTER);
	    gd = new GridData();
	    gd.verticalAlignment = GridData.CENTER;
	    tmpLabel.setLayoutData(gd);
	    
	    Label selectedLabel = new Label(parent, SWT.LEFT);
	    selectedLabel
				.setText("Selected");
	    gd = new GridData(GridData.FILL_HORIZONTAL);
	    selectedLabel.setLayoutData(gd);	
	}

	/**
	 * Returns this field editor's list control.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the list control
	 */
    public ListViewer createListViewer(Composite parent) {
		ListViewer viewer = new ListViewer(parent, SWT.BORDER | SWT.MULTI
				| SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setLabelProvider(new RodinElementLabelProvider());
		viewer
				.setContentProvider(new RodinElementTableContentProvider<T>(
						type));
		return viewer;
	}
    
    
	/**
	 * Create the set of buttons.
	 * 
	 * @param parent
	 *            the parent composite for creating the buttons.
	 */
	private void createButtons(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		container.setLayout(layout);
		
		// Add button.
		add = new Button(container, SWT.PUSH);
		add.setText("Add >> ");
		add.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				addElement();
			}
			
		});
		add.setLayoutData(new GridData());
		
		remove = new Button(container, SWT.PUSH);
		remove.setText("<< Remove");
		remove.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				removeElements();
			}
			
		});
		remove.setLayoutData(new GridData());
	}

	/**
	 * Utility method for adding selected elements.
	 */
	protected void addElement() {
		selectedElement.addAll(selectionToList(availableViewer.getSelection()));
		refresh();
	}

	/**
	 * Utility method for removing selected elements.
	 */
	protected void removeElements() {
		selectedElement
				.removeAll(selectionToList(selectedViewer.getSelection()));
		refresh();
	}

	/**
	 * Utility method for refreshing the two viewers.
	 */
	private void refresh() {
		availableViewer.refresh();
		selectedViewer.refresh();
	}

	/**
	 * @param selection
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Collection<T> selectionToList(ISelection selection) {
		assert selection instanceof IStructuredSelection;
		IStructuredSelection ssel = (IStructuredSelection) selection;
		Collection<T> result = new ArrayList<T>();
		Object[] array = ssel.toArray();
		for (Object obj : array) {
			result.add((T) obj);
		}
		return result;
	}

	/**
	 * Method for setting the input element of the viewer.
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
	 * Return the collection of selected elements.
	 * 
	 * @return the selected elements.
	 */
	public Collection<T> getSelected() {
		return selectedElement;
	}
	
}
