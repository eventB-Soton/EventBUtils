/*******************************************************************************
 * Copyright (c) 2010 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.inst.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eventb.core.IEventBProject;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.inst.GenInstPlugin;
import ch.ethz.eventb.inst.IGenInst;
import ch.ethz.eventb.inst.IRenaming;
import ch.ethz.eventb.internal.inst.MachineRootRenaming;
import ch.ethz.eventb.internal.inst.ui.utils.Messages;
import ch.ethz.eventb.internal.inst.wizard.utils.AbstractWizardPage;


/**
 * <p>
 * This wizard page collecting information on how various elements going to be
 * renamed.
 * <ul>
 * <li>The variables.</li>
 * <li>The events (including it parameters).</li>
 * </ul>
 * </p>
 * 
 * @author htson
 */public class RenamingWizardPage extends AbstractWizardPage {

	// The editable tree viewer.
	private TreeViewer treeViewer;

	// The original column
	private TreeViewerColumn original;
	
	// The original column
	private TreeViewerColumn values;

	// Small offset for width of the table columns.
	private final int OFFSET = 1;

	// The first page, i.e. the component wizard page.
	private ComponentWizardPage componentPage;
	
	// The string for the NONE instantiation.
	private final static String NONE_RENAMING = "--";
	
	/**.
	 * Constructor.
	 */
	public RenamingWizardPage(ComponentWizardPage componentPage) {
		super(Messages.wizard_instantiationpage_name);
		this.componentPage = componentPage;
		setTitle(Messages.wizard_instantiationpage_title);
		setDescription(Messages.wizard_instantiationpage_description);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		container.setLayout(gl);

		// Create the table viewer.
		treeViewer = new TreeViewer(container, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);

		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		treeViewer.setContentProvider(new ITreeContentProvider() {
			
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// Do nothing.
				// The content should be synchronised with the variable
				// <code>renaming</code>.
			}
			
			public void dispose() {
				// Do nothing.
			}
			
			public Object[] getElements(Object inputElement) {
				assert(inputElement instanceof IRenaming);
				IRenaming renaming = (IRenaming) inputElement;
				return renaming.getChildren();
			}
			
			public boolean hasChildren(Object element) {
				return getElements(element).length != 0;
			}
			
			public Object getParent(Object element) {
				assert(element instanceof IRenaming);
				IRenaming renaming = (IRenaming) element;
				return renaming.getParent();
			}
			
			public Object[] getChildren(Object parentElement) {
				assert(parentElement instanceof IRenaming);
				IRenaming renaming = (IRenaming) parentElement;
				return renaming.getChildren();
			}
		});

		original = createColumn();
		original.setLabelProvider(new CellLabelProvider(){
		    @Override
		    public void update(ViewerCell cell) {
		    	Object element = cell.getElement();
				cell.setText(element.toString());
		    }

		});
		
		values = createColumn();

		values.setLabelProvider(new CellLabelProvider(){
		    @Override
		    public void update(ViewerCell cell) {
		    	Object element = cell.getElement();
		    	assert element instanceof IRenaming;
		    	IRenaming renaming = (IRenaming) element;
		    	cell.setText(renaming.getRenamingString());
		    }

		});

		values.setEditingSupport(new EditingSupport(treeViewer) {

		    @Override
		    protected boolean canEdit(Object element) {
		     	return true;
		    }

		    @Override
		    protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(treeViewer.getTree());
			}

		    @Override
		    protected Object getValue(Object element) {
		    	assert element instanceof IRenaming;
		    	return ((IRenaming) element).getRenamingString();
		    }

		    @Override
		    protected void setValue(Object element, Object value) {
		    	Assert.isTrue(value instanceof String);
		    	Assert.isTrue(element instanceof IRenaming);
				if (!value.equals(NONE_RENAMING)){
					IRenaming renaming = (IRenaming) element;
					renaming.setRenamingString((String) value);
		    		treeViewer.refresh();
		    	}
		    }

		});
		
		treeViewer.getTree().addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				int width = treeViewer.getTree().getSize().x;
				original.getColumn().setWidth(width/2-OFFSET);
				values.getColumn().setWidth(width/2-OFFSET);
				super.controlResized(e);
			}
			
		});
		
		// Create a save button.
		Button saveButton = new Button(container, SWT.PUSH);
		saveButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IGenInst genInst = componentPage.getGenInst();
				
				// Getting a name of the file to save through an input dialog.
				Shell shell = RenamingWizardPage.this
						.getShell();
				InputDialog dialog = new InputDialog(shell, "Enter a name of a file",
						"Dialog message", "GenInst", null);
				dialog.open();
				String barename = dialog.getValue();
				
				if (barename == null) {
					return;
				}
				
				// Check if the file exists then ask for overriding option.
				IEventBProject tgtPrj = genInst.getTargetProject();
				IRodinFile genInstFile = tgtPrj.getRodinProject().getRodinFile(
						GenInstPlugin.getGenInstFileName(barename));				
				int code = Dialog.OK;
				if (genInstFile.exists()) {
					Dialog overrideDialog = new YesNoDialog(
							shell,
							"Override?",
							"The file is already existed. Do you want to override the existing file or not?");
					overrideDialog.open();
					code = overrideDialog.getReturnCode();
				}
				
				// Do nothing if cancel is chosen. 
				if (code == Dialog.CANCEL)
					return;

				// Dump the information from GenInst to the file using dialog
				ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(
						RenamingWizardPage.this.getShell());
				SaveGenInstFile op = new SaveGenInstFile(genInstFile, genInst);
				try {
					monitorDialog.run(true, true, op);
				}
				catch (InvocationTargetException ite) {
					
				}
				catch (InterruptedException ie) {
					
				}
//				try {
//					RodinCore.run(op, new ProgressMonitorPart(
//							(Composite) RenamingWizardPage.this.getControl(), null, true));
//				} catch (RodinDBException ex) {
//					ex.printStackTrace();
//				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		// Initialise the components.
		initialise();

		// Update the status.
		updateStatus(null);

		setControl(container);
	}
	
	/**
	 * Utility method for creating a column in the main table viewer.
	 * 
	 * @return the newly created table column.
	 */
	private TreeViewerColumn createColumn() {
		TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.LEFT);
		int width = treeViewer.getTree().getSize().x;
		column.getColumn().setWidth(width/2);
		column.getColumn().setResizable(true);
		return column;
	}

	/**
	 * Utility method for initialising the renaming information and setting the
	 * initial input for the main table viewer.
	 */
	private void initialise() {
		IMachineRoot mch = componentPage.getBasedMachine();
		MachineRootRenaming renaming = new MachineRootRenaming(mch);
		IGenInst genInst = componentPage.getGenInst();
		genInst.setRenaming(renaming);
		
		if (treeViewer != null) {
			treeViewer.setInput(renaming);
			treeViewer.refresh(true);
			treeViewer.getTree().pack();
		}
	}

	/**
	 * Utility method to update the status message and also set the completeness
	 * of the page.
	 * 
	 * @param message
	 *            the error message or <code>null</code>.
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.inst.wizard.utils.IWizardPageChangedListener#pageChanged()
	 */
	public void pageChanged() {
		// Re-initialise the page if there is any change occur on the page that
		// this page depends on.
		initialise();
	}

	
}