package com.iiqtools.jdp.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

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

	public static boolean hasAnnotation(final IAnnotatable annotatable, final String name) throws JavaModelException {
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

	public static MessageConsole getConsole(final String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		
		// no console found, so create a new one
		MessageConsole console = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { console });
		return console;
	}

	public static IMarker[] findJavaProblemMarkers(ICompilationUnit unit) throws CoreException {
		IMarker[] markers = null;
		if (unit != null) {
			IResource javaSourceFile = unit.getUnderlyingResource();
			markers = javaSourceFile.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
					IResource.DEPTH_INFINITE);
		}
		return markers;
	}
}
