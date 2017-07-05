package com.iiqtools.jdp;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

public class IIQToolsClasspathContainer implements IClasspathContainer {

	IPath containerPath;
	String description;

	/**
	 * This constructor uses the provided IPath and IJavaProject arguments to
	 * assign the instance variables that are used for determining the classpath
	 * entries included in this container. The provided IPath comes from the
	 * classpath entry element in project's .classpath file. It is a three
	 * segment path with the following segments: [0] - Unique container ID [1] -
	 * project relative directory that this container will collect files from
	 * [2] - comma separated list of extensions to include in this container
	 * (extensions do not include the preceding ".")
	 * 
	 * @param containerPath
	 *            unique path for this container instance, including directory
	 *            and extensions a segments
	 * @param javaProject
	 *            the Java project that is referencing this container
	 */
	public IIQToolsClasspathContainer(IPath containerPath, IJavaProject javaProject) {
		this.containerPath = containerPath;
		this.description = "IIQ Tools Annotations";
	}

	@Override
	public IClasspathEntry[] getClasspathEntries() {
		try {
			Bundle bundle = Platform.getBundle("com.iiqtools.jdp");
			URL fileURL = FileLocator.toFileURL(bundle.getEntry("/"));
			String absolutePath = fileURL.getFile() + "com.iiqtools.jdp.annotation.jar";
			IClasspathEntry classpathEntry = JavaCore.newLibraryEntry(new Path(absolutePath), null, new Path("/"));

			return new IClasspathEntry[] { classpathEntry };

		} catch (IOException e) {
		}

		return new IClasspathEntry[] {};
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getKind() {
		return 0;
	}

	@Override
	public IPath getPath() {
		return containerPath;
	}
}
