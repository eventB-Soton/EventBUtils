/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 ******************************************************************************/

package ch.ethz.eventb.internal.decomposition.tests;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.IEventBProject;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProblem;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinMarkerUtil;


/**
 * The class used to test the behavior of the static checker on refinements.
 */
public abstract class AbstractSCTests extends AbstractEventBTests {

	protected final static IProgressMonitor monitor = new NullProgressMonitor();
	
	protected void runBuilder(IEventBProject prj) throws CoreException {
		final IRodinProject rp = (IRodinProject) prj
				.getAdapter(IRodinProject.class);
		final IProject project = rp.getProject();
		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		IMarker[] buildPbs = project.findMarkers(
				RodinMarkerUtil.BUILDPATH_PROBLEM_MARKER, true,
				IResource.DEPTH_INFINITE);
		if (buildPbs.length != 0) {
			for (IMarker marker : buildPbs) {
				System.out.println("Build problem for " + marker.getResource());
				System.out.println("  " + marker.getAttribute(IMarker.MESSAGE));
			}
			fail("Build produced build problems, see console");
		}
	}

	protected void containsMarkers(IInternalElement element, boolean yes)
			throws CoreException {
		IFile file = element.getResource();
		IMarker[] markers = file.findMarkers(
				RodinMarkerUtil.RODIN_PROBLEM_MARKER, true,
				IResource.DEPTH_INFINITE);

		if (yes)
			assertTrue("should contain markers", markers.length != 0);
		else
			assertEquals("should not contain markers", 0, markers.length);
	}

	protected void hasMarker(IRodinElement element, IAttributeType attrType)
			throws Exception {
		hasMarker(element, attrType, null);
	}

	protected void hasMarker(IRodinElement element, IAttributeType attrType,
			IRodinProblem problem, String... args) throws Exception {
		IRodinFile file = (IRodinFile) element.getOpenable();
		IMarker[] markers = file.getResource().findMarkers(
				RodinMarkerUtil.RODIN_PROBLEM_MARKER, true,
				IResource.DEPTH_INFINITE);
		for (IMarker marker : markers) {
			IRodinElement elem = RodinMarkerUtil.getInternalElement(marker);
			if (elem != null && elem.equals(element)) {
				if (attrType != null) {
					IAttributeType attributeType = RodinMarkerUtil
							.getAttributeType(marker);
					assertEquals("problem not attached to attribute", attrType,
							attributeType);
				}
				if (problem == null)
					return;
				if (problem.getErrorCode().equals(
						RodinMarkerUtil.getErrorCode(marker))) {
					String[] pargs = RodinMarkerUtil.getArguments(marker);
					assertEquals(args.length, pargs.length);
					for (int i = 0; i < args.length; i++) {
						assertEquals(args[i], pargs[i]);
					}
					return;
				}
			}
		}
		fail("problem marker missing from element"
				+ ((attrType != null) ? " (attribute: " + attrType.getId()
						+ ")" : ""));
	}
}
