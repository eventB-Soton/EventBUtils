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
import java.util.Map.Entry;

import org.rodinp.core.location.IInternalLocation;


public class ReferenceTable {
	private final Map<IInternalLocation, Set<Symbol>> references = new LinkedHashMap<IInternalLocation, Set<Symbol>>();

	public void add(IInternalLocation location, Symbol symbol) {
		Set<Symbol> set = references.get(location);
		if (set == null) {
			set = new LinkedHashSet<Symbol>();
			references.put(location, set);
		}
		set.add(symbol);
	}

	public Set<Symbol> getSymbols() {
		final Set<Symbol> symbols = new LinkedHashSet<Symbol>();
		for (Set<Symbol> set : references.values()) {
			symbols.addAll(set);
		}
		return symbols;
	}

	public Set<Entry<IInternalLocation, Set<Symbol>>> entrySet() {
		return references.entrySet();
	}
}