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

import java.util.HashSet;
import java.util.Set;

import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         Implementation of {@link IElementDistribution}. Each element
 *         distribution is associated with an model distribution. The element
 *         distribution depends on an array of event labels, which can be set by
 *         the method {@link #setEventLabels(String...)}.
 *         </p>
 */
public class ElementDistribution implements IElementDistribution {

	// The project name.
	private String prjName;

	// The set of accessed variables.
	private Set<String> vars;
	
	// The set of event labels.
	private String[] evtLabels;
	
	// The associated model distribution.
	private ModelDistribution modelDist;

	/**
	 * Constructor. Create the element distribution associated with a model
	 * distribution. The name of the project, the set of event labels and the
	 * accessed variables are initialised to default values.
	 * 
	 * @param modelDist
	 *            a model distribution.
	 */
	public ElementDistribution(IModelDistribution modelDist) {
		this.modelDist = (ModelDistribution) modelDist;
		this.prjName = IElementDistribution.DEFAULT_PROJECT_NAME;
		evtLabels = new String[0];
		vars = new HashSet<String>();
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution#getModelDistribution()
	 */
	public ModelDistribution getModelDistribution() {
		return modelDist;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution#getMachineRoot()
	 */
	public IMachineRoot getMachineRoot() {
		return modelDist.getMachineRoot();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution#setProjectName(java.lang.String)
	 */
	public void setProjectName(String prjName) {
		this.prjName = prjName;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution#getProjectName()
	 */
	public String getProjectName() {
		return prjName;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution#setEventLabels(java.lang.String[])
	 */
	public void setEventLabels(String ... evtLabels) {
		this.evtLabels = evtLabels;
		this.setOutOfDate();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution#getEventLabels()
	 */
	public String[] getEventLabels() {
		return evtLabels;
	}

	/**
	 * Utility method to mark the distribution out-of-date by resetting the set
	 * of accessed variables to <code>null</code>.
	 */
	private void setOutOfDate() {
		vars = null;
		this.getModelDistribution().setOutOfDate();
	}

	/**
	 * Utility method for calculate the set of shared variables.
	 */
	private void calculateAccessedVariables() {
		if (!this.isOutOfDate())
			return;
		
		IMachineRoot mch = this.getMachineRoot();
		
		vars = new HashSet<String>();
		
		IEvent[] events;
		try {
			events = mch.getEvents();
		} catch (RodinDBException e) {
			e.printStackTrace();
			return;
		}

		// Add the free identifiers from the events.
		for (String evtLabel : evtLabels) {
			try {
				for (IEvent event : events) {
					if (event.getLabel().equals(evtLabel)) {
						vars.addAll(EventBUtils.getFreeIdentifiers(event));
					}
				}
			} catch (RodinDBException e) {
				e.printStackTrace();
			}
		}
		
		// Remove the constants and sets.
		try {
			vars.removeAll(EventBUtils.getSeenCarrierSetsAndConstants(mch));
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution#getAccessedVariables()
	 */
	public Set<String> getAccessedVariables() {
		if (this.isOutOfDate()) {
			calculateAccessedVariables();
		}
		return vars;
	}

	/**
	 * Utility method for checking if the distribution is out-of-date.
	 * 
	 * @return <code>true</code> if the distribution is out-of-date, otherwise
	 *         return <code>false</code>.
	 */
	private boolean isOutOfDate() {
		return vars == null;
	}

}
