package com.iiqtools.jdp.util;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class BshScriptValidator {

	private URLClassLoader classLoader = null;

	public BshScriptValidator(IJavaProject javaProject) {
		try {
			IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
			if (entries != null) {

				URL[] urls = new URL[entries.length];
				for (int i = 0; i < entries.length; ++i) {
					urls[i] = entries[i].getPath().toFile().toURI().toURL();
				}

				this.classLoader = new URLClassLoader(urls);
			}
		} catch (JavaModelException e) {
			// discard errors
		} catch (MalformedURLException e) {
			// discard errors
		}
	}

	public BshSyntaxError validate(String script) {
		if (this.classLoader == null) {
			return null;
		}

		if (script == null) {
			return null;
		}

		BshSyntaxError error = null;

		// create bsh parser from the class path of the given javaProject
		try {
			Reader reader = new StringReader(script);

			Class<?> clazz = Class.forName("bsh.Parser", true, this.classLoader);

			// parser = new bsh.Parser(reader);
			Constructor<?> constructor = clazz.getConstructor(java.io.Reader.class);
			Object parser = constructor.newInstance(reader);

			// parser.setRetainComments(true);
			Method setRetainCommentsMethod = clazz.getMethod("setRetainComments", boolean.class);
			setRetainCommentsMethod.invoke(parser, true);

			// boolean eof = parser.Line();
			Method lineMethod = clazz.getMethod("Line");

			try {
				while (!(Boolean) lineMethod.invoke(parser))
					;
			} catch (InvocationTargetException e) {

				Throwable target = e.getTargetException();
				if (target != null) {
					if ("bsh.ParseException".equals(target.getClass().getName())) {

						// TODO: extract line and column
						error = new BshSyntaxError(target.getMessage(), -1, -1);
					}
				}
			}
		} catch (Exception e) {
			// discard reflection errors
			error = null;
		}

		return error;
	}
}
