/*******************************************************************************
 * Copyright (c) 2010,2020 ETH Zurich.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/

package ch.ethz.eventb.utils.tests;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.IAction;
import org.eventb.core.IAxiom;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IExtendsContext;
import org.eventb.core.IGuard;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IParameter;
import org.eventb.core.IRefinesEvent;
import org.eventb.core.IRefinesMachine;
import org.eventb.core.ISeesContext;
import org.eventb.core.IVariable;
import org.eventb.core.IVariant;
import org.eventb.core.IWitness;
import org.eventb.core.ast.FormulaFactory;
import org.junit.After;
import org.junit.Before;
import org.rodinp.core.RodinDBException;
import org.rodinp.internal.core.debug.DebugHelpers;

import ch.ethz.eventb.utils.EventBUtils;

/**
 * <p>
 * Abstract class for testing Event-B models.
 * </p>
 *
 * @author htson
 * @version 0.1.3
 * @see EventBUtils
 * @since 0.1.3
 */
@SuppressWarnings("restriction")
public abstract class AbstractEventBTests extends AbstractTests {

	/**
	 * The null progress monitor for testing.
	 */
	public IProgressMonitor nullMonitor = new NullProgressMonitor();

	/**
	 * The testing workspace.
	 */
	public IWorkspace workspace = ResourcesPlugin.getWorkspace();

	/**
	 * The formula factory used to create formulae.
	 */
	protected static final FormulaFactory ff = FormulaFactory.getDefault();

	/**
	 * Constructor: Create a test case.
	 */
	public AbstractEventBTests() {
		super();
	}

	/**
	 * Constructor: Create a test case with the given name.
	 * 
	 * @param name
	 *            the name of test
	 */
	public AbstractEventBTests(String name) {
		super(name);
	}

	/**
	 * <ol>
	 * <li>Super method.</li>
	 * <li>Turn off auto-building.</li>
	 * <li>Disable Rodin indexing.</li>
	 * <li>Delete the old workspace.</li>
	 * </ol>
	 * 
	 * @see AbstractEventBTests#setUp()
	 */
	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// ensure autobuilding is turned off
		IWorkspaceDescription wsDescription = workspace.getDescription();
		if (wsDescription.isAutoBuilding()) {
			wsDescription.setAutoBuilding(false);
			workspace.setDescription(wsDescription);
		}

		// disable indexing
		DebugHelpers.disableIndexing();

