/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 ******************************************************************************/
package ch.ethz.eventb.internal.decomposition.sc;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IConfigurationElement;
import org.eventb.core.IContextRoot;
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

public class ContextModule extends SCProcessorModule {

	public static final IModuleType<MachineVariableModule> MODULE_TYPE = SCCore
			.getModuleType(DecompositionPlugin.PLUGIN_ID + ".contextModule"); //$NON-NLS-1$

	public void process(IRodinElement element, IInternalElement target,
			ISCStateRepository repository, IProgressMonitor monitor)
			throws CoreException {
		final IRodinFile contextFile = (IRodinFile) element;
		final IContextRoot contextRoot = (IContextRoot) contextFile.getRoot();

		IDecomposedElement elt = (IDecomposedElement) contextRoot
				.getAdapter(IDecomposedElement.class);
		if (!elt.isDecomposed())
			return;

		String fileName = EventBPlugin.getSCContextFileName(contextRoot
				.getElementName());
		IRodinFile scTmpFile = contextRoot.getRodinProject().getRodinFile(
				fileName + "_tmp"); //$NON-NLS-1$
		IConfigurationElement confElement = (IConfigurationElement) scTmpFile
				.getRoot();
		confElement.setConfiguration(EventBUtils.DECOMPOSITION_CONFIG_POG,
				monitor);
	}

	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

}
