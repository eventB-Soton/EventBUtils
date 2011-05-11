package ch.ethz.eventb.internal.inst.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.inst.IGenInst;

public class SaveGenInstFile implements IRunnableWithProgress {
	private IRodinFile genInstFile;
	
	private IGenInst genInst;
	
	public SaveGenInstFile(IRodinFile genInstFile, IGenInst genInst) {
		this.genInstFile = genInstFile;
		this.genInst = genInst;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		try {
			genInst.saveAs(genInstFile, monitor);
		} catch (RodinDBException e) {
			throw new InvocationTargetException(e);
		}
	}

	
}
