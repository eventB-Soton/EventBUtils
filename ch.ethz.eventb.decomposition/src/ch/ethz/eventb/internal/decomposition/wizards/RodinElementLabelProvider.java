package ch.ethz.eventb.internal.decomposition.wizards;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.ILabeledElement;
import org.rodinp.core.RodinDBException;

public class RodinElementLabelProvider extends LabelProvider implements IBaseLabelProvider {

	@Override
	public Image getImage(Object element) {
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ILabeledElement) {
			try {
				return ((ILabeledElement) element).getLabel();
			} catch (RodinDBException e) {
				return super.getText(element);
			}
		}
		if (element instanceof IIdentifierElement) {
			try {
				return ((IIdentifierElement) element).getIdentifierString();
			} catch (RodinDBException e) {
				return super.getText(element);
			}
	    }
		
		return super.getText(element);
	}

}
