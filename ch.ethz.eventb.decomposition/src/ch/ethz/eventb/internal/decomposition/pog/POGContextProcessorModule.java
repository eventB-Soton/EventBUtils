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
package ch.ethz.eventb.internal.decomposition.pog;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.pog.POGCore;
import org.eventb.core.pog.POGProcessorModule;
import org.eventb.core.pog.state.IPOGStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IRodinElement;

import ch.ethz.eventb.decomposition.DecompositionPlugin;

public class POGContextProcessorModule extends POGProcessorModule {

	public static final IModuleType<POGContextProcessorModule> MODULE_TYPE = 
		POGCore.getModuleType(DecompositionPlugin.PLUGIN_ID + ".pogContextModule"); //$NON-NLS-1$
	
	public void process(IRodinElement element, IPOGStateRepository repository,
			IProgressMonitor monitor) throws CoreException {
		// Do nothing.
	}

	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

}
