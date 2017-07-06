package com.iiqtools.jdp;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

// http://www.javatips.net/api/integrity-master/de.gebit.integrity.eclipse/src/de/gebit/integrity/eclipse/classpath/IntegrityClasspathContainerInitializer.java

public class IIQToolsClasspathContainerInitializer extends ClasspathContainerInitializer {

	/**
	 * The path name of the IIQ Tools Classpath Container.
	 */
	public static final String PLUGIN_CONTAINER_ID = "com.iiqtools.jdp.IIQTOOLS_CLASSPATH_CONTAINER";

	@Override
	public void initialize(IPath containerPath, IJavaProject javaProject) throws CoreException {
		if (isValidContainerPath(containerPath)) {
			JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { javaProject },
					new IClasspathContainer[] { new IIQToolsClasspathContainer(containerPath, javaProject) }, null);
		}
	}

	@Override
	public boolean canUpdateClasspathContainer(IPath aContainerPath, IJavaProject aProject) {
		return true;
	}

	@Override
	public void requestClasspathContainerUpdate(IPath aContainerPath, IJavaProject aProject,
			IClasspathContainer aContainerSuggestion) throws CoreException {

		if (aContainerSuggestion instanceof IIQToolsClasspathContainer) {
			IIQToolsClasspathContainer tempContainer = (IIQToolsClasspathContainer) aContainerSuggestion;
			JavaCore.setClasspathContainer(aContainerPath, new IJavaProject[] { aProject },
					new IClasspathContainer[] { tempContainer }, null);
		}
	}

	private static boolean isValidContainerPath(IPath path) {
		return (path != null) && (path.segmentCount() > 0) && PLUGIN_CONTAINER_ID.equals(path.segment(0));
	}

}
