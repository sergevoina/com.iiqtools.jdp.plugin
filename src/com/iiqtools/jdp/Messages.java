package com.iiqtools.jdp;

import org.eclipse.osgi.util.NLS;

/**
 * Plugin messages container
 * 
 * @author Serge Voina
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.iiqtools.jdp.messages";

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String classpathContainerDescription;

	public static String directoryPageName;
	public static String directoryPageDesc;
	public static String directoryPageTitle;
	public static String directoryPageBrowse;
	public static String directoryPageLabel;
	public static String directoryPageError;
	public static String directoryDialogMessage;

	public static String messageDialogTitle;

	public static String hasUnsavedChangesError;
	public static String hasJavaProblemsError;
	public static String selectionIsNotSupportedError;
	public static String cannotFindTopLevelTypeError;
	public static String cannotFindArtefactAnnotationError;
	
	public static String basicStructuralValidation;
}