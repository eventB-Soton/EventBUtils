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

public class ElementListChooserViewer<T extends IRodinElement> {

	ListViewer availableViewer;
	
	ListViewer selectedViewer;
	
	List<T> selectedElement;
	
	IElementType<T> type;
	
	String title;
	
	public ElementListChooserViewer(Composite parent, IElementType<T> type, String title) {
		this.type = type;
		selectedElement = new ArrayList<T>();
		this.title = title;
		createControl(parent);
	}

	private void createControl(Composite comp) {
		Group parent = new Group(comp, SWT.DEFAULT);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalSpan = 3;
		parent.setLayoutData(layoutData);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		parent.setLayout(layout);
		parent.setText(title);
		createLabels(parent);
        availableViewer = createListViewer(parent);
		createButtons(parent);
		selectedViewer = createListViewer(parent);
		availableViewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				return !selectedElement.contains(element);
			}
			
		});
		selectedViewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				return selectedElement.contains(element);
			}
			
		});		
	}
	
	/**
     * Returns this field editor's list control.
     *
     * @param parent the parent control
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
    
    
	private void createButtons(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		container.setLayout(layout);
		
		Button add = new Button(container, SWT.PUSH);
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
		
		Button remove = new Button(container, SWT.PUSH);
		remove.setText("<< Remove");
		remove.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				removeElement();
			}
			
		});
		remove.setLayoutData(new GridData());
	}

	protected void removeElement() {
		selectedElement
				.removeAll(selectionToList(selectedViewer.getSelection()));
		refresh();
	}

	private void refresh() {
		availableViewer.refresh();
		selectedViewer.refresh();
	}

	protected void addElement() {
		selectedElement.addAll(selectionToList(availableViewer.getSelection()));
		refresh();
	}

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

	public void setInput(IParent input) {
		availableViewer.setInput(input);
		selectedViewer.setInput(input);
		selectedElement = new ArrayList<T>();
	}

	public Collection<T> getSelected() {
		return selectedElement;
	}
	
}
