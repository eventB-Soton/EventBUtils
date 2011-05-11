package ch.ethz.eventb.internal.inst.wizard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class YesNoDialog extends Dialog {

    /**
     * The title of the dialog.
     */
    private String title;

    /**
     * The message to display, or <code>null</code> if none.
     */
    private String message;

	protected YesNoDialog(Shell parentShell, String title, String message) {
		super(parentShell);
		this.title = title;
		this.message = message;
	}

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
    }
    
    protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.YES_ID,
				IDialogConstants.YES_LABEL, false);
		createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL,
				true);
	}
	
    protected void buttonPressed(int buttonId) {
    	if (buttonId == IDialogConstants.YES_ID) {
    		setReturnCode(OK);
    		close();
    	}
    	else if (buttonId == IDialogConstants.NO_ID) {
    		setReturnCode(CANCEL);
    		close();	
    	}
    	super.buttonPressed(buttonId);
    }
    
	protected Control createDialogArea(Composite parent) {
        // create composite
        Composite composite = (Composite) super.createDialogArea(parent);
        
		if (message != null) {
			Label label = new Label(composite, SWT.WRAP);
			label.setText(message);
			GridData data = new GridData(GridData.GRAB_HORIZONTAL
					| GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(parent.getFont());
        }
        
        applyDialogFont(composite);
        return composite;
	}
}
