package com.iiqtools.jdp;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SettingsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IntegerFieldEditor ctText;
	private StringFieldEditor tpText;

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		Composite entryTable = new Composite(parent, SWT.NULL);
		// Add in dummy labels for spacing
		new Label(entryTable, SWT.NONE);
		new Label(entryTable, SWT.NONE);

		tpText = new StringFieldEditor("targetProperties", "Target Properties", entryTable);
		tpText.setStringValue(IIQToolsPlugin.getDefault().getTargetPropertiesPreference());

		ctText = new IntegerFieldEditor("connectionTimeout", "Connection Timeout (sec)", entryTable);
		ctText.setStringValue(String.valueOf(IIQToolsPlugin.getDefault().getConnectionTimeoutPreference()));

		return entryTable;
	}

	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// Initialize the preference store we wish to use
		setPreferenceStore(IIQToolsPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Performs special processing when this page's Restore Defaults button has been
	 * pressed. Sets the contents of the nameEntry field to be the default
	 */
	@Override
	protected void performDefaults() {
		tpText.setStringValue(IIQToolsPlugin.getDefault().getDefaultTargetPropertiesPreference());
		ctText.setStringValue(String.valueOf(IIQToolsPlugin.getDefault().getDefaultConnectionTimeoutPreference()));
	}

	/**
	 * Method declared on IPreferencePage. Save the author name to the preference
	 * store.
	 */
	public boolean performOk() {
		IIQToolsPlugin.getDefault().setTargetPropertiesPreference(tpText.getStringValue());
		IIQToolsPlugin.getDefault().setConnectionTimeoutPreference(ctText.getIntValue());

		return super.performOk();
	}
}
