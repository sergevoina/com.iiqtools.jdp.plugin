package com.iiqtools.jdp.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class JdpToolsNature implements IProjectNature {

	public static final String NATURE_ID = "com.iiqtools.jdp";

	private IProject project;

	public void configure() throws CoreException {
		// Add nature-specific information
		// for the project, such as adding a builder
		// to a project's build spec.

//		String jarPath = "";
//		IClasspathEntry jar = JavaCore.newLibraryEntry(new Path(jarPath), null, null);
	}

	public void deconfigure() throws CoreException {
		// Remove the nature-specific information here.
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject value) {
		project = value;
	}
}