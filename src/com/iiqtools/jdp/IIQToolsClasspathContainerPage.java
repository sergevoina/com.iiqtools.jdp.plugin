package com.iiqtools.jdp;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A very simple configuration page to make the IIQ Tools Classpath Container
 * show up in the libraries to add by the user. The page is actually empty,
 * besides some text, since there's nothing to configure anyway!
 * 
 * 
 */
public class IIQToolsClasspathContainerPage extends WizardPage implements IClasspathContainerPage {

	private String selectedDir = null;
	private Text txtPath;
	private Button btnBrowse;

	/**
	 * Default constructor for instantiation in Eclipse.
	 */
	public IIQToolsClasspathContainerPage() {
		super(Messages.PageName, Messages.PageTitle, null);
		setDescription(Messages.PageDesc);
		setPageComplete(true);
	}

	// @Override
	// public IClasspathEntry getSelection() {
	// return JavaCore.newContainerEntry(new
	// Path(IIQToolsClasspathContainerInitializer.PATH));
	// }

	@Override
	public boolean finish() {
		String dir = getSelectedDir();
		if (dir != null && dir.length() > 0) {
			if (!new File(dir).isDirectory()) {
				String msg = NLS.bind(Messages.DirErr, dir);
				setErrorMessage(msg);
				return false;
			}
		}
		return true;
	}

	@Override
	public IClasspathEntry getSelection() {

		IPath containerPath = new Path(IIQToolsClasspathContainerInitializer.PLUGIN_CONTAINER_ID);

		String dirPath = getSelectedDir();
		if ((dirPath != null) && (dirPath.length() > 0)) {

			String os = Platform.getOS();
			if (Platform.OS_WIN32.equals(os)) {
				if ((dirPath.length() > 1) && dirPath.charAt(1) == ':') {
					StringBuilder sb = new StringBuilder();
					sb.append(dirPath.charAt(0)).append("/");

					if (dirPath.length() > 2) {
						sb.append(dirPath.substring(2));
					}
					dirPath = sb.toString();
				}
			}

			containerPath = containerPath.append(dirPath);
		}

		return JavaCore.newContainerEntry(containerPath);
	}

	@Override
	public void setSelection(IClasspathEntry containerEntry) {
		if (containerEntry != null) {
			this.selectedDir = IIQToolsClasspathContainer.getSelectedDirectory(containerEntry.getPath());
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		// composite.setLayout(new GridLayout());
		// composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL |
		// GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());

		composite.setLayout(new GridLayout(2, false));

		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText(Messages.DirLabel);

		this.txtPath = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.txtPath.setLayoutData(gridData);
		this.txtPath.setText(this.selectedDir != null ? this.selectedDir : "");

		btnBrowse = new Button(composite, SWT.NONE);
		btnBrowse.setText(Messages.Browse);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		btnBrowse.setLayoutData(gridData);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onBrowse();
			}
		});

		super.setControl(composite);
	}

	/**
	 * Creates a directory dialog
	 */
	protected void onBrowse() {
		DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(), SWT.SAVE);
		dialog.setMessage(Messages.DirSelect);
		dialog.setFilterPath(this.txtPath.getText());
		String dir = dialog.open();
		if (dir != null) {
			txtPath.setText(dir);
		}
	}

	/**
	 * @return the current extension list
	 */
	protected String getSelectedDir() {
		return txtPath.getText().trim().toLowerCase();
	}

}
