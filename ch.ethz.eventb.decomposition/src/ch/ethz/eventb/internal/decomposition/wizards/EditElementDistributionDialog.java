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

public class EditElementDistributionDialog extends Dialog {

	IElementDistribution elemDist;
	Text prjText;
	String prjName;
	String [] evts;
	ElementListChooserViewer<IEvent> viewer;
	
	protected EditElementDistributionDialog(Shell parentShell,
			IElementDistribution elemDist) {
		super(parentShell);
		this.elemDist = elemDist;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		
		// Project name
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
		
		// Choosing events distribution
		viewer = new ElementListChooserViewer<IEvent>(prjNameComp,
				IEvent.ELEMENT_TYPE, "Choosing events' distribution");
		
		viewer.setInput(elemDist.getMachineRoot());
		return control;
	}
	
	

	@Override
	protected void okPressed() {
		prjName = prjText.getText();
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

	public String getProjectName() {
		return prjName;
	}

	public String [] getEvents() {
		return evts;
	}

}
