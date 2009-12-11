/*******************************************************************************
 * Copyright (c) 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.decomposition.tests;

import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IConvergenceElement.Convergence;
import org.junit.Test;

import ch.ethz.eventb.decomposition.astyle.IExternalElement;
import ch.ethz.eventb.internal.decomposition.DecompositionUtils;

/**
 * @author htson
 *         <p>
 *         Test class for {@link DecompositionUtils}.
 *         </p>
 */
public class DecompositionUtilsTests extends AbstractDecompositionTests {

	/**
	 * Test method for
	 * {@link DecompositionUtils#setEventStatus(IEvent, IEvent, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testConvergenceAttributeOnInternal() throws Exception {
		IEventBProject prj = createRodinProject("P");
		// The non-decomposed machine with a source event
		IMachineRoot m = createMachine(prj, "M");
		IEvent srcEvt = createEvent(m, "evt");

		// The sub-machine with a destination event
		IMachineRoot m1 = createMachine(prj, "M1");
		IEvent destEvt = createEvent(m1, "evt");

		// If the source event is ordinary, the destination event is ordinary
		srcEvt.setConvergence(Convergence.ORDINARY, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is convergent, the destination event is ordinary
		srcEvt.setConvergence(Convergence.CONVERGENT, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is anticipated, the destination event is
		// anticipated
		srcEvt.setConvergence(Convergence.ANTICIPATED, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be anticipated", destEvt
				.getConvergence().equals(Convergence.ANTICIPATED));
	}

	/**
	 * Test method for
	 * {@link DecompositionUtils#setEventStatus(IEvent, IEvent, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testConvergenceAttributeOnExternal() throws Exception {
		IEventBProject prj = createRodinProject("P");
		// The non-decomposed machine with a source event
		IMachineRoot m = createMachine(prj, "M");
		IEvent srcEvt = createEvent(m, "evt");

		// The sub-machine with a destination event
		IMachineRoot m1 = createMachine(prj, "M1");
		IEvent destEvt = createEvent(m1, "evt");
		IExternalElement destElt = (IExternalElement) destEvt
				.getAdapter(IExternalElement.class);
		destElt.setExternal(true, null);

		// If the source event is ordinary, the destination event is ordinary
		srcEvt.setConvergence(Convergence.ORDINARY, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is convergent, the destination event is ordinary
		srcEvt.setConvergence(Convergence.CONVERGENT, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
		// If the source event is anticipated, the destination event is ordinary
		srcEvt.setConvergence(Convergence.ANTICIPATED, null);
		destEvt.setConvergence(Convergence.CONVERGENT, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be ordinary", destEvt
				.getConvergence().equals(Convergence.ORDINARY));
	}

	/**
	 * Test method for
	 * {@link DecompositionUtils#setEventStatus(IEvent, IEvent, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testExtendedAttribute() throws Exception {
		IEventBProject prj = createRodinProject("P");
		// The non-decomposed machine with a source event
		IMachineRoot m = createMachine(prj, "M");
		IEvent srcEvt = createEvent(m, "evt");

		// The sub-machine with a destination event
		IMachineRoot m1 = createMachine(prj, "M1");
		IEvent destEvt = createEvent(m1, "evt");

		// If the source event is non-extended, the destination event is
		// non-extended
		srcEvt.setExtended(false, null);
		destEvt.setExtended(true, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertFalse("The destination event should not be extended", destEvt
				.isExtended());

		// If the source event is extended, the destination event is
		// non-extended
		srcEvt.setExtended(true, null);
		destEvt.setExtended(true, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertFalse("The destination event should not be extended", destEvt
				.isExtended());
	}

	/**
	 * Test method for
	 * {@link DecompositionUtils#setEventStatus(IEvent, IEvent, org.eclipse.core.runtime.IProgressMonitor)}
	 * .
	 */
	@Test
	public void testExternalAttribute() throws Exception {
		IEventBProject prj = createRodinProject("P");
		// The non-decomposed machine with a source event
		IMachineRoot m = createMachine(prj, "M");
		IEvent srcEvt = createEvent(m, "evt");
		IExternalElement srcElt = (IExternalElement) srcEvt
				.getAdapter(IExternalElement.class);

		// The sub-machine with a destination event
		IMachineRoot m1 = createMachine(prj, "M1");
		IEvent destEvt = createEvent(m1, "evt");
		IExternalElement destElt = (IExternalElement) destEvt
				.getAdapter(IExternalElement.class);
		
		// If the source event is external, the destination event is external
		srcElt.setExternal(true, null);
		destElt.setExternal(false, null);
		DecompositionUtils.setEventStatus(srcEvt, destEvt, null);
		assertTrue("The destination event should be external", destElt
				.isExternal());
	}
	
}
