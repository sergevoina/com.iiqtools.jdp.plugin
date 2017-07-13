package com.iiqtools.jdp.handlers;

import java.util.Date;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
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
import com.iiqtools.jdp.util.ArtefactInfo;
import com.iiqtools.jdp.util.ArtefactUtil;
import com.iiqtools.jdp.util.JdtUtil;
import com.iiqtools.jdp.util.PluginUtil;

/**
 * The handler that generates BeanShell script from a Java class
 * 
 * @author Serge Voina
 *
 */
public class JdpArtefactHandler extends AbstractHandler {

	final String lineSeparator;

	public JdpArtefactHandler() {
		this.lineSeparator = "\n";
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		try {
			ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
			if (currentSelection instanceof ITreeSelection) {
				handleTreeSelection(event, (ITreeSelection) currentSelection);
			} else if (currentSelection instanceof ITextSelection) {
				IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
				if (activeEditor != null) {
					IJavaElement javaElement = JavaUI.getEditorInputJavaElement(activeEditor.getEditorInput());
					if (javaElement instanceof ICompilationUnit) {
						handleCompilationUnit(event, (ICompilationUnit) javaElement);
					}
				}
			}
		} catch (Exception e) {
			MessageDialog.openInformation(window.getShell(), Messages.messageDialogTitle, e.getMessage());
		}
		return null;
	}

	private void handleTreeSelection(ExecutionEvent event, ITreeSelection currentSelection) throws Exception {
		TreePath[] paths = ((ITreeSelection) currentSelection).getPaths();
		if (paths != null && paths.length > 0) {
			Object lastSegment = paths[0].getLastSegment();
			if (lastSegment instanceof ICompilationUnit) {
				handleCompilationUnit(event, (ICompilationUnit) lastSegment);
			}

			// TODO: package

			// TODO: project
		}
	}

	private void handleCompilationUnit(ExecutionEvent event, ICompilationUnit compilationUnit) throws Exception {

		if (compilationUnit instanceof IOpenable) {
			IOpenable openable = (IOpenable) compilationUnit;
			if (openable.hasUnsavedChanges()) {
				throw new Exception(Messages.hasUnsavedChangesError);
			}
		}

		IMarker[] markers = JdtUtil.getJavaProblemMarkers(compilationUnit);
		if (markers.length > 0) {
			boolean hasErrors = false;
			// boolean hasWarining = false;
			StringBuilder sb = new StringBuilder();
			sb.append("Please fix all errors first:");

			for (IMarker marker : markers) {
				Map<String, Object> map = marker.getAttributes();

				int severity = (int) map.get("severity");

				if (severity == IMarker.SEVERITY_ERROR) {
					hasErrors = true;
					sb.append(System.lineSeparator()).append(map.get("message"));
				}
				// if (severity == IMarker.SEVERITY_WARNING) {
				// hasWarining = true;
				// }
			}

			if (hasErrors) {
				throw new Exception(Messages.hasJavaProblemsError);
			}

			// if (hasWarining) {
			// MessageDialog.openInformation(window.getShell(), "Custom
			// Container",
			// "The compilation unit has warnings!");
			// }
		}

		IType topLevelType = JdtUtil.resolveTopLevelType(compilationUnit);
		if (topLevelType == null) {
			// TODO:
			throw new Exception("Cannot find top level type");
		}

		// IAnnotation annotation = topLevelType.getAnnotation("Artefact");
		// if (annotation == null || !annotation.exists()) {
		// throw new Exception("Cannot find @Artefact annotation");
		// }

		ArtefactInfo artefactInfo = JdtUtil.getArtefactInfo(topLevelType);
		if (artefactInfo == null) {
			throw new Exception("Cannot find @Artefact annotation");
		}

		IFile targetFile = topLevelType.getJavaProject().getProject().getFile(artefactInfo.target);

		String script = getIIQScript(compilationUnit, topLevelType, artefactInfo);

		ArtefactUtil.updateArtefact(targetFile, script, artefactInfo);

		MessageConsole console = JdtUtil.getConsole("IIQ Tools");
		if (console != null) {
			MessageConsoleStream out = console.newMessageStream();
			out.println("Successfully updated IIQ Artefact: " + artefactInfo.target);
			out.flush();
			out.close();
		}
	}

