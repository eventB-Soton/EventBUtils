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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IAxiom;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IEventBProject;
import org.eventb.core.IGuard;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.junit.Before;

/**
 * @author htson
 *         <p>
 *         Abstract class for testing manipulation of Event-B elements. Some
 *         simple models are created by the setup method {@link #setUp()}.
 *         </p>
 */
public abstract class EventBTests extends AbstractEventBTests {

	/**
	 * Some predefined projects.
	 */
	protected IEventBProject P1;
	
	protected IEventBProject P2;
	
	protected IEventBProject P3;
	
	protected IEventBProject P4;

	/**
	 * Some predefined contexts:
	 * - ctx1_1, ctx1_2, ctx1_3 in project P1.
	 * - ctx2_1, ctx2_2 in project P2.
	 */
	protected IContextRoot ctx1_1;
	
	protected IContextRoot ctx1_2;
	
	protected IContextRoot ctx1_3;
	
	protected IContextRoot ctx2_1;
	
	protected IContextRoot ctx2_2;
	
	/**
	 * Some carrier sets.
	 */
	protected ICarrierSet S;
	
	protected ICarrierSet T;
	
	protected ICarrierSet U;
	
	protected ICarrierSet V;

	/**
	 * Some constants.
	 */
	protected IConstant a;
	
	protected IConstant b;
	
	protected IConstant c;
	
	protected IConstant d;
	
	protected IConstant e;
	
	protected IConstant f;
	
	protected IConstant g;
	
	protected IConstant h;
	
	/**
	 * Some axioms and theorems.
	 */
	protected IAxiom axm1_1_1;
	
	protected IAxiom axm1_1_2;
	
	protected IAxiom axm1_1_3;

	protected IAxiom axm1_2_1;
	
	protected IAxiom axm1_2_2;
	
	protected IAxiom axm1_3_1;
	
	protected IAxiom axm1_3_2;

	/**
	 * Some predefined machines.
	 * - mch1_1, mch1_2, mch1_3 in project P1.
	 * - mch2_1, mch2_2 in project P2.
	 */
	protected IMachineRoot mch1_1;
	
	protected IMachineRoot mch1_2;
	
	protected IMachineRoot mch1_3;
	
	protected IMachineRoot mch2_1;
	
	protected IMachineRoot mch3_1;
	
	protected IMachineRoot mch4_1;
	
	/**
	 * Some variables.
	 */
	IVariable x;

	IVariable y;
	
	IVariable u;
	
	IVariable v;

	IVariable z;

	IVariable p;
	
	/**
	 * Some invariants within machines.
	 */
	protected IInvariant inv1_1_1;
	
	protected IInvariant inv1_1_2;
	
	protected IInvariant inv1_1_3;
		
	protected IInvariant inv1_2_1;
	
	protected IInvariant inv1_2_2;
	
	protected IInvariant inv1_2_3;
	
	protected IInvariant inv1_3_1;

	protected IInvariant inv1_3_2;

	protected IInvariant inv1_3_3;

	protected IInvariant inv1_3_4;
	
	protected IInvariant inv1_3_5;

	/**
	 * Some events within machines.
	 */
	protected IEvent init1_1;
	
	protected IEvent evt1_1_1;

	protected IEvent evt1_1_2;

	protected IEvent evt1_1_3;

	protected IEvent init1_2;
	
	protected IEvent evt1_2_1;
	
	protected IEvent evt1_2_2;

	protected IEvent evt1_2_3;

	protected IEvent evt1_2_4;

	protected IEvent init1_3;

	protected IEvent evt1_3_1;

	protected IEvent evt1_3_2;
	
	protected IEvent evt1_3_3;

	protected IEvent evt1_3_4;
	
	protected IEvent evt1_3_5;

	/**
	 * Some guards within events
	 */
	protected IGuard grd1_1_2_1;

	protected IGuard grd1_1_3_1;

	protected IGuard grd1_1_3_2;

	protected IGuard grd1_2_1_1;

