package ch.ethz.eventb.internal.decomposition.sc;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IConfigurationElement;
import org.eventb.core.IMachineRoot;
import org.eventb.core.sc.SCCore;
import org.eventb.core.sc.SCProcessorModule;
import org.eventb.core.sc.state.ISCStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;

import ch.ethz.eventb.decomposition.DecompositionPlugin;
import ch.ethz.eventb.decomposition.IDecomposedElement;
import ch.ethz.eventb.internal.decomposition.sc.astyle.MachineVariableModule;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;

public class MachineModule extends SCProcessorModule {

	public static final IModuleType<MachineVariableModule> MODULE_TYPE = SCCore
			.getModuleType(DecompositionPlugin.PLUGIN_ID + ".machineModule"); //$NON-NLS-1$

	public void process(IRodinElement element, IInternalElement target,
			ISCStateRepository repository, IProgressMonitor monitor)
			throws CoreException {
		final IRodinFile machineFile = (IRodinFile) element;
		final IMachineRoot machineRoot = (IMachineRoot) machineFile.getRoot();

		IDecomposedElement elt = (IDecomposedElement) machineRoot
				.getAdapter(IDecomposedElement.class);
		if (!elt.isDecomposed())
			return;

		String fileName = EventBPlugin.getSCMachineFileName(machineRoot
				.getElementName());
		IRodinFile scTmpFile = machineRoot.getRodinProject().getRodinFile(
				fileName + "_tmp");
		IConfigurationElement confElement = (IConfigurationElement) scTmpFile
				.getRoot();
		confElement.setConfiguration(EventBUtils.DECOMPOSITION_CONFIG_POG,
				monitor);
	}

	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

}
