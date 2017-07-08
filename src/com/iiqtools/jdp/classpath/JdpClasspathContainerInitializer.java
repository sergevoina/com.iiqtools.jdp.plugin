package com.iiqtools.jdp.classpath;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.iiqtools.jdp.Constants;

// http://www.javatips.net/api/integrity-master/de.gebit.integrity.eclipse/src/de/gebit/integrity/eclipse/classpath/IntegrityClasspathContainerInitializer.java

public class JdpClasspathContainerInitializer extends ClasspathContainerInitializer {

	@Override
	public void initialize(IPath containerPath, IJavaProject javaProject) throws CoreException {
		if (isValidContainerPath(containerPath)) {
			JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { javaProject },
					new IClasspathContainer[] { new JdpClasspathContainer(containerPath, javaProject) }, null);
		}
	}

	@Override
	public boolean canUpdateClasspathContainer(IPath aContainerPath, IJavaProject aProject) {
		return true;
	}

	@Override
	public void requestClasspathContainerUpdate(IPath aContainerPath, IJavaProject aProject,
			IClasspathContainer aContainerSuggestion) throws CoreException {

		if (aContainerSuggestion instanceof JdpClasspathContainer) {
			JdpClasspathContainer tempContainer = (JdpClasspathContainer) aContainerSuggestion;
			JavaCore.setClasspathContainer(aContainerPath, new IJavaProject[] { aProject },
					new IClasspathContainer[] { tempContainer }, null);
		}
	}

	private static boolean isValidContainerPath(IPath path) {
		return (path != null) && (path.segmentCount() > 0) && Constants.PLUGIN_CONTAINER_ID.equals(path.segment(0));
	}

}