	protected IGuard grd1_2_2_1;

	protected IGuard grd1_2_3_1;

	protected IGuard grd1_2_4_1;

	protected IGuard grd1_3_1_1;

	protected IGuard grd1_3_2_1;
	
	protected IGuard grd1_3_2_2;

	protected IGuard grd1_3_3_1;

	protected IGuard grd1_3_3_2;

	protected IGuard grd1_3_3_3;

	protected IGuard grd1_3_4_1;
	
	protected IGuard grd1_3_4_2;
	
	protected IGuard grd1_3_5_1;

	/**
	 * Some actions within events
	 */
	protected IAction act1_1_1_1;
	
	protected IAction act1_1_1_2;

	protected IAction act1_1_2_1;

	protected IAction act1_1_3_1;

	protected IAction act1_1_3_2;

	protected IAction act1_2_1_1;

	protected IAction act1_2_1_2;

	protected IAction act1_2_2_1;

	protected IAction act1_2_2_2;

	protected IAction act1_2_3_1;

	protected IAction act1_2_4_1;
	
	protected IAction act_init1_3_1;

	protected IAction act_init1_3_2;
	
	protected IAction act_init1_3_3;
	
	protected IAction act1_3_1_1;

	protected IAction act1_3_2_1;

	protected IAction act1_3_2_2;

	protected IAction act1_3_2_3;

	protected IAction act1_3_3_1;

	protected IAction act1_3_3_2;
	
	protected IAction act1_3_4_1;
	
	protected IAction act1_3_5_1;

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Create a few projects
		P1 = createRodinProject("P1"); //$NON-NLS-1$
		P2 = createRodinProject("P2"); //$NON-NLS-1$
		P3 = createRodinProject("P3"); //$NON-NLS-1$
		P4 = createRodinProject("P4"); //$NON-NLS-1$
		
		// Create some contexts inside the projects
		ctx1_1 = createContext(P1, "ctx1_1");
		ctx1_2 = createContext(P1, "ctx1_2");
		ctx1_3 = createContext(P1, "ctx1_3");
		ctx2_1 = createContext(P2, "ctx2_1");
		ctx2_2 = createContext(P2, "ctx2_2");
		
		// Create content of ctx1_1.
		// CONTEXT ctx1_1
		// SETS S, T
		// CONSTANTS a, b, c, d
		// AXIOMS
		//   axm1_1_1: partition(S, {a}, {b}, {c})
		//   axm1_1_2: d : ℕ
		//   thm1_1_3: a /= b
		// END
		S = createCarrierSet(ctx1_1, "S");
		T = createCarrierSet(ctx1_1, "T");
		a = createConstant(ctx1_1, "a");
		b = createConstant(ctx1_1, "b");
		c = createConstant(ctx1_1, "c");
		d = createConstant(ctx1_1, "d");
		axm1_1_1 = createAxiom(ctx1_1, "axm1_1_1",
				"partition(S, {a}, {b}, {c})", false);
		axm1_1_2 = createAxiom(ctx1_1, "axm1_1_2",
				"d ∈ ℕ", false);
		axm1_1_3 = createAxiom(ctx1_1, "thm1_1_3", "a ≠ b", true);
		ctx1_1.getRodinFile().save(new NullProgressMonitor(), false);
		
		// Create content for ctx1_2.
		// CONTEXT ctx1_2
		// SETS U
		// CONSTANTS e, f
		// AXIOMS
		//   axm1_2_1: partition(U, {e}, {f})
		//   thm1_2_2: e /= f
		// END
		U = createCarrierSet(ctx1_2, "U");
		e = createConstant(ctx1_2, "e");
		f = createConstant(ctx1_2, "f");
		axm1_2_1 = createAxiom(ctx1_2, "axm1_2_1", "partition(U, {e}, {f})",
				false);
		axm1_2_2 = createAxiom(ctx1_2, "thm1_2_2", "e ≠ f", true);
		ctx1_2.getRodinFile().save(new NullProgressMonitor(), false);
		
