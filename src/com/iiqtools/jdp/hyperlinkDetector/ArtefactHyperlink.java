package com.iiqtools.jdp.hyperlinkDetector;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.iiqtools.jdp.Messages;
import com.iiqtools.jdp.util.ArtefactUtil;

public class ArtefactHyperlink implements IHyperlink {
	private IRegion targetRegion;
	private IFile targetFile;
	private final String xpath;

	public ArtefactHyperlink(IRegion targetRegion, IFile targetFile, String xpath) {
		this.targetRegion = targetRegion;
		this.targetFile = targetFile;
		this.xpath = xpath;
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
		if (targetFile != null) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window != null) {
					if (!targetFile.exists()) {
						MessageDialog.openInformation(window.getShell(), Messages.messageDialogTitle,
								"The target artefact doesn't exist");
					} else {
						try {
							IEditorPart editorPart = IDE.openEditor(window.getActivePage(), targetFile, true);

							if (editorPart instanceof ITextEditor) {
								handleTextEditor((ITextEditor) editorPart, targetFile);
							} else if (editorPart instanceof MultiPageEditorPart) {
								MultiPageEditorPart multiPagePart = (MultiPageEditorPart) editorPart;
								IEditorPart[] parts = multiPagePart.findEditors(editorPart.getEditorInput());

								if (parts != null) {
									for (IEditorPart part : parts) {
										if (part instanceof ITextEditor) {
											handleTextEditor((ITextEditor) part, targetFile);
										}
									}
								}
							}
						} catch (Exception e) {
						}
					}
				}
			}
		}
	}

	private void handleTextEditor(ITextEditor textEditor, IFile file) throws Exception {
		IRegion region = ArtefactUtil.getNodePosition(file, xpath);
		if (region != null) {
			textEditor.selectAndReveal(region.getOffset(), region.getLength());
		}
	}
}
