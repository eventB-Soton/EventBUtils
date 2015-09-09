/*******************************************************************************
 * Copyright (c) 2009 ETH Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.internal.utils;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.ethz.eventb.internal.utils.messages"; //$NON-NLS-1$


	
	// Progress monitor strings
	public static String progress_CreateEventBProject;
	public static String progress_CreateProject;
	public static String progress_OpenProject;
	public static String progress_SetRodinProjectNature;
	
	public static String progress_CreateContext;
	public static String progress_CreateContextFile;
	public static String progress_GetFreeComponentName;
	public static String progress_SetDefaultConfiguration;
	public static String progress_CreateMachine;
	public static String progress_CreateMachineFile;
	
	public static String progress_CreateExtendsContextClause;
	public static String progress_CreateExtendsContextElement;
	public static String progress_SetAbstractContextName;
	
	public static String progress_CreateCarrierSet;
	public static String progress_CreateCarrierSetElement;
	public static String progress_SetCarrierSetIdentifierString;

	public static String progress_CreateConstant;
	public static String progress_CreateConstantElement;
	public static String progress_SetConstantIdentifierString;

	public static String progress_CreateAxiom;
	public static String progress_CreateAxiomElement;
	public static String progress_SetAxiomLabel;
	public static String progress_SetAxiomPredicateString;
	public static String progress_SetAxiomIsTheorem;
	
	public static String progress_CreateRefinesMachineClause;
	public static String progress_CreateRefinesMachineElement;
	public static String progress_SetRefinesMachineAbstractMachineName;
	
	public static String progress_CreateSeesContextClause;
	public static String progress_CreateSeesContextElement;
	public static String progress_SetSeenContextName;
	
	public static String progress_CreateVariable;
	public static String progress_CreateVariableElement;
	public static String progress_SetVariableIdentifierString;

	public static String progress_CreateInvariant;
	public static String progress_CreateInvariantElement;
	public static String progress_SetInvariantLabel;
	public static String progress_SetInvariantPredicateString;
	public static String progress_SetInvariantIsTheorem;

	public static String progress_CreateEvent;
	public static String progress_CreateEventElement;
	public static String progress_SetEventLabel;
	public static String progress_SetEventConvergence;
	public static String progress_SetEventExtended;

	public static String progress_CreateRefinesEventClause;
	public static String progress_CreateRefinesEventElement;
	public static String progress_SetAbstractEventLabel;

	public static String progress_CreateParameter;
	public static String progress_CreateParameterElement;
	public static String progress_SetParameterIdentifierString;

	public static String progress_CreateGuard;
	public static String progress_CreateGuardElement;
	public static String progress_SetGuardLabel;
	public static String progress_SetGuardPredicateString;
	public static String progress_SetGuardIsTheorem;
	
	public static String progress_CreateWitness;
	public static String progress_CreateWitnessElement;
	public static String progress_SetWitnessLabel;
	public static String progress_SetWitnessPredicateString;

	public static String progress_CreateAction;
	public static String progress_CreateActionElement;
	public static String progress_SetActionLabel;
	public static String progress_SetActionAssignmentString;


	// Error strings
	public static String error_NullProject;
	public static String error_NonExistingProject;

	public static String error_NullContext;
	public static String error_ExistingContext;
	public static String error_NonExistingContext;

	public static String error_NullMachine;
	public static String error_ExistingMachine;
	public static String error_NonExistingMachine;

	public static String error_NullEvent;
	public static String error_NonExistingEvent;

	public static String error_NullSCMachine;
	public static String error_NonExistingSCMachine;

	public static String error_NotAnAxiom;




	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
