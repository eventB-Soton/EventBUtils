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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.inst.IGenInst;
import ch.ethz.eventb.internal.inst.ui.utils.Messages;
import ch.ethz.eventb.internal.inst.wizard.utils.AbstractWizardPage;
import ch.ethz.eventb.utils.EventBSCUtils;

/**
 * <p>
 * This wizard page collecting information on how various elements going to be
 * instantiated.
 * <ul>
 * <li>The source carrier sets.</li>
 * <li>The source constants.</li>
 * </ul>
 * </p>
 * 
 * @author htson
 */
public class InstantiationWizardPage extends AbstractWizardPage {

	// The editable table viewer.
	private  TableViewer tableViewer;

	// The original column
	private TableViewerColumn original;
	
	// The original column
	private TableViewerColumn values;

	// Small offset for width of the table columns.
	private final int OFFSET = 1;

	// The previous page, i.e. the component wizard page.
	private ComponentWizardPage componentPage;
	
	// The string for the NONE instantiation.
	private final static String NONE_INSTANTIATION = "--";
	
	/**
	 * Constructor.
	 */
	public InstantiationWizardPage(ComponentWizardPage componentPage) {
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
		tableViewer = new TableViewer(container, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);

		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		tableViewer.setContentProvider(new IStructuredContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// Do nothing
			}

			public void dispose() {
				// Do nothing
			}

			public Object[] getElements(Object inputElement) {
				IGenInst genInst = componentPage.getGenInst();
				Map<IRodinElement, String> instantiation = genInst.getInstantiation();
				Set<IRodinElement> keySet = instantiation.keySet();
				return keySet.toArray(new IRodinElement[keySet.size()]);
			}
		});

		original = createColumn();
		original.setLabelProvider(new CellLabelProvider(){
		    @Override
		    public void update(ViewerCell cell) {
		    	Object element = cell.getElement();
				Assert.isTrue(element instanceof IIdentifierElement,
						"Element must have an identifier");
				try {
					cell.setText(((IIdentifierElement) element)
							.getIdentifierString());
				} catch (RodinDBException e) {
					e.printStackTrace();
				}
		    	
		    }

		});
		
		values = createColumn();

		values.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				IGenInst genInst = componentPage.getGenInst();
				Map<IRodinElement, String> instantiation = genInst
						.getInstantiation();
				cell.setText(instantiation.get(cell.getElement()));
			}

		});

		values.setEditingSupport(new EditingSupport(tableViewer) {

		    @Override
		    protected boolean canEdit(Object element) {
		    	// Value can always be edited.
		     	return true;
		    }

		    @Override
		    protected CellEditor getCellEditor(Object element) {
				// Create a text cell editor for constant.
				return new TextCellEditor(tableViewer.getTable());
			}

		    @Override
		    // For ComboBoxCellEditor, the value is the index of the list.
		    protected Object getValue(Object element) {
				IGenInst genInst = componentPage.getGenInst();
				Map<IRodinElement, String> instantiation = genInst
						.getInstantiation();
				return instantiation.get(element);
		    }

		    @Override
		    protected void setValue(Object element, Object value) {
				Assert.isTrue(value instanceof String);
				Assert.isTrue(element instanceof IRodinElement);
				if (!value.equals(NONE_INSTANTIATION)) {
					IGenInst genInst = componentPage.getGenInst();
					genInst.setInstantiation((IRodinElement) element,
							(String) value);
					tableViewer.refresh();
					updateStatus();
				}
		    }

		});
		
		tableViewer.getTable().addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				int width = tableViewer.getTable().getSize().x;
				original.getColumn().setWidth(width/2-OFFSET);
				values.getColumn().setWidth(width/2-OFFSET);
				super.controlResized(e);
			}
			
		});
		
		initialise();
		
		setPageComplete(getError() == null);

		setControl(container);
	}
	
	/**
	 * Utility method for creating a column in the main table viewer.
	 * 
	 * @return the newly created table column.
	 */
	private TableViewerColumn createColumn() {
		TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.LEFT);
		int width = tableViewer.getTable().getSize().x;
		column.getColumn().setWidth(width/2);
		column.getColumn().setResizable(true);
		return column;
	}
	
	
	/**
	 * Utility method for initialising the generic instantiation information and
	 * setting the initial input for the main table viewer.
	 */
	private void initialise() {
		IMachineRoot mch = componentPage.getBasedMachine();
		try {
			Collection<IRodinElement> seenElements = EventBSCUtils
					.getSeenCarrierSetsAndConstants(mch);
			IGenInst genInst = componentPage.getGenInst();
			genInst.initInstantiation(seenElements, NONE_INSTANTIATION);
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (tableViewer != null) {
			IGenInst genInst = componentPage.getGenInst();
			Map<IRodinElement, String> instantiation = genInst.getInstantiation();
			tableViewer.setInput(instantiation);
			tableViewer.refresh(true);
			tableViewer.getTable().pack();
		}
	}

	/**
	 * Utility method to update the status message and also set the completeness
	 * of the page.
	 * 
	 * @param message
	 *            the error message or <code>null</code>.
	 */
	private void updateStatus(final String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/**
	 * Utility method to update the status message by checking for error within
	 * the page.
	 */
	private void updateStatus() {
		updateStatus(getError());
	}

	/**
	 * Utility method for checking error within the page.
	 * 
	 * @return the error message if any, <code>null</code> otherwise.
	 */
	private String getError() {
		IGenInst genInst = componentPage.getGenInst();
		if (!genInst.isFullyInstantiated())
			return "All element must be instantiated";
		return null;
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