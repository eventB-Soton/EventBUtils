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

package ch.ethz.eventb.internal.decomposition.tests.astyle.sc;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eventb.core.IEventBRoot;
import org.rodinp.core.IRodinProblem;
import org.rodinp.core.RodinMarkerUtil;

import ch.ethz.eventb.decomposition.DecompositionPlugin;
import ch.ethz.eventb.internal.decomposition.astyle.sc.DecompositionProblem;

/**
 * The class used to check the problems detected by the static checker.
 */
public class DecompositionProblemTest extends TestCase {

	static {
		DecompositionPlugin.getDefault();
	}

	private static class Spec implements Comparable<Spec> {

		public final DecompositionProblem problem;
		public final int arity;

		public Spec(final DecompositionProblem problem, final int arity) {
			this.problem = problem;
			this.arity = arity;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Spec && problem.equals(((Spec) obj).problem);
		}

		@Override
		public int hashCode() {
			return problem.hashCode();
		}

		@Override
		public String toString() {
			return problem.toString() + "/" + arity;
		}

		public int compareTo(Spec o) {
			return problem.compareTo(o.problem);
		}
	}

	private static Spec spec(final DecompositionProblem problem, final int arity) {
		return new Spec(problem, arity);
	}

	private static Spec[] specs = new Spec[] {
			spec(DecompositionProblem.EventHasDisappearedError, 1),
			spec(DecompositionProblem.EventInvalidStatusError, 2),
			spec(DecompositionProblem.VariableHasDisappearedError, 1),
			spec(DecompositionProblem.VariableInvalidNatureError, 1),
			spec(DecompositionProblem.ParametersInExternalEventError, 0),
			spec(DecompositionProblem.GuardsInExternalEventError, 0),
			spec(DecompositionProblem.ActionsInExternalEventError, 0),
			spec(DecompositionProblem.ActionOnPrivateAndSharedError, 1),
			spec(DecompositionProblem.ActionOnSharedError, 1) };

	private static Map<DecompositionProblem, Spec> specMap = new EnumMap<DecompositionProblem, Spec>(
			DecompositionProblem.class);
	static {
		for (Spec spec : specs) {
			specMap.put(spec.problem, spec);
		}
	}

	/**
	 * Checks whether the messages loaded from the properties take the correct
	 * number of parameters.
	 */
	public void testArguments() throws Exception {
		for (Spec spec : specs) {
			assertEquals("wrong number of arguments", spec.arity, spec.problem
					.getArity());
		}
	}

	/**
	 * Checks whether the messages loaded from the properties file are complete.
	 */
	public void testMessages() throws Exception {
		Set<IRodinProblem> problems = new HashSet<IRodinProblem>(
				specs.length * 4 / 3 + 1);
		for (Spec spec : specs) {
			problems.add(spec.problem);
		}
		for (IRodinProblem problem : DecompositionProblem.values()) {
			boolean found = problems.contains(problem);
			assertTrue("No spec for problem " + problem, found);
		}
	}

	public static boolean check(IEventBRoot root) throws CoreException {
		boolean ok = true;
		IMarker[] markers = root.getResource().findMarkers(
				RodinMarkerUtil.RODIN_PROBLEM_MARKER, true,
				IResource.DEPTH_INFINITE);
		for (IMarker marker : markers) {
			String errorCode = RodinMarkerUtil.getErrorCode(marker);
			DecompositionProblem problem;
			try {
				problem = DecompositionProblem.valueOfErrorCode(errorCode);
			} catch (IllegalArgumentException e) {
				// not a graph problem
				continue;
			}
			Spec spec = specMap.get(problem);
			assertNotNull("missing problem spec", spec);
			int k = RodinMarkerUtil.getArguments(marker).length;
			if (spec.arity != k) {
				ok = false;
				System.out.println("Wrong number of arguments "
						+ problem.toString() + "/" + k + " expected: "
						+ spec.arity);
			}
		}
		return ok;
	}

}
