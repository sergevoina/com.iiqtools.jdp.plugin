package com.iiqtools.jdp.hyperlinkDetector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.iiqtools.jdp.Messages;

public class ArtefactHyperlink implements IHyperlink {
	private IRegion targetRegion;
	private IPath targetPath;

	public ArtefactHyperlink(IRegion targetRegion, IPath targetPath, String xpath) {
		this.targetRegion = targetRegion;
		this.targetPath = targetPath;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return targetRegion;
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		return null;
	}

	@Override
	public void open() {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(targetPath);
		if (file != null) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window != null) {
					if (!file.exists()) {
						MessageDialog.openInformation(window.getShell(), Messages.messageDialogTitle,
								"The target artefact doesn't exist");
					} else {
						try {
							IDE.openEditor(window.getActivePage(), file, true);
						} catch (PartInitException e) {
						}
					}
				}
			}
		}
	}
}
