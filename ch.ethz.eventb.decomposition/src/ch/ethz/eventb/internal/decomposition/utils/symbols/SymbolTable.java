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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.rodinp.core.IInternalElement;

/**
 * A symbol table stores symbols associated to their respective declared names.
 * Here is made the assumption that different symbols have different names. This
 * assumption holds for targeted elements: carrier sets, constants and
 * variables.
 * 
 * @author "Nicolas Beauger"
 * 
 */
public class SymbolTable {
	private final Map<String, Symbol> symbols = new LinkedHashMap<String, Symbol>();

	/**
	 * Puts a new symbol into the table, with the given name and element.
	 * 
	 * @param name
	 *            the name of the symbol
	 * @param element
	 *            the element associated with the name
	 */
	public void put(String name, IInternalElement element) {
		symbols.put(name, new Symbol(name, element));
	}

	/**
	 * Returns the symbol associated with the given name, or <code>null</code>
	 * if no such symbol is stored in the table.
	 * 
	 * @param name
	 *            a string name
	 * @return a symbol, or <code>null</code>
	 */
	public Symbol get(String name) {
		return symbols.get(name);
	}

	/**
	 * Returns the names of all symbols stored in this table.
	 * 
	 * @return a set of symbol names
	 */
	public Set<String> getNames() {
		return new LinkedHashSet<String>(symbols.keySet());
	}
}