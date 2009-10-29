/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.decomposition.utils.symbols;

import org.eventb.core.IContextRoot;
import org.rodinp.core.RodinDBException;

/**
 * Symbol gatherer for contexts.
 * 
 * @author "Nicolas Beauger"
 * 
 */
public class ContextSymbolGatherer extends SymbolGatherer<IContextRoot> {

	public ContextSymbolGatherer(IContextRoot root) {
		super(root);
	}

	@Override
	public void addDeclaredSymbols(SymbolTable symbolTable) throws RodinDBException {
		addIdentifiers(root.getCarrierSets(), symbolTable);
		addIdentifiers(root.getConstants(), symbolTable);
	}

	@Override
	public void addReferencedSymbols(SymbolTable symbolTable, ReferenceTable referenceTable)
			throws RodinDBException {
		addPredicateReferences(root.getAxioms(), symbolTable, referenceTable);
	}

}