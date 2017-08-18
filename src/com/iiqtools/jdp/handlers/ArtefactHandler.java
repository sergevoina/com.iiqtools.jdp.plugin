package com.iiqtools.jdp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

import com.iiqtools.jdp.Messages;
import com.iiqtools.jdp.util.ArtefactUtil;
import com.iiqtools.jdp.util.BshScriptBuilder;
import com.iiqtools.jdp.util.JdtUtil;

/**
 * The handler that generates BeanShell script from a Java class
 * 
 * @author Serge Voina
 *
 */
public class ArtefactHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		try {
			boolean handled = false;

			ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
			if (currentSelection instanceof ITreeSelection) {
				handled = handleTreeSelection(event, (ITreeSelection) currentSelection);
			} else if (currentSelection instanceof ITextSelection) {
				IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
				if (activeEditor != null) {
					IJavaElement javaElement = JavaUI.getEditorInputJavaElement(activeEditor.getEditorInput());
					if (javaElement instanceof ICompilationUnit) {
						handled = handleCompilationUnit(event, (ICompilationUnit) javaElement);
					}
				}
			}

			if (!handled) {
				MessageDialog.openInformation(window.getShell(), Messages.messageDialogTitle,
						Messages.selectionIsNotSupportedError);
			}
		} catch (Exception e) {
			MessageDialog.openError(window.getShell(), Messages.messageDialogTitle, e.getMessage());
		}
		return null;
	}

	protected boolean handleTreeSelection(ExecutionEvent event, ITreeSelection currentSelection) throws Exception {
		boolean handled = false;

		TreePath[] paths = ((ITreeSelection) currentSelection).getPaths();
		if (paths != null) {
			for (TreePath path : paths) {
				Object lastSegment = path.getLastSegment();
				if (lastSegment instanceof ICompilationUnit) {
					handled = handleCompilationUnit(event, (ICompilationUnit) lastSegment);
				}
			}
			// TODO: package
			// TODO: project
		}

		return handled;
	}

	protected boolean handleCompilationUnit(ExecutionEvent event, ICompilationUnit compilationUnit) throws Exception {
		BshScriptBuilder bshScript = BshScriptBuilder.parse(compilationUnit);

		if (!bshScript.isValid()) {
			throw new Exception(bshScript.getErrorMessage());
		}

		ArtefactUtil.updateArtefact(bshScript.getTargetFile(), bshScript.getScript(), bshScript.getArtefactInfo());

		MessageConsole console = JdtUtil.getConsole("IIQ Tools");
		if (console != null) {
			MessageConsoleStream out = console.newMessageStream();
			out.println("Successfully updated IIQ Artefact: " + bshScript.getArtefactInfo().target);
			out.flush();
			out.close();
		}

		return true;
	}
}