		// Create content for ctx1_3.
		// CONTEXT ctx1_3
		// REFINES ctx1_1, ctx1_2
		// SETS V
		// CONSTANTS g, h
		// AXIOMS
		//   axm1_3_1: partition(V, {g}, {h})
		//   thm1_3_2: g /= h
		// END
		createExtendsContextClause(ctx1_3, "ctx1_1");
		createExtendsContextClause(ctx1_3, "ctx1_2");
		V = createCarrierSet(ctx1_3, "V");
		g = createConstant(ctx1_3, "g");
		h = createConstant(ctx1_3, "h");
		axm1_3_1 = createAxiom(ctx1_3, "axm1_3_1", "partition(V, {g}, {h})",
				false);
		axm1_3_2 = createAxiom(ctx1_3, "thm1_3_2", "g ≠ h", true);
		ctx1_3.getRodinFile().save(new NullProgressMonitor(), false);
		
		// Create some machines inside the projects
		mch1_1 = createMachine(P1, "mch1_1");
		mch1_2 = createMachine(P1, "mch1_2");
		mch1_3 = createMachine(P1, "mch1_3");
		mch2_1 = createMachine(P2, "mch2_1");
		mch3_1 = createMachine(P3, "mch3_1");
		mch4_1 = createMachine(P4, "mch4_1");
		
		// Create content for mch1_1.
		// MACHINE mch1_1
		// SEES ctx1_1
		// VARIABLES x, y
		// INVARIANTS
		//   inv1_1_1: x : S
		//   inv1_1_2: y : N
		//   thm1_1_3: y >= 0
		// EVENTS
		//   ...
		// END
		createSeesContextClause(mch1_1, "ctx1_1");
		x = createVariable(mch1_1, "x");
		y = createVariable(mch1_1, "y");
		inv1_1_1 = createInvariant(mch1_1, "inv1_1_1", "x ∈ S", false);
		inv1_1_2 = createInvariant(mch1_1, "inv1_1_2", "y ∈ ℕ", false);
		inv1_1_3 = createInvariant(mch1_1, "thm1_1_3", "y ≥ 0", true);
		
		//   INITIALISATION
		//   STATUS ordinary
		//   BEGIN
		//     act_init_1_1_1: x := a
		//     act_init_1_1_2: y :: NAT
		//   END
		init1_1 = createEvent(mch1_1, IEvent.INITIALISATION);
		createAction(init1_1, "act_init_1_1_1", "x ≔ a");
		createAction(init1_1, "act_init_1_1_2", "y :∈ ℕ");
		
		//   evt1_1_1
		//   STATUS ordinary
		//   BEGIN
		//     act1_1_1_1: x :: {a, b}
		//     act1_1_1_2: y := 0
		//   END
		evt1_1_1 = createEvent(mch1_1, "evt1_1_1");
		act1_1_1_1 = createAction(evt1_1_1, "act1_1_1_1", "x :∈ {a, b}");
		act1_1_1_2 = createAction(evt1_1_1, "act1_1_1_2", "y ≔ 0");
		
		//   evt1_1_2
		//   STATUS ordinary
		//   WHEN
		//     grd1_1_2_1: y /= 0
		//   THEN
		//     act1_1_2_1: x, y :| x' = b & y' = y + 2
		//   END
		//
		evt1_1_2 = createEvent(mch1_1, "evt1_1_2");
		grd1_1_2_1 = createGuard(evt1_1_2, "grd1_1_2_1", "y ≠ 0");
		act1_1_2_1 = createAction(evt1_1_2, "act1_1_2_1",
				"x, y :∣ x' = b ∧ y' = y + 2");
		
