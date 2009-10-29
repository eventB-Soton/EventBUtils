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

import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

/**
 * Symbol gatherer for machines.
 * 
 * @author "Nicolas Beauger"
 * 
 */
public class MachineSymbolGatherer extends SymbolGatherer<IMachineRoot> {

	public MachineSymbolGatherer(IMachineRoot root) {
		super(root);
	}

	@Override
	public void addDeclaredSymbols(SymbolTable symbolTable)
			throws RodinDBException {
		addIdentifiers(root.getVariables(), symbolTable);
	}

	@Override
	public void addReferencedSymbols(SymbolTable symbolTable,
			ReferenceTable referenceTable) throws RodinDBException {
		addPredicateReferences(root.getInvariants(), symbolTable,
				referenceTable);
		addExpressionReferences(root.getVariants(), symbolTable, referenceTable);
		IEvent[] events = root.getEvents();
		for (IEvent event : events) {
			addPredicateReferences(event.getGuards(), symbolTable,
					referenceTable);
			addAssignmentReferences(event.getActions(), symbolTable,
					referenceTable);
		}
	}
}