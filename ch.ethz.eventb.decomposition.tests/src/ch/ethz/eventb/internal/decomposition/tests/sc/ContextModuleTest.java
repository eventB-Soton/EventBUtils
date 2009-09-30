/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 ******************************************************************************/

package ch.ethz.eventb.internal.decomposition.tests.sc;

import org.eventb.core.IContextRoot;
import org.eventb.core.IEventBProject;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.eventb.decomposition.IDecomposedElement;
import ch.ethz.eventb.internal.decomposition.tests.AbstractSCTests;
import ch.ethz.eventb.internal.decomposition.utils.EventBUtils;

/**
 * The class used to test the behavior of the static checker on contexts.
 */
public class ContextModuleTest extends AbstractSCTests {

	private IEventBProject prj;
	private IContextRoot context;

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		prj = createRodinProject("P");
		context = createContext(prj, "ctx");
		context.getRodinFile().save(monitor, false);
	}

	/**
	 * Checks that a non-decomposed context has the expected configuration.
	 */
	@Test
	public void testNonDecomposedContextConfiguration() throws Exception {
		// Builds the .bcm file
		runBuilder(prj);

		assertEquals(context.getSCContextRoot().getConfiguration(),
				EventBUtils.DECOMPOSITION_CONFIG_SC);
	}

	/**
	 * Checks that a decomposed context has the expected configuration.
	 */
	@Test
	public void testDecomposedContextConfiguration() throws Exception {
		IDecomposedElement elt = (IDecomposedElement) context
				.getAdapter(IDecomposedElement.class);
		elt.setDecomposed(monitor);
		context.getRodinFile().save(monitor, false);

		// Builds the .bcm file
		runBuilder(prj);

		assertEquals(context.getSCContextRoot().getConfiguration(),
				EventBUtils.DECOMPOSITION_CONFIG_POG);
	}
}