	private String getIIQScript(ICompilationUnit compilationUnit, IType topLevelType, ArtefactInfo artefactInfo)
			throws JavaModelException {

		final StringBuilder sb = new StringBuilder();
		sb.append(this.lineSeparator);

		if (artefactInfo.header) {
			appendHeader(sb, compilationUnit, topLevelType);
		}

		appendImportDeclarations(sb, compilationUnit, topLevelType);
		appendFields(sb, compilationUnit, topLevelType);
		appendMethods(sb, compilationUnit, topLevelType);

		return sb.toString();
	}

	private void appendHeader(final StringBuilder sb, final ICompilationUnit compilationUnit,
			final IType topLevelType) {
		// Generated by IIQ Tools JDP on <Date> from
		// <Class name>

		sb.append("// Generated by IIQ Tools JDP on ").append(new Date()).append(" from").append(this.lineSeparator)
				.append("// ").append(topLevelType.getFullyQualifiedName()).append(this.lineSeparator).append("//")
				.append(this.lineSeparator);
	}

	private void appendMethods(final StringBuilder sb, final ICompilationUnit compilationUnit, final IType topLevelType)
			throws JavaModelException {

		final String cuSource = compilationUnit.getSource();
		IMethod bodyMethod = null;

		IMethod[] methods = topLevelType.getMethods();
		for (IMethod method : methods) {
			if (JdtUtil.hasAnnotation(method, "ArtefactBody")) {
				bodyMethod = method;

				// TODO:
				// shiftBodyLeft = true;

			} else {
				if (!JdtUtil.hasAnnotation(method, "ArtefactIgnore")) {
					ISourceRange sourceRange = method.getSourceRange();
					int offset = sourceRange.getOffset() - 1;
					int length = sourceRange.getLength() + 1;

					// go up until new line
					while (offset > 0) {
						char ch = cuSource.charAt(offset);
						if (!Character.isWhitespace(ch) || ch == '\r' || ch == '\n') {
							offset++;
							length--;
							break;
						}
						offset--;
						length++;
					}

					String script = cuSource.substring(offset, offset + length);
					script = PluginUtil.shiftLeft(script);
					sb.append(script).append(this.lineSeparator).append(this.lineSeparator);
				}
			}
		}

		if (bodyMethod != null) {
			final String methodName = bodyMethod.getElementName();

			CompilationUnit cu = SharedASTProvider.getAST(compilationUnit, SharedASTProvider.WAIT_NO, null);

			cu.accept(new ASTVisitor() {
				@Override
				public boolean visit(MethodDeclaration node) {

					SimpleName simpleName = node.getName();
					if (simpleName.toString().equals(methodName)) {
						Block block = node.getBody();

						int s = block.getStartPosition();
						int l = block.getLength();

						try {
							String script = compilationUnit.getSource();
							script = script.substring(s + 1, s + l - 2);
							script = PluginUtil.shiftLeft(script);
							script = PluginUtil.shiftLeft(script);

							sb.append(script);
						} catch (JavaModelException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}

						sb.append(lineSeparator);
					}
					return false;
				};
			});
		}
	}

	private void appendFields(final StringBuilder sb, final ICompilationUnit compilationUnit, final IType topLevelType)
			throws JavaModelException {

		boolean appendEol = false;
		IField[] fields = topLevelType.getFields();
		for (IField field : fields) {
			IAnnotation annotation = field.getAnnotation("ArtefactIgnore");
			if (!annotation.exists()) {
				String script = field.getSource();
				script = PluginUtil.shiftLeft(script);
				sb.append(script).append(this.lineSeparator);
				appendEol = true;
			}
		}

		if (appendEol) {
			sb.append(this.lineSeparator);
		}
	}

	private void appendImportDeclarations(StringBuilder sb, ICompilationUnit compilationUnit, IType topLevelType)
			throws JavaModelException {
		boolean appendEol = false;

		IImportDeclaration[] imports = compilationUnit.getImports();
		for (IImportDeclaration importDeclaration : imports) {
			String elementName = importDeclaration.getElementName();
			if (elementName != null && !elementName.startsWith("com.iiqtools.jdp.annotation.")) {
				sb.append("import ").append(importDeclaration.getElementName()).append(";").append(this.lineSeparator);
				appendEol = true;
			}
		}
		if (appendEol) {
			sb.append(this.lineSeparator);
		}
	}
}
