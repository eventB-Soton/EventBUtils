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

import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.eventb.core.ast.Expression;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;

/**
 * A symbol is an identifier element declared in a context (carrier set,
 * constant) or a machine (variable).
 * 
 * @author "Nicolas Beauger"
 * 
 */
public class Symbol {
	private final String name;
	private final IInternalElement element;

	public Symbol(String name, IInternalElement element) {
		this.name = name;
		this.element = element;
	}

	/**
	 * Returns the name of this symbol.
	 * 
	 * @return a string
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the element associated with this symbol.
	 * 
	 * @return an internal element
	 */
	public IInternalElement getElement() {
		return element;
	}

	/**
	 * Returns the type of the element associated with this symbol.
	 * <p>
	 * This is fully equivalent to
	 * 
	 * <pre>
	 * getElement().getElementType()
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @return an internal element type
	 */
	public IInternalElementType<?> getElementType() {
		return element.getElementType();
	}

	/**
	 * Returns the type expression of the element associated with this symbol,
	 * or <code>null</code> if it could not be computed.
	 * <p>
	 * Relies on the fact that the element has been statically checked.
	 * </p>
	 * 
	 * @return a type expression, or <code>null</code>
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	public Expression getTypeExpression() throws RodinDBException {
		if (element.getElementType() == IConstant.ELEMENT_TYPE) {
			return EventBUtils.getTypeExpression((IContextRoot) element
					.getRoot(), name);
		}
		if (element.getElementType() == IVariable.ELEMENT_TYPE) {
			return EventBUtils.getTypeExpression((IMachineRoot) element
					.getRoot(), name);
		}
		return null;
	}

	/**
	 * Returns the typing theorem for the element associated with this symbol,
	 * or <code>null</code> if it could not be computed.
	 * 
	 * @return the string of the typing theorem, or <code>null</code>
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	public String getTypingTheorem() throws RodinDBException {
		final Expression typingExpression = getTypeExpression();
		if (typingExpression == null) {
			return null;
		}
		return EventBUtils.makeTypingTheorem(name, typingExpression);
	}
}