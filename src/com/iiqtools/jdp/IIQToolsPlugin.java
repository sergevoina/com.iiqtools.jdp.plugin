package com.iiqtools.jdp;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class IIQToolsPlugin extends AbstractUIPlugin {
	// The shared instance.
	private static IIQToolsPlugin plugin;

	// The identifiers for the preferences
	public static final String TARGET_PROPERTIES_PREFERENCE = "target-properties";
	public static final String CONNECTION_TIMEOUT_PREFERENCE = "connection-timeout";

	// The default values for the preferences
	public static final String DEFAULT_TARGET_PROPERTIES = "sandbox.target.properties";
	public static final int DEFAULT_CONNECTION_TIMEOUT = 10;

	public IIQToolsPlugin() {
		plugin = this;
	}

	public static IIQToolsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Initializes a preference store with default preference values for this
	 * plug-in.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(TARGET_PROPERTIES_PREFERENCE, DEFAULT_TARGET_PROPERTIES);
		store.setDefault(CONNECTION_TIMEOUT_PREFERENCE, DEFAULT_CONNECTION_TIMEOUT);
	}

	/**
	 * Return the target properties preference default
	 */
	public String getDefaultTargetPropertiesPreference() {
		return getPreferenceStore().getDefaultString(TARGET_PROPERTIES_PREFERENCE);
	}

	/**
	 * Return the target properties preference
	 */
	public String getTargetPropertiesPreference() {
		return getPreferenceStore().getString(TARGET_PROPERTIES_PREFERENCE);
	}

	/**
	 * Set the target properties preference
	 */
	public void setTargetPropertiesPreference(String fileName) {
		getPreferenceStore().setValue(TARGET_PROPERTIES_PREFERENCE, fileName);
	}

	/**
	 * Return the connection timeout preference default
	 */
	public int getDefaultConnectionTimeoutPreference() {
		return getPreferenceStore().getDefaultInt(CONNECTION_TIMEOUT_PREFERENCE);
	}

	/**
	 * Return the connection timeout preference
	 */
	public int getConnectionTimeoutPreference() {
		return getPreferenceStore().getInt(CONNECTION_TIMEOUT_PREFERENCE);
	}

	/**
	 * Set the connection timeout preference
	 */
	public void setConnectionTimeoutPreference(int timeout) {
		getPreferenceStore().setValue(CONNECTION_TIMEOUT_PREFERENCE, timeout);
	}
}
