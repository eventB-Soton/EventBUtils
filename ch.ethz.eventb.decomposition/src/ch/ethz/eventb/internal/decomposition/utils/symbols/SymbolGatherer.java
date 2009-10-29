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

import static org.rodinp.core.RodinCore.getInternalLocation;

import java.util.Set;

import org.eventb.core.EventBAttributes;
import org.eventb.core.IAssignmentElement;
import org.eventb.core.IEventBRoot;
import org.eventb.core.IExpressionElement;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.IPredicateElement;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.GivenType;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.location.IAttributeLocation;

/**
 * A symbol gatherer scans a model and populates a symbol table with declared or
 * occurring symbols.
 * 
 * @author "Nicolas Beauger"
 * 
 * @param <T>
 *            a root type
 */
public abstract class SymbolGatherer<T extends IEventBRoot> {
	protected final T root;

	/**
	 * Constructor.
	 * 
	 * @param root
	 *            the root of the model to scan
	 */
	public SymbolGatherer(T root) {
		this.root = root;
	}

	/**
	 * Populates the given symbol table with declarations found in the root.
	 * 
	 * @param symbolTable
	 *            a symbol table to populate
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	public abstract void addDeclaredSymbols(SymbolTable symbolTable)
			throws RodinDBException;

	/**
	 * Populates the given reference table with the occurrences of symbols that
	 * are declared in the given symbol table and are found in the root.
	 * 
	 * @param symbolTable
	 *            a table containing symbols to reference
	 * @param referenceTable
	 *            a reference table to populate
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	public abstract void addReferencedSymbols(SymbolTable symbolTable,
			ReferenceTable referenceTable) throws RodinDBException;

	protected static void addIdentifiers(IIdentifierElement[] identifiers,
			SymbolTable symbolTable) throws RodinDBException {
		for (IIdentifierElement identifier : identifiers) {
			symbolTable.put(identifier.getIdentifierString(), identifier);
		}
	}

	protected void addPredicateReferences(IPredicateElement[] predicates,
			SymbolTable symbolTable, ReferenceTable referenceTable)
			throws RodinDBException {
		for (IPredicateElement predicate : predicates) {
			final Predicate parsedPred = Lib.parsePredicate(predicate
					.getPredicateString());

			final IAttributeLocation location = getInternalLocation(predicate,
					EventBAttributes.ASSIGNMENT_ATTRIBUTE);
			addSymbolReferences(parsedPred, symbolTable, referenceTable,
					location);
		}
	}

	protected void addExpressionReferences(IExpressionElement[] expressions,
			SymbolTable symbolTable, ReferenceTable referenceTable)
			throws RodinDBException {
		for (IExpressionElement expression : expressions) {
			final Expression parsedExpr = Lib.parseExpression(expression
					.getExpressionString());
			final IAttributeLocation location = getInternalLocation(expression,
					EventBAttributes.EXPRESSION_ATTRIBUTE);
			addSymbolReferences(parsedExpr, symbolTable, referenceTable,
					location);
		}
	}

	protected void addAssignmentReferences(IAssignmentElement[] assignments,
			SymbolTable symbolTable, ReferenceTable referenceTable)
			throws RodinDBException {
		for (IAssignmentElement assignment : assignments) {
			final Assignment parsedAssign = Lib.parseAssignment(assignment
					.getAssignmentString());

			final IAttributeLocation location = getInternalLocation(assignment,
					EventBAttributes.ASSIGNMENT_ATTRIBUTE);
			addSymbolReferences(parsedAssign, symbolTable, referenceTable,
					location);
		}
	}

	private void addSymbolReferences(Formula<?> formula,
			SymbolTable symbolTable, ReferenceTable referenceTable,
			IAttributeLocation location) throws RodinDBException {
		final FreeIdentifier[] freeIdentifiers = formula
				.getSyntacticallyFreeIdentifiers();

		for (FreeIdentifier identifier : freeIdentifiers) {
			final String name = identifier.getName();
			final Symbol symbol = symbolTable.get(name);
			if (symbol != null) {
				// add directly referenced symbols
				referenceTable.add(location, symbol);
				// add implicit type references
				final Expression typingExpression = symbol.getTypeExpression();
				if (typingExpression != null) {
					addTypeReferences(typingExpression, symbolTable,
							referenceTable, location);
				}
			}
		}
	}

	private static void addTypeReferences(Expression typingExpression,
			SymbolTable symbolTable, ReferenceTable referenceTable,
			IAttributeLocation location) {
		final Set<GivenType> givenTypes = typingExpression.getGivenTypes();
		for (GivenType givenType : givenTypes) {
			final String typeName = givenType.getName();
			final Symbol typeSymbol = symbolTable.get(typeName);
			if (typeSymbol != null) {
				referenceTable.add(location, typeSymbol);
			}
		}
	}
}