		// Delete the old workspace
		workspace.getRoot().delete(true, null);

	}

	/**
	 * <ol>
	 * <li>Delete the old workspace.</li>
	 * <li>Super method.</li>
	 * </ol>
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	@Override
	protected void tearDown() throws Exception {
		workspace.getRoot().delete(true, null);
		super.tearDown();
	}

	// =========================================================================
	// Utility methods for testing various Event-B elements.
	// =========================================================================

	/**
	 * Utility method for testing EXTENDS clauses of a context.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param ctx
	 *            A context root whose EXTENDS clauses will be tested.
	 * @param expected
	 *            the array of expected EXTENDS clauses. Each clause is
	 *            represented by the abstract context name. The order of the
	 *            EXTENDS clause is important.
	 */
	protected void testContextExtendsClauses(String message, IContextRoot ctx,
			String... expected) {
		try {
			IExtendsContext[] extendsCtxs = ctx.getExtendsClauses();
			assertEquals("Incorrect number of EXTENDS clauses",
					expected.length, extendsCtxs.length);
			for (int i = 0; i < expected.length; i++) {
				testExtendsClause(message, extendsCtxs[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing an EXTEND clause.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param extendCtx
	 *            the EXTEND clause under test.
	 * @param expected
	 *            the expected abstract context name.
	 */
	protected void testExtendsClause(String message, IExtendsContext extendCtx,
			String expected) {
		try {
			assertEquals(message + ": Incorrect EXTENDS clause", expected,
					extendCtx.getAbstractContextName());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the carrier sets of a context.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param ctx
	 *            a context whose carrier sets will be tested.
	 * @param expected
	 *            an array of expected carrier sets. Each carrier set is
	 *            represented by its identifier. The order of the carrier sets
	 *            is important.
	 */
	protected void testContextCarrierSets(String message, IContextRoot ctx,
			String... expected) {
		try {
			ICarrierSet[] sets = ctx.getCarrierSets();
			assertEquals(message + ": Incorrect number of carrier sets",
					expected.length, sets.length);
			for (int i = 0; i < expected.length; i++) {
				testCarrierSet(message, sets[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing a carrier set.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param set
	 *            the carrier set under test.
	 * @param expected
	 *            the expected identifier of the carrier set.
	 */
	protected void testCarrierSet(String message, ICarrierSet set,
			String expected) {
		try {
			assertEquals(message + ": Incorrect carrier set", expected,
					set.getIdentifierString());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the constants of a context.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param ctx
	 *            a context whose constants will be tested.
	 * @param expected
	 *            an array of expected constants. Each constant is represented
	 *            by its identifier. The order of the constants is important.
	 */
	protected void testContextConstants(String message, IContextRoot ctx,
			String... expected) {
		try {
			IConstant[] csts = ctx.getConstants();
			assertEquals(message + ": Incorrect number of constants",
					expected.length, csts.length);
			for (int i = 0; i < expected.length; i++) {
				testConstant(message, csts[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing a constant.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param set
	 *            the constant under test.
	 * @param expected
	 *            the expected identifier of the constant.
	 */
	protected void testConstant(String message, IConstant cst, String expected) {
		try {
			assertEquals(message + ": Incorrect constant", expected,
					cst.getIdentifierString());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the axioms of a context.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param ctx
	 *            a context root whose axioms will be tested.
	 * @param expected
	 *            the expected pretty-print axioms. The axioms are
	 *            "pretty-printed" as follows:
	 *            "label:predicateString:isTheorem". The order of the axioms is
	 *            important.
	 */
	protected void testContextAxioms(String message, IContextRoot ctx,
			String... expected) {
		try {
			IAxiom[] axioms = ctx.getAxioms();
			assertEquals(message + ": Incorrect number of axioms",
					expected.length, axioms.length);
			for (int i = 0; i < expected.length; i++) {
				testAxiom(message, axioms[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing an axiom.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param axiom
	 *            the axiom under test.
	 * @param expected
	 *            the expected pretty print axiom. The axiom is "pretty-printed"
	 *            as follows: "label:predicateString:isTheorem".
	 */
	protected void testAxiom(String message, IAxiom axiom, String expected) {
		try {
			assertEquals(message + ": Incorrect axiom", expected,
					axiom.getLabel() + ":" + axiom.getPredicateString() + ":"
							+ axiom.isTheorem());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the REFINES clauses of a machine.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param mch
	 *            a machine root whose REFINES clauses will be tested.
	 * @param expected
	 *            an array of expected REFINES clause. Each REFINES clause is
	 *            represented by its abstract machine name. The order of the
	 *            REFINES clauses is important.
	 */
	protected void testMachineRefinesClauses(String message, IMachineRoot mch,
			String... expected) {
		try {
			IRefinesMachine[] refinesClauses = mch.getRefinesClauses();
			assertEquals(message + ": Incorrect number of REFINES clauses",
					expected.length, refinesClauses.length);
			for (int i = 0; i < expected.length; i++) {
				testRefinesClause(message, refinesClauses[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing a REFINES (machine) clause.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param seesClause
	 *            the REFINES (machine) clause under test.
	 * @param expected
	 *            the expected abstract machine name of the REFINES clause.
	 */
	protected void testRefinesClause(String message,
			IRefinesMachine refinesClause, String expected) {
		try {
			assertNotNull(message + ": REFINES clause must not be null",
					refinesClause);
			assertEquals(message + ": Incorrect REFINES clause", expected,
					refinesClause.getAbstractMachineName());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the SEES clauses of a machine.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param mch
	 *            a machine root whose SEES clauses will be tested.
	 * @param expected
	 *            an array of expected SEES clause. Each SEES clause is
	 *            represented by its seen context name. The order of the SEES
	 *            clauses is important.
	 */
	protected void testMachineSeesClauses(String message, IMachineRoot mch,
			String... expected) {
		try {
			ISeesContext[] seesClauses = mch.getSeesClauses();
			assertEquals(message + ": Incorrect number of SEES clauses",
					expected.length, seesClauses.length);
			for (int i = 0; i < expected.length; i++) {
				testSeesClause(message, seesClauses[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing a SEES clause.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param seesClause
	 *            the SEES clause under test.
	 * @param expected
	 *            the expected seen context name of the SEES clause.
	 */
	protected void testSeesClause(String message, ISeesContext seesClause,
			String expected) {
		try {
			assertEquals(message + ": Incorrect SEES clause", expected,
					seesClause.getSeenContextName());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the variables of a machine.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param mch
	 *            the machine root whose variables will be tested.
	 * @param expected
	 *            an array of expected variables. Each variable is represented
	 *            by its identifier. The order of the variables is important.
	 */
	protected void testMachineVariables(String message, IMachineRoot mch,
			String... expected) {
		try {
			IVariable[] vars = mch.getVariables();
			assertEquals(message + ": Incorrect number of variables",
					expected.length, vars.length);
			for (int i = 0; i < expected.length; i++) {
				testVariable(message, vars[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the variables of a machine.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param mch
	 *            the machine root whose variables will be tested.
	 * @param expected
	 *            an array of expected variables. Each variable is represented
	 *            by its identifier. The order of the variables is NOT
	 *            important.
	 */
	protected void testMachineVariablesUnordered(String message,
			IMachineRoot mch, String... expected) {
		try {
			IVariable[] vars = mch.getVariables();
			assertEquals(message + ": Incorrect number of variables",
					expected.length, vars.length);
			for (int i = 0; i < expected.length; i++) {
				boolean b = false;
				for (int j = 0; j < vars.length; j++) {
					if (vars[j].getIdentifierString().equals(expected[i])) {
						b = true;
						break;
					}
				}
				if (!b) {
					fail("Variable " + expected[i] + " cannot be found");
				}
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing a variable.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param var
	 *            the variable under test.
	 * @param expected
	 *            the expected identifier of the variable.
	 */
	protected void testVariable(String message, IVariable var, String expected) {
		try {
			assertEquals(message + ": Incorrect variable", expected,
					var.getIdentifierString());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the invariants of a machine.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param mch
	 *            a context root whose invariants will be tested.
	 * @param expected
	 *            the expected pretty-print invariants. The invariants are
	 *            "pretty-printed" as follows:
	 *            "label:predicateString:isTheorem". The order of the invariants
	 *            is important.
	 */
	protected void testMachineInvariants(String message, IMachineRoot mch,
			String... expected) {
		try {
			IInvariant[] invs = mch.getInvariants();
			assertEquals(message + ": Incorrect number of invariants",
					expected.length, invs.length);
			for (int i = 0; i < expected.length; i++) {
				testInvariant(message, invs[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing an invariant.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param inv
	 *            the invariant under test.
	 * @param expected
	 *            the expected pretty-print invariant. The invariant is
	 *            "pretty-printed" as follows:
	 *            "label:predicateString:isTheorem".
	 */
	protected void testInvariant(String message, IInvariant inv, String expected) {
		try {
			assertEquals(
					message + ": Incorrect invariant",
					expected,
					inv.getLabel() + ":" + inv.getPredicateString() + ":"
							+ inv.isTheorem());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the variants of a machine.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param mch
	 *            a machine root whose variants will be tested.
	 * @param expected
	 *            the expected pretty-print variants. The variants are
	 *            "pretty-printed" as follows: "expressionString". The order of
	 *            the variants is important.
	 */
	protected void testMachineVariants(String message, IMachineRoot mch,
			String... expected) {
		try {
			IVariant[] variants = mch.getVariants();
			assertEquals(message + ": Incorrect number of variants",
					variants.length, expected.length);
			for (int i = 0; i < expected.length; i++) {
				testVariant(message, variants[i], expected[i]);
			}
		} catch (RodinDBException e) {
			failUnexpectedException(e);
		}

	}

	/**
	 * Utility method for testing a variant.
	 * 
	 * @param message
	 *            a message for debugging
	 * @param var
	 *            the variant to be tested
	 * @param expected
	 *            the expected pretty-print variant. The variant are
	 *            "pretty-printed" as follows: "expressionString".
	 */
	protected void testVariant(String message, IVariant var, String expected) {
		try {
			assertEquals(message + ": Incorrect expression string", expected,
					var.getExpressionString());
		} catch (RodinDBException e) {
			failUnexpectedException(e);
		}
	}

	/**
	 * Utility method for testing the events of a machine.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param mch
	 *            a machine root whose events will be tested.
	 * @param expected
	 *            the expected pretty-print events (only the signature). The
	 *            events are "pretty-printed" as follows:
	 *            "label:convergent:isExtended". The order of the events is
	 *            important.
	 */
	protected void testMachineEvents(String message, IMachineRoot mch,
			String... expected) {
		try {
			IEvent[] evts = mch.getEvents();
			assertEquals(message + ": Incorrect number of events",
					expected.length, evts.length);
			for (int i = 0; i < expected.length; i++) {
				testEvent(message, evts[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing an event.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param evt
	 *            the event under test.
	 * @param expected
	 *            the expected pretty-print event (only the signature). The
	 *            event is "pretty-printed" as follows:
	 *            "label:convergent:isExtended".
	 */
	protected void testEvent(String message, IEvent evt, String expected) {
		try {
			assertNotNull(message + ": The event must not be null", evt);
			assertEquals(
					message + ": Incorrect event",
					expected,
					evt.getLabel() + ":" + evt.getConvergence() + ":"
							+ evt.isExtended());
		} catch (CoreException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the REFINES clauses of an event.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param mch
	 *            an event whose REFINES clauses will be tested.
	 * @param expected
	 *            an array of expected REFINES clause. Each REFINES clause is
	 *            represented by its abstract event name. The order of the
	 *            REFINES clauses is important.
	 */
	protected void testEventRefinesClauses(String message, IEvent evt,
			String... expected) {
		try {
			IRefinesEvent[] refinesClauses = evt.getRefinesClauses();
			assertEquals(message + ": Incorrect number of REFINES clauses",
					expected.length, refinesClauses.length);
			for (int i = 0; i < expected.length; i++) {
				testRefinesClause(message, refinesClauses[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing a REFINES (event) clause.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param seesClause
	 *            the REFINES (event) clause under test.
	 * @param expected
	 *            the expected abstract event name of the REFINES clause.
	 */
	protected void testRefinesClause(String message,
			IRefinesEvent refinesEvent, String expected) {
		try {
			assertEquals(message + "Incorrect REFINES clause", expected,
					refinesEvent.getAbstractEventLabel());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the parameters of an event.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param evt
	 *            an event whose parameters will be tested.
	 * @param expected
	 *            the expected set of parameters. Each parameter is represented
	 *            by its identifier. The order of the parameters is important.
	 */
	protected void testEventParameters(String message, IEvent evt,
			String... expected) {
		try {
			IParameter[] params = evt.getParameters();
			assertEquals(message + ": Incorrect number of parameters",
					expected.length, params.length);
			for (int i = 0; i < expected.length; i++) {
				testParameter(message, params[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing a parameter.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param par
	 *            the parameter under test.
	 * @param expected
	 *            the expected parameter identifier.
	 */
	protected void testParameter(String message, IParameter par, String expected) {
		try {
			assertEquals(message + ": Incorrect parameter", expected,
					par.getIdentifierString());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the guards of an event.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param evt
	 *            an event whose guards will be tested.
	 * @param expected
	 *            the expected pretty-print guards. The guards are
	 *            "pretty-printed" as follows:
	 *            "label:predicateString:isTheorem". The order of the guards is
	 *            important.
	 */
	protected void testEventGuards(String message, IEvent evt,
			String... expected) {
		try {
			IGuard[] grds = evt.getGuards();
			assertEquals(message + ": Incorrect number of guards",
					expected.length, grds.length);
			for (int i = 0; i < grds.length; i++) {
				testGuard(message, grds[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing a guard.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param grd
	 *            the guard under test.
	 * @param expected
	 *            the expected pretty-print guard. The guard is "pretty-printed"
	 *            as follows: "label:predicateString:isTheorem".
	 */
	protected void testGuard(String message, IGuard grd, String expected) {
		try {
			assertEquals(
					message + ": Incorrect guard",
					expected,
					grd.getLabel() + ":" + grd.getPredicateString() + ":"
							+ grd.isTheorem());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the witnesses of an event.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param evt
	 *            an event whose witnesses will be tested.
	 * @param expected
	 *            the expected pretty-print witnesses. The witnesses are
	 *            "pretty-printed" as follows: "label:predicateString". The
	 *            order of the witnesses is important.
	 */
	protected void testEventWitnesses(String message, IEvent evt,
			String... expected) {
		try {
			IWitness[] wits = evt.getWitnesses();
			assertEquals(message + ": Incorrect number of witnesses",
					expected.length, wits.length);
			for (int i = 0; i < expected.length; i++) {
				testWitness(message, wits[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing an witness.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param wit
	 *            the witness under test.
	 * @param expected
	 *            the expected pretty-print witness. The witness is
	 *            "pretty-printed" as follows: "label:predicateString".
	 */
	protected void testWitness(String message, IWitness wit, String expected) {
		try {
			assertEquals(message + ": Incorrect witness", expected,
					wit.getLabel() + ":" + wit.getPredicateString());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing the actions of an event.
	 * 
	 * @param message
	 *            a message for debugging.
	 * @param evt
	 *            an event whose actions will be tested.
	 * @param expected
	 *            expected pretty-print actions. The actions are
	 *            "pretty-printed" as follows: "label:assignmentString". The
	 *            order of the actions is important.
	 */
	protected void testEventActions(String message, IEvent evt,
			String... expected) {
		try {
			IAction[] acts = evt.getActions();
			assertEquals(message + ": Incorrect number of actions",
					expected.length, acts.length);
			for (int i = 0; i < expected.length; i++) {
				testAction(message, acts[i], expected[i]);
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

	/**
	 * Utility method for testing an action.
	 * 
	 * @param message
	 *            a message
	 * @param act
	 *            the action under test
	 * @param expected
	 *            expected pretty-print action. The action is "pretty-printed"
	 *            as follows: "label:assignmentString".
	 */
	protected void testAction(String message, IAction act, String expected) {
		try {
			assertEquals(message + ": Incorrect action", expected,
					act.getLabel() + ":" + act.getAssignmentString());
		} catch (RodinDBException e) {
			e.printStackTrace();
			fail("There should be no exception");
			return;
		}
	}

}
