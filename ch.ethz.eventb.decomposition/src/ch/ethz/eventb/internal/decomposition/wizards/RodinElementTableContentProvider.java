package ch.ethz.eventb.internal.decomposition.wizards;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.rodinp.core.IElementType;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

public class RodinElementTableContentProvider<T extends IRodinElement>
		implements IStructuredContentProvider {

	private IElementType<T> type;

	public RodinElementTableContentProvider(IElementType<T> type) {
		this.type = type;
	}
	
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IParent)
			try {
				return ((IParent) inputElement).getChildrenOfType(type);
			} catch (RodinDBException e) {
				return null;
			}
		return null;
	}

	public void dispose() {
		// Do nothing
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Do nothing
	}


}
