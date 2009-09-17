package ch.ethz.eventb.internal.decomposition.utils;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.ethz.eventb.internal.decomposition.utils.messages"; //$NON-NLS-1$
	
	public static String button_add;
	public static String button_remove;
	public static String button_edit;
	
	public static String label_available;
	public static String label_selected;
	public static String label_project;
	public static String label_machine;
	public static String label_style;

	public static String wizard_name;
	public static String wizard_description;
	public static String wizard_title;
	public static String wizard_machine;
	public static String wizard_elements;
	public static String wizard_project;
	public static String wizard_error;
	public static String wizard_style;

	public static String decomposition_forbidden;
	public static String decomposition_defaultproject;
	public static String decomposition_description;
	public static String decomposition_cleanup;
	public static String decomposition_contexts;
	public static String decomposition_seesclauses;
	public static String decomposition_external;
	public static String decomposition_external_comment;
	public static String decomposition_internal_comment;
	public static String decomposition_invariants;
	public static String decomposition_machine;
	public static String decomposition_project;
	public static String decomposition_submodel;
	public static String decomposition_private_comment;
	public static String decomposition_shared_comment;
	public static String decomposition_typing;
	public static String decomposition_typingtheorems;
	public static String decomposition_variable;
	public static String decomposition_variables;
	public static String decomposition_error_existingmachine;
	public static String decomposition_error_existingproject;
	public static String decomposition_astyle;
	public static String decomposition_bstyle;
	
	public static String attributeManipulation_external_true;
	public static String attributeManipulation_external_false;
	public static String attributeManipulation_nature_private;
	public static String attributeManipulation_nature_shared;
	
	public static String database_VariableInvalidNatureFailure;
	public static String scuser_VariableInvalidNatureError;
	public static String scuser_VariableHasDisappearedError;
	public static String scuser_EventInvalidStatusError;
	public static String scuser_EventHasDisappearedError;
	public static String scuser_ParametersInExternalEventError;
	public static String scuser_GuardsInExternalEventError;
	public static String scuser_ActionsInExternalEventError;
	public static String scuser_ActionOnPrivateAndSharedError;
	public static String scuser_ActionOnSharedError;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
