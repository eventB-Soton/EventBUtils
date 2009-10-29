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
package ch.ethz.eventb.internal.decomposition.tests.symbols;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eventb.core.IMachineRoot;
import org.junit.Test;

import ch.ethz.eventb.internal.decomposition.tests.AbstractDecompositionTests;
import ch.ethz.eventb.internal.decomposition.utils.symbols.ContextSymbolGatherer;
import ch.ethz.eventb.internal.decomposition.utils.symbols.MachineSymbolGatherer;
import ch.ethz.eventb.internal.decomposition.utils.symbols.ReferenceTable;
import ch.ethz.eventb.internal.decomposition.utils.symbols.Symbol;
import ch.ethz.eventb.internal.decomposition.utils.symbols.SymbolTable;

/**
 * @author "Nicolas Beauger"
 *
 */
public class SymbolGathererTests extends AbstractDecompositionTests {

	private static void assertSymbolTable(SymbolTable symbolTable, String... expected) {
		final Set<String> actualNames = symbolTable.getNames();
		
		assertNames(actualNames, expected);
	}

	
	private static void assertReferenceTable(ReferenceTable referenceTable, String... expected) {
		final Set<Symbol> symbols = referenceTable.getSymbols();
		final Set<String> actualNames = getNames(symbols);
		assertEquals("Duplicate symbol names !!!", symbols.size(), actualNames.size());
		assertNames(actualNames, expected);
	}

	private static Set<String> getNames(Collection<Symbol> symbols) {
		final Set<String> names = new LinkedHashSet<String>();
		for (Symbol symbol : symbols) {
			names.add(symbol.getName());
		}
		return names;
	}
	
	private static void assertNames(final Set<String> actualNames,
			String... expected) {
		for (String name : expected) {
			assertTrue("Missing name: "+ name, actualNames.contains(name));
		}
		assertEquals("Wrong number of names", expected.length, actualNames.size());
	}
	
	@Test
	public void testContextAddDeclaredSymbols() throws Exception {
		final ContextSymbolGatherer symbGthr = new ContextSymbolGatherer(ctx1_1);
		final SymbolTable symbolTable = new SymbolTable();
		symbGthr.addDeclaredSymbols(symbolTable);
		
		assertSymbolTable(symbolTable, "a", "b", "c", "d", "S", "T");
	}

	@Test
	public void testMachineAddDeclaredSymbols() throws Exception {
		final MachineSymbolGatherer symbGthr = new MachineSymbolGatherer(mch1_1);
		final SymbolTable symbolTable = new SymbolTable();
		symbGthr.addDeclaredSymbols(symbolTable);
		
		assertSymbolTable(symbolTable, "x", "y");
	}

	@Test
	public void testContextAddReferencedSymbols() throws Exception {
		final ContextSymbolGatherer symbGthr = new ContextSymbolGatherer(ctx1_1);
		final SymbolTable symbolTable = new SymbolTable();
		symbGthr.addDeclaredSymbols(symbolTable);

		final ReferenceTable referenceTable = new ReferenceTable();
		symbGthr.addReferencedSymbols(symbolTable, referenceTable);
		
		assertReferenceTable(referenceTable, "a", "b", "c", "d", "S");
	} 
	
	@Test
	public void testMachineAddReferencedSymbols() throws Exception {
		final ContextSymbolGatherer ctxSymbGthr = new ContextSymbolGatherer(ctx1_1);
		final SymbolTable symbolTable = new SymbolTable();
		ctxSymbGthr.addDeclaredSymbols(symbolTable);

		final MachineSymbolGatherer mchSymbGthr = new MachineSymbolGatherer(mch1_1);
		final ReferenceTable referenceTable = new ReferenceTable();
		mchSymbGthr.addReferencedSymbols(symbolTable, referenceTable);
		
		assertReferenceTable(referenceTable, "a", "b", "S");
	} 
	
	@Test
	public void testImplicitTypeReference() throws Exception {
		// ctx1_1 in P1 defines set S and constant a of type S
		// ctx1_1 is assumed to be built and to have a corresponding SC file
		final ContextSymbolGatherer ctxSymbGthr = new ContextSymbolGatherer(ctx1_1);
		final SymbolTable symbolTable = new SymbolTable();
		ctxSymbGthr.addDeclaredSymbols(symbolTable);

		final IMachineRoot mch = createMachine(P1, "mch1_4");

		createVariable(mch, "x");
		// implicit reference to S which types a (and therefore x)
		createInvariant(mch, "inv", "x = a", false);

		final MachineSymbolGatherer mchSymbGthr = new MachineSymbolGatherer(mch);
		final ReferenceTable referenceTable = new ReferenceTable();
		mchSymbGthr.addReferencedSymbols(symbolTable, referenceTable);

		// check that S is also referenced
		assertReferenceTable(referenceTable, "a", "S");
	}

}