		//   evt1_1_3
		//   STATUS ordinary
		//   ANY s
		//   WHERE
		//     grd1_1_3_1: s : S
		//     grd1_1_3_2: y >= 5
		//   THEN
		//     act1_1_3_1: x :: S \ {s}
		//     act1_1_3_2: y :| y' = y - 4
		//   END
		//
		evt1_1_3 = createEvent(mch1_1, "evt1_1_3");
		createParameter(evt1_1_3, "s");
		grd1_1_3_1 = createGuard(evt1_1_3, "grd1_1_3_1", "s ∈ S");
		grd1_1_3_2 = createGuard(evt1_1_3, "grd1_1_3_2", "y ≥ 5");
		act1_1_3_1 = createAction(evt1_1_3, "act1_1_3_1", "x :∈ S ∖ {s}");
		act1_1_3_2 = createAction(evt1_1_3, "act1_1_3_2", "y :∣ y' = y − 4");
		
		// Save mch1_1
		mch1_1.getRodinFile().save(new NullProgressMonitor(), false);

		// Create content for mch1_2.
		// MACHINE mch1_2
		// REFINES mch1_1
		// SEES ctx1_1, ctx1_2
		// VARIABLES x, y, u, v
		// INVARIANTS
		//   inv1_2_1: u : U
		//   inv1_2_2: v : N
		//   thm1_2_3: v >= 0
		// EVENTS
		//   ...
		// END
		createRefinesMachineClause(mch1_2, "mch1_1");
		createSeesContextClause(mch1_2, "ctx1_1");
		createSeesContextClause(mch1_2, "ctx1_2");
		createVariable(mch1_2, "x");
		createVariable(mch1_2, "y");
		u = createVariable(mch1_2, "u");
		v = createVariable(mch1_2, "v");
		inv1_2_1 = createInvariant(mch1_2, "inv1_2_1", "u ∈ U", false);
		inv1_2_2 = createInvariant(mch1_2, "inv1_2_2", "v ∈ ℕ", false);
		inv1_2_3 = createInvariant(mch1_2, "thm1_2_3", "v ≥ 0", true);

		// INITIALISATION
		//   extended
		// STATUS ordinary
		// BEGIN
		//   act_init_1_2_1: u := e
		//   act_init_1_2_2: v := 0
		// END
		init1_2 = createEvent(mch1_2, IEvent.INITIALISATION);
		init1_2.setExtended(true, new NullProgressMonitor());
		createAction(init1_2, "act_init_1_2_1", "u ≔ e");
		createAction(init1_2, "act_init_1_2_2", "v ≔ 0");
		
		// evt1_2_1
		// STATUS ordinary
		// REFINES evt1_1_1
		// WHEN
		//   grd1_2_1_1: y = 0
		// THEN
		//   act1_2_1_1: x :: {a, b}
		//   act1_2_1_2: v := y + 2
		// END
		evt1_2_1 = createEvent(mch1_2, "evt1_2_1");
		createRefinesEventClause(evt1_2_1, "evt1_1_1");
		grd1_2_1_1 = createGuard(evt1_2_1, "grd1_2_1_1", "y = 0");
		act1_2_1_1 = createAction(evt1_2_1, "act1_2_1_1", "x :∈ {a, b}");
		act1_2_1_2 = createAction(evt1_2_1, "act1_2_1_2", "v ≔ y + 2");
		
		// evt1_2_2
		//   extended
		// STATUS ordinary
		// REFINES evt1_1_2
		// WHEN
		//   grd1_2_2_1: u = f
		// THEN
		//   act1_2_2_1: u := e
		//   act1_2_2_2: v := v + 1
		// END
		evt1_2_2 = createEvent(mch1_2, "evt1_2_2");
		evt1_2_2.setExtended(true, new NullProgressMonitor());
		createRefinesEventClause(evt1_2_2, "evt1_1_2");
		grd1_2_2_1 = createGuard(evt1_2_2, "grd1_2_2_1", "u = f");
		act1_2_2_1 = createAction(evt1_2_2, "act1_2_2_1", "u ≔ e");
		act1_2_2_2 = createAction(evt1_2_2, "act1_2_2_2", "v ≔ v + 1");
		
