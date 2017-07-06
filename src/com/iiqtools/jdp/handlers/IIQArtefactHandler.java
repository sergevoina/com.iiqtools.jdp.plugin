package com.iiqtools.jdp.handlers;

import java.io.File;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
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

import com.iiqtools.jdp.annotation.EOL;
import com.iiqtools.jdp.util.JdtUtil;
import com.iiqtools.jdp.util.TextUtil;
import com.iiqtools.jdp.util.XmlUtil;

/**
 * http://www.vogella.com/tutorials/EclipseJDT/article.html
 * 
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class IIQArtefactHandler extends AbstractHandler {

	final String lineSeparator;

	public IIQArtefactHandler() {
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
			MessageDialog.openInformation(window.getShell(), "IIQ Tools JDP", e.getMessage());
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

		// TODO: check the java file is saved

		// TODO: check the java class doesn't have errors

		IType topLevelType = JdtUtil.resolveTopLevelType(compilationUnit);
		if (topLevelType == null) {
			// TODO:
			throw new Exception("Cannot find top level type");
		}

		IAnnotation annotation = topLevelType.getAnnotation("Artefact");
		if (annotation == null || !annotation.exists()) {
			throw new Exception("Cannot find @Artefact annotation");
		}

		String target = null;
		String xpath = null;
		EOL eol = null;
		for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
			String memberName = pair.getMemberName();
			if ("target".equals(memberName)) {
				target = (String) pair.getValue();
			} else if ("xpath".equals(memberName)) {
				xpath = (String) pair.getValue();
			} else if ("eol".equals(memberName)) {
				String val = (String) pair.getValue();
				if (val != null && val.startsWith("EOL.")) {
					val = val.substring(4);
				}
				eol = EOL.valueOf(val);
			}
		}

		IJavaProject javaProject = topLevelType.getJavaProject();

		File projectDir = javaProject.getResource().getLocation().toFile();
		if (projectDir == null) {
			throw new Exception("Cannot find JavaProject location");
		}

		File artefactFile = new File(projectDir, target);
		if (!artefactFile.exists() || !artefactFile.isFile()) {
			throw new Exception("Cannot find IIQ Artefact: " + target);
		}

		String script = getIIQScript(compilationUnit, topLevelType);

		XmlUtil.updateArtefact(artefactFile, xpath, script, eol);

		MessageConsole console = JdtUtil.findConsole("IIQ Tools");
		if (console != null) {
			MessageConsoleStream out = console.newMessageStream();
			out.println("Successfully updated IIQ Artefact: " + target);
			out.flush();
			out.close();
		}
	}

	private String getIIQScript(ICompilationUnit compilationUnit, IType topLevelType) throws JavaModelException {

		final StringBuilder sb = new StringBuilder();
		sb.append(this.lineSeparator);

		appendHeader(sb, compilationUnit, topLevelType);

		appendImportDeclarations(sb, compilationUnit, topLevelType);
		appendFields(sb, compilationUnit, topLevelType);
		appendMethods(sb, compilationUnit, topLevelType);

		return sb.toString();
	}

	private void appendHeader(StringBuilder sb, ICompilationUnit compilationUnit, IType topLevelType) {
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

					String script = TextUtil.shiftLeft(cuSource.substring(offset, offset + length));
					// cuSource

					// String script = method.getSource();
					sb.append(script).append(this.lineSeparator).append(this.lineSeparator);
				}
			}
		}

		if (bodyMethod != null) {
			final String methodName = bodyMethod.getElementName();
			CompilationUnit cu = JdtUtil.parse(compilationUnit);
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

							script = TextUtil.shiftLeft(TextUtil.shiftLeft(script));
							sb.append(script);
						} catch (JavaModelException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}

						// sb.append(bodyMethod.getSource()).append(this.lineSeparator);
						sb.append(lineSeparator);
					}
					return false;
				};

			});
		}
	}

	private void appendFields(StringBuilder sb, ICompilationUnit compilationUnit, IType topLevelType)
			throws JavaModelException {

		boolean appendEol = false;
		IField[] fields = topLevelType.getFields();
		for (IField field : fields) {
			IAnnotation annotation = field.getAnnotation("ArtefactIgnore");
			if (!annotation.exists()) {
				String script = TextUtil.shiftLeft(field.getSource());
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
