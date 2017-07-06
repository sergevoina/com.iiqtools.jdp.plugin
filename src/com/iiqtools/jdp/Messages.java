package com.iiqtools.jdp;

import org.eclipse.osgi.util.NLS;

/**
 * Bundle messages container
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.iiqtools.jdp.messages";

	public static String PageName;

	public static String PageDesc;

	public static String PageTitle;

	public static String Browse;

	public static String DirErr;

	public static String DirLabel;

	public static String DirSelect;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}