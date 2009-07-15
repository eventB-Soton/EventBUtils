/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/

package ch.ethz.eventb.internal.decomposition.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         An implementation of {@link IModelDistribution}.
 *         </p>
 */
public class ModelDistribution implements IModelDistribution {

	// The machine associated with the distribution.
	private IMachineRoot mch;
	
	// A list of element sub-distributions.
	private List<IElementDistribution> elemDists;
	
	// The set of shared variables. This need to be reset to <code>null</code>
	// if the element sub-distributions changed.
	private Set<String> sharedVars;
	
	/**
	 * Constructor. Create a model distribution of a machine.
	 * 
	 * @param mch
	 *            a machine.
	 */
	public ModelDistribution(IMachineRoot mch) {
		this.mch = mch;
		elemDists = new ArrayList<IElementDistribution>();
		sharedVars = new HashSet<String>();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IModelDistribution#getDistributions()
	 */
	public IElementDistribution[] getElementDistributions() {
		return elemDists.toArray(new IElementDistribution[elemDists.size()]);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IModelDistribution#createElementDistribution()
	 */
	public IElementDistribution createElementDistribution() {
		IElementDistribution elemDist = new ElementDistribution(this);
		elemDists.add(elemDist);
		
		// Mark out of date
		this.setOutOfDate();
		return elemDist;
	}

	/**
	 * Utility method for setting out-of-date.
	 */
	public void setOutOfDate() {
		sharedVars = null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IModelDistribution#getMachineRoot()
	 */
	public IMachineRoot getMachineRoot() {
		return mch;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IModelDistribution#removeElementDistribution(ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution)
	 */
	public void removeElementDistribution(IElementDistribution elemDist) {
		elemDists.remove(elemDist);
		// Mark out of date
		this.setOutOfDate();
		return;
	}

	/**
	 * Utility method for calculating the set of shared variables based on the
	 * element sub-distributions. A variable is shared if it appears in more
	 * than one element sub-distributions.
	 * 
	 * @throws RodinDBException
	 *             if some errors occurred.
	 */
	private void calculateSharedVariables() throws RodinDBException {
		if (!this.isOutOfDate())
			return;
		
		IVariable[] vars = mch.getVariables();
		
		sharedVars = new HashSet<String>();
		
		for (IVariable var : vars) {
			int occurrance = 0;
			String ident = var.getIdentifierString();
			for (IElementDistribution elemDist : elemDists) {
				if (elemDist.getAccessedVariables().contains(ident)) {
					occurrance++;
				}
				if (occurrance > 1)
					break;
			}
			if (occurrance != 1) {
				sharedVars.add(ident);
			}
			
		}
		
	}

	/**
	 * Checking if the information is currently out-of-date.
	 * @return
	 */
	private boolean isOutOfDate() {
		return sharedVars == null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IModelDistribution#getSharedVariables()
	 */
	public Set<String> getSharedVariables() throws RodinDBException {
		if (this.isOutOfDate())
			calculateSharedVariables();
		return sharedVars;
	}

}
