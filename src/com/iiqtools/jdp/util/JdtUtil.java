package com.iiqtools.jdp.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

// http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html

public class JdtUtil {
	/**
	 * Returns the top level type of the given compilation unit.
	 * 
	 * @param compilationUnit
	 *            the DOM CompilationUnit
	 * @return the top level type
	 * @throws JavaModelException
	 */
	public static IType resolveTopLevelType(final ICompilationUnit compilationUnit) throws JavaModelException {
		IType topLevelType = null;

		if ((compilationUnit != null) && compilationUnit.exists()) {
			IType[] types = compilationUnit.getTypes();
			if ((types != null) && (types.length > 0)) {
				topLevelType = types[0];
				if (topLevelType.getDeclaringType() != null) {
					topLevelType = topLevelType.getDeclaringType();
				}
			}
		}

		return topLevelType;
	}

	public static boolean hasAnnotation(IAnnotatable annotatable, String name) throws JavaModelException {
		boolean res = false;

		if (annotatable != null) {
			IAnnotation[] annotations = annotatable.getAnnotations();
			if (annotations != null) {
				for (IAnnotation annotation : annotations) {
					if (annotation.getElementName().equals(name)) {
						res = true;
						break;
					}
				}
			}
		}

		return res;
	}

	public static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	
	// public static void removeAnnotation() {
	// if (javaElement instanceof IMethod) {
	//
	// // Get the compilation unit for traversing AST
	// final ASTParser parser = ASTParser.newParser(AST.JLS4);
	// parser.setSource(javaElement.getCompilationUnit());
	// parser.setResolveBindings(true);
	//
	// final CompilationUnit compilationUnit = (CompilationUnit)
	// parser.createAST(null);
	//
	// // Record modification - to be later written with ASTRewrite
	// compilationUnit.recordModifications();
	//
	// // Get AST node for IMethod
	// int methodIndex =
	// javaElement.getCompilationUnit().getSource().indexOf(javaElement.getSource());
	//
	// ASTNode methodASTNode = NodeFinder.perform(compilationUnit.getRoot(),
	// methodIndex, javaElement.getSource().length());
	//
	// // Create the annotation
	// final NormalAnnotation newNormalAnnotation =
	// methodASTNode.getAST().newNormalAnnotation();
	// newNormalAnnotation.setTypeName(methodASTNode.getAST().newName("AnnotationTest"));
	//
	// // Add logic for writing the AST here.
	//
	// }
	// }

	public static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit); // set source
		parser.setResolveBindings(true); // we need bindings later on
		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
	}

	public static void formatUnitSourceCode(ICompilationUnit unit, IProgressMonitor monitor) throws JavaModelException {
		CodeFormatter formatter = ToolFactory.createCodeFormatter(null);
		ISourceRange range = unit.getSourceRange();
		TextEdit formatEdit = formatter.format(CodeFormatter.K_COMPILATION_UNIT, unit.getSource(), range.getOffset(),
				range.getLength(), 0, null);
		if (formatEdit != null && formatEdit.hasChildren()) {
			unit.applyTextEdit(formatEdit, monitor);
		} else {
			monitor.done();
		}
	}
}
