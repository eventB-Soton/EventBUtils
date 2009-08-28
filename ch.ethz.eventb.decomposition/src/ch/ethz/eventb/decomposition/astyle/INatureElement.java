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

package ch.ethz.eventb.decomposition.astyle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rodinp.core.RodinDBException;

/**
 * Common protocol for Event-B nature of a variable.
 * <p>
 * The nature is represented by an integer constant. Only the specified values
 * {@link Nature#PRIVATE} and {@link Nature#SHARED} are allowed.
 * </p>
 * <p>
 * The attribute storing the nature is <i>optional</i>. Thus, if the attribute
 * is not present, the value should be interpreted as <i>undefined</i>.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface INatureElement {

	/**
	 * The enumerated type <code>Nature</code> specifies the different types of
	 * variables. Each type is associated with an integer code used to represent
	 * it in the database. The codes are public and can be used if necessary to
	 * access the database without this interface.
	 */
	enum Nature {
		PRIVATE(0), SHARED(1);

		private final int code;

		Nature(int code) {
			this.code = code;
		}

		/**
		 * Gets the code associated to this nature.
		 * 
		 * @return the code
		 */
		public int getCode() {
			return code;
		}

		private static final INatureElement.Nature[] natures = new INatureElement.Nature[] {
				INatureElement.Nature.PRIVATE, INatureElement.Nature.SHARED };

		/**
		 * Gets the nature with the specified code.
		 * 
		 * @param n
		 *            the code of the nature to be returned
		 * @return the nature with the <tt>n</tt> code
		 */
		public static Nature valueOf(int n) {
			if (n < 0 || n > 1)
				throw new IllegalArgumentException("Nature value out of range");
			return natures[n];
		}

	}

	/**
	 * Checks whether the nature is defined or not.
	 * 
	 * @return <tt>true</tt> if and only if the nature is defined.
	 * @throws RodinDBException
	 *             if there was a problem when accessing the database.
	 */
	boolean hasNature() throws RodinDBException;

	/**
	 * Sets the nature to one of the values {@link Nature#PRIVATE} or
	 * {@link Nature#SHARED}.
	 * 
	 * @param value
	 *            the nature to be set
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if the progress
	 *            reporting is not desired
	 * @throws RodinDBException
	 *             if there was a problem when accessing the database
	 */
	void setNature(Nature value, IProgressMonitor monitor)
			throws RodinDBException;

	/**
	 * Returns the nature stored in this attribute.
	 * 
	 * @return the nature; one of {@link Nature#PRIVATE}, {@link Nature#SHARED}
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	Nature getNature() throws RodinDBException;
}
