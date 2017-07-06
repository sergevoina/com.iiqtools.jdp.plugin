package com.iiqtools.jdp;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

	// plugin id
	public static final String PLUGIN_ID = "com.iiqtools.jdp";
	// Custom library relative path. The specified path is always relative to
	// the
	// root of the bundle and may begin with "/".
	public static final String PLUGIN_LIBRARY_PATH = "com.iiqtools.jdp.annotation.jar";

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

		List<String> listOfFiles = new ArrayList<String>();
		listOfFiles.add(getAnnotationLibraryPath());

		String dirPath = getSelectedDirectory(containerPath);
		if (dirPath != null) {
			File[] files = new File(dirPath).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File file, String name) {
					return (name != null) && name.endsWith(".jar") && !name.endsWith("-src.zip");
				}
			});

			if (files != null) {
				for (File file : files) {
					listOfFiles.add(file.getAbsolutePath());
				}
			}
		}

		IClasspathEntry[] entries = new IClasspathEntry[listOfFiles.size()];
		for (int i = 0; i < listOfFiles.size(); ++i) {
			entries[i] = newClasspathEntry(listOfFiles.get(i));
		}

		return entries;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	@Override
	public IPath getPath() {
		return containerPath;
	}

	private IClasspathEntry newClasspathEntry(String path) {
		// the absolute path of the corresponding source archive or folder, or
		// null if
		// none.
		IPath srcPath = null;
		// the location of the root of the source files within the source
		// archive or
		// folder or null if this location should be automatically detected.
		IPath srcRoot = null;

		if (path != null && path.endsWith(".jar")) {
			String srcFile = path.substring(0, path.length() - 4) + "-src.zip";

			if (new File(srcFile).exists()) {
				srcPath = new Path(srcFile);
				// srcRoot = new Path("/");
			}
		}

		IPath libPath = new Path(path);
		return JavaCore.newLibraryEntry(libPath, srcPath, srcRoot, false);
	}

	private String getAnnotationLibraryPath() {
		String path = null;

		// Bundle bundle = Platform.getBundle("com.iiqtools.jdp");
		// URL fileURL = FileLocator.toFileURL(bundle.getEntry("/"));
		// String absolutePath = fileURL.getFile() +
		// "com.iiqtools.jdp.annotation.jar";
		// IClasspathEntry classpathEntry = JavaCore.newLibraryEntry(new
		// Path(absolutePath), null, new Path("/"));

		/* get bundle with the specified id */
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		if (bundle != null) {
			// Returns a URL to the entry at the specified path in this bundle.
			// This
			// bundle's class loader is not used to search for the entry. Only
			// the contents
			// of this bundle are searched for the entry.
			//
			// The specified path is always relative to the root of this bundle
			// and may
			// begin with "/". A path value of "/" indicates the root of this
			// bundle.
			//
			// Note: Jar and zip files are not required to include directory
			// entries. URLs
			// to directory entries will not be returned if the bundle contents
			// do not
			// contain directory entries.
			//
			// bundleentry://1004.fwk664232848/test.custom.annotation.jar
			URL libURL = bundle.getEntry(PLUGIN_LIBRARY_PATH);
			if (libURL != null) {
				try {
					// file:/C:/Users/...
					URL localLibURL = FileLocator.toFileURL(libURL);
					if (localLibURL != null) {
						// file:/C:/Users/...
						URI localLibURI = new URI(localLibURL.toExternalForm());
						if (localLibURI != null) {
							// /C:/Users/...
							path = localLibURI.getPath();
						}
					}
				} catch (IOException e) {
					// e.printStackTrace();
				} catch (URISyntaxException e) {
					// e.printStackTrace();
				}
			}
		}

		return path;
	}

	public static String getSelectedDirectory(IPath containerPath) {
		String dirPath = null;

		if (containerPath != null) {
			boolean win32 = Platform.OS_WIN32.equals(Platform.getOS());

			if (containerPath.segmentCount() > 1) {
				StringBuilder sb = new StringBuilder();
				for (int i = 1; i < containerPath.segmentCount(); ++i) {
					String segment = containerPath.segment(i);

					if (i == 1) {
						if (win32) {
							sb.append(segment.charAt(0)).append(':');
						} else {
							sb.append("/").append(segment);
						}
					} else {
						sb.append("/").append(segment);
					}
				}

				dirPath = sb.toString();
			}
		}
		return dirPath;
	}

}
