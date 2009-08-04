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

package ch.ethz.eventb.internal.decomposition.astyle;

import java.util.HashSet;
import java.util.Set;

import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.internal.decomposition.IModelDecomposition;
import ch.ethz.eventb.internal.decomposition.ISubModel;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;

/**
 * @author htson
 *         <p>
 *         Implementation of {@link ISubModel} for A-style decomposition. 
 *         Each sub-model is issued from a model decomposition. A sub-model 
 *         relies on an array of event labels, which can be set by
 *         the method {@link #setEvents(String...)}.
 *         </p>
 */
public class SubModel implements ISubModel {

	// The project name.
	private String prjName;

	// The set of accessed variables.
	private Set<String> vars;
	
	// The set of event labels.
	private String[] evtLabels;
	
	// The associated model distribution.
	private ModelDecomposition modelDecomp;

	/**
	 * Constructor. Creates a sub-model. The name of the project, the set of 
	 * events and the accessed variables are initialized to default values.
	 * 
	 * @param modelDecomp
	 *            a model decomposition.
	 */
	public SubModel(IModelDecomposition modelDecomp) {
		this.modelDecomp = (ModelDecomposition) modelDecomp;
		this.prjName = ISubModel.DEFAULT_PROJECT_NAME;
		evtLabels = new String[0];
		vars = new HashSet<String>();
	}
	
	public ModelDecomposition getModelDecomposition() {
		return modelDecomp;
	}

	public IMachineRoot getMachineRoot() {
		return modelDecomp.getMachineRoot();
	}

	public void setProjectName(String prjName) {
		this.prjName = prjName;
	}

	public String getProjectName() {
		return prjName;
	}

	public void setEvents(String ... evtLabels) {
		this.evtLabels = evtLabels;
		this.setOutOfDate();
	}

	public String[] getEvents() {
		return evtLabels;
	}

	/**
	 * Utility method to mark the sub-model as out-of-date. It resets the
	 * sets of accessed variables and shared variables to <code>null</code>.
	 */
	private void setOutOfDate() {
		vars = null;
		getModelDecomposition().setOutOfDate();
	}

	/**
	 * Utility method to calculate the set of accessed variables.
	 * 
	 * @throw RodinDBException if a problem occurs when accessing the Rodin
	 *        database.
	 */
	private void calculateAccessedVariables() throws RodinDBException {
		if (!isOutOfDate())
			return;
		
		IMachineRoot mch = this.getMachineRoot();
		vars = new HashSet<String>();
		// Adds the free identifiers from the events.
		for (String evtLabel : evtLabels) {
			for (IEvent event : mch.getEvents()) {
				if (event.getLabel().equals(evtLabel)) {
					vars.addAll(EventBUtils.getFreeIdentifiers(event));
				}
			}
		}
		// Removes the constants and sets.
		vars.removeAll(EventBUtils.getSeenCarrierSetsAndConstants(mch));
	}

	public Set<String> getAccessedVariables() throws RodinDBException {
		if (isOutOfDate()) {
			calculateAccessedVariables();
		}
		return vars;
	}

	/**
	 * Utility method to check if the sub-model is out-of-date.
	 * 
	 * @return <code>true</code> if and only if the status is out-of-date.
	 */
	private boolean isOutOfDate() {
		return vars == null;
	}

}