		// evt1_2_3
		//   extended
		// STATUS ordinary
		// REFINES evt1_1_3
		// WHEN
		//   grd1_2_3_1: y > v
		// THEN
		//   act1_2_3_1: v := v + 1
		// END
		evt1_2_3 = createEvent(mch1_2, "evt1_2_3");
		evt1_2_3.setExtended(true, new NullProgressMonitor());
		createRefinesEventClause(evt1_2_3, "evt1_1_3");
		grd1_2_3_1 = createGuard(evt1_2_3, "grd1_2_3_1", "y > v");
		act1_2_3_1 = createAction(evt1_2_3, "act1_2_3_1", "v ≔ v + 1");
		
		// evt1_2_4
		// STATUS ordinary
		// WHEN
		//   grd1_2_4_1: v >= 3
		// THEN
		//   act1_2_4_1: v := v - 1
		//   act1_2_4_2: u := e
		// END
		evt1_2_4 = createEvent(mch1_2, "evt1_2_4");
		grd1_2_4_1 = createGuard(evt1_2_4, "grd1_2_4_1", "v ≥ 3");
		act1_2_4_1 = createAction(evt1_2_4, "act1_2_4_1", "v, u ≔ v − 1, e");
		
		// Save mch1_2
		mch1_2.getRodinFile().save(new NullProgressMonitor(), false);

		// Create content mch1_3.
		// MACHINE mch1_3
		// REFINES mch1_2
		// SEES ctx1_3
		// VARIABLES y, u, v, z, p
		// INVARIANTS
		//   inv1_3_1: z = 0 => y = 0
		//   inv1_3_2: z = 0 => x = a
		//   inv1_3_3: z /= 0 => x = b
		//   thm1_3_4: x /= c
		//   inv1_3_5: p : NAT --> V
		// EVENTS
		//   ...
		// END
		createRefinesMachineClause(mch1_3, "mch1_2");
		createSeesContextClause(mch1_3, "ctx1_3");
		createVariable(mch1_3, "y");
		createVariable(mch1_3, "u");
		createVariable(mch1_3, "v");
		z = createVariable(mch1_3, "z");
		p = createVariable(mch1_3, "p");
		inv1_3_1 = createInvariant(mch1_3, "inv1_3_1", "z = 0 ⇒ y = 0", false);
		inv1_3_2 = createInvariant(mch1_3, "inv1_3_2", "z = 0 ⇒ x = a", false);
		inv1_3_3 = createInvariant(mch1_3, "inv1_3_3", "z ≠ 0 ⇒ x = b", false);
		inv1_3_4 = createInvariant(mch1_3, "thm1_3_4", "x ≠ c", true);
		inv1_3_5 = createInvariant(mch1_3, "inv1_3_5", "p ∈ ℕ → V", false);
		
		// INITIALISATION
		// STATUS ordinary
		// BEGIN
		//   act_init1_3_1: y := 0
		//   act_init1_3_2: u, v := e, 0
		//   act_init1_3_3: z, p := 0, NAT ** {g}
		// END
		init1_3 = createEvent(mch1_3, IEvent.INITIALISATION);
		act_init1_3_1 = createAction(init1_3, "act_init1_3_1", "y ≔ 0");
		act_init1_3_2 = createAction(init1_3, "act_init1_3_2", "u, v ≔ e, 0");
		act_init1_3_3 = createAction(init1_3, "act_init1_3_3", "z, p ≔ 0, ℕ × {g}");
		
		// evt1_3_1
		// STATUS ordinary
		// REFINES evt1_2_1
		// WHEN
		//   grd1_3_1_1: z = 0
		// WITH
		//   x': x' = x
		// THEN
		//   act1_3_1_1: v := 2
		// END
		evt1_3_1 = createEvent(mch1_3, "evt1_3_1");
		createRefinesEventClause(evt1_3_1, "evt1_2_1");
		grd1_3_1_1 = createGuard(evt1_3_1, "grd1_3_1_1", "z = 0");
		createWitness(evt1_3_1, "x'", "x' = x");
		act1_3_1_1 = createAction(evt1_3_1, "act1_3_1_1", "v ≔ 2");
		
