/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 ******************************************************************************/

package ch.ethz.eventb.internal.decomposition.sc.astyle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IVariable;
import org.eventb.core.sc.SCCore;
import org.eventb.core.sc.SCFilterModule;
import org.eventb.core.sc.state.IIdentifierSymbolInfo;
import org.eventb.core.sc.state.IIdentifierSymbolTable;
import org.eventb.core.sc.state.ISCStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IRodinElement;

import ch.ethz.eventb.decomposition.DecompositionPlugin;
import ch.ethz.eventb.decomposition.astyle.INatureElement;

import static ch.ethz.eventb.decomposition.astyle.AStyleAttributes.NATURE_ATTRIBUTE;

public class MachineVariableModule extends SCFilterModule {

	public static final IModuleType<MachineVariableModule> MODULE_TYPE = SCCore
			.getModuleType(DecompositionPlugin.PLUGIN_ID + ".varModule"); //$NON-NLS-1$

	private IIdentifierSymbolTable identifierSymbolTable;

	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

	@Override
	public void initModule(ISCStateRepository repository,
			IProgressMonitor monitor) throws CoreException {
		identifierSymbolTable = (IIdentifierSymbolTable) repository
				.getState(IIdentifierSymbolTable.STATE_TYPE);
	}

	public boolean accept(IRodinElement element, ISCStateRepository repository,
			IProgressMonitor monitor) throws CoreException {

		IVariable variable = (IVariable) element;
		String variableName = variable.getIdentifierString();
		INatureElement elt = (INatureElement) variable
				.getAdapter(INatureElement.class);
		IIdentifierSymbolInfo symbolInfo = identifierSymbolTable
				.getSymbolInfo(variableName);
		symbolInfo.setAttributeValue(NATURE_ATTRIBUTE, elt.getNature()
				.getCode());
		return true;
	}

	@Override
	public void endModule(ISCStateRepository repository,
			IProgressMonitor monitor) throws CoreException {
		identifierSymbolTable = null;
	}

}
