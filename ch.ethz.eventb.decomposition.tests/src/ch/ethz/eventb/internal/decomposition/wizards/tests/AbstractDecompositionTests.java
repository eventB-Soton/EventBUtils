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

package ch.ethz.eventb.internal.decomposition.wizards.tests;

import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;

import ch.ethz.eventb.internal.decomposition.wizards.IElementDistribution;
import ch.ethz.eventb.internal.decomposition.wizards.IModelDistribution;
import ch.ethz.eventb.internal.decomposition.wizards.ModelDistribution;

/**
 * @author htson
 *         <p>
 *         Abstract class for testing related with decomposition, e.g. for
 *         {@link IElementDistribution}, {@link IModelDistribution},
 *         {@link DecompostionUtils}.
 *         </p>
 */
public class AbstractDecompositionTests extends EventBTests {

	// Some model distributions.
	protected IModelDistribution modelDist1;

	protected IModelDistribution modelDist2;
	
	protected IModelDistribution modelDist3;

	// Some element distributions.
	protected IElementDistribution elemDist1;
	
	protected IElementDistribution elemDist2;
	
	protected IElementDistribution elemDist3;
	
	/* (non-Javadoc)
	 * @see ch.ethz.eventb.internal.decomposition.wizards.tests.EventBTests#setUp()
	 */
	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Create the model distributions.
		modelDist1 = new ModelDistribution(mch1_1);
		modelDist2 = new ModelDistribution(mch1_2);
		modelDist3 = new ModelDistribution(mch1_3);
		
		// Create the element distributions.
		elemDist1 = modelDist3.createElementDistribution();
		elemDist1.setEventLabels("evt1_3_1", "evt1_3_5");
		
		elemDist2 = modelDist3.createElementDistribution();
		elemDist2.setEventLabels("evt1_3_2", "evt1_3_3");

		elemDist3 = modelDist3.createElementDistribution();
		elemDist3.setEventLabels("evt1_3_4");
		
		// ensure autobuilding is turned on
		IWorkspaceDescription wsDescription = workspace.getDescription();
		if (wsDescription.isAutoBuilding()) {
			wsDescription.setAutoBuilding(true);
			workspace.setDescription(wsDescription);
		}

		workspace.build(IncrementalProjectBuilder.FULL_BUILD,
				new NullProgressMonitor());
	}

}