		// evt1_3_2
		// STATUS ordinary
		// REFINES evt1_2_2
		// WHEN
		//   grd1_3_2_1: y /= 0
		//   grd1_3_2_2: u = f
		// WITH
		//   x': x' = x
		// THEN
		//   act1_3_2_1: y := y + 2
		//   act1_3_2_2: u := e
		//   act1_3_2_3: v := v + 1
		// END
		evt1_3_2 = createEvent(mch1_3, "evt1_3_2");
		createRefinesEventClause(evt1_3_2, "evt1_2_2");
		grd1_3_2_1 = createGuard(evt1_3_2, "grd1_3_2_1", "y ≠ 0");
		grd1_3_2_2 = createGuard(evt1_3_2, "grd1_3_2_2", "u = f");
		createWitness(evt1_3_2, "x'", "x' = x");
		act1_3_2_1 = createAction(evt1_3_2, "act1_3_2_1", "y ≔ y + 2");
		act1_3_2_2 = createAction(evt1_3_2, "act1_3_2_2", "u ≔ e");
		act1_3_2_3 = createAction(evt1_3_2, "act1_3_2_3", "v ≔ v + 1");

		// evt1_3_3
		// STATUS ordinary
		// REFINES evt1_2_3
		// ANY t
		// WHEN
		//   grd1_3_3_1: t /= a
		//   grd1_3_3_2: y >= 5
		//   grd1_3_3_3: y > v
		// WITH
		//   s : s = a
		//   x': x' = x
		// THEN
		//   act1_3_3_1: y :| y' = y - 4
		//   act1_3_3_2: v := v + 1
		// END
		evt1_3_3 = createEvent(mch1_3, "evt1_3_3");
		createRefinesEventClause(evt1_3_3, "evt1_2_3");
		createParameter(evt1_3_3, "t");
		grd1_3_3_1 = createGuard(evt1_3_3, "grd1_3_3_1", "t ≠ a");
		grd1_3_3_2 = createGuard(evt1_3_3, "grd1_3_3_2", "y ≥ 5");
		grd1_3_3_3 = createGuard(evt1_3_3, "grd1_3_3_3", "y > v");
		createWitness(evt1_3_3, "s", "s = a");
		createWitness(evt1_3_3, "x'", "x' = x");
		act1_3_3_1 = createAction(evt1_3_3, "act1_3_3_1", "y :∣ y' = y − 4");
		act1_3_3_2 = createAction(evt1_3_3, "act1_3_3_2", "v ≔ v + 1");
	
		// evt1_3_4
		//   extended
		// STATUS ordinary
		// REFINES evt1_2_4
		evt1_3_4 = createEvent(mch1_3, "evt1_3_4");
		evt1_3_4.setExtended(true, new NullProgressMonitor());
		createRefinesEventClause(evt1_3_4, "evt1_2_4");
		createParameter(evt1_3_4, "r");
		grd1_3_4_1 = createGuard(evt1_3_4, "grd1_3_4_1", "r ∈ ℕ");
		grd1_3_4_2 = createGuard(evt1_3_4, "grd1_3_4_2", "p(r) = g");
		act1_3_4_1 = createAction(evt1_3_4, "act1_3_4_1", "p(r) ≔ h");
		
		// evt1_3_5
		// STATUS ordinary
		// WHEN
		//   grd1_3_5_1: z >= 2
		// THEN
		//   act1_3_5_1: z := z - 1
		// END
		evt1_3_5 = createEvent(mch1_3, "evt1_3_5");
		grd1_3_5_1 = createGuard(evt1_3_5, "grd1_3_5_1", "z ≥ 2");
		act1_3_5_1 = createAction(evt1_3_5, "act1_3_5_1", "z ≔ z − 1");
		
		// Save mch1_3
		mch1_3.getRodinFile().save(new NullProgressMonitor(), false);
	}

}
