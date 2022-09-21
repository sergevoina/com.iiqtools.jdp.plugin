package com.iiqtools.jdp.handlers;

import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.IProgressService;

import com.iiqtools.jdp.IIQToolsPlugin;
import com.iiqtools.jdp.Messages;
import com.iiqtools.jdp.util.JdtUtil;

public class DeployHadler extends AbstractHandler {

	String errorMessage = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		try {
			boolean handled = false;

			ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
			if (currentSelection instanceof ITreeSelection) {
				handled = handleTreeSelection(event, window, (ITreeSelection) currentSelection);
			} else if (currentSelection instanceof ITextSelection) {
				IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
				if (activeEditor != null) {
					if (activeEditor.isDirty()) {
						throw new Exception(Messages.docHasUnsavedChangesError);
					} else {
						IEditorInput input = activeEditor.getEditorInput();
						if (input instanceof FileEditorInput) {
							handled = handleDocument(event, window,
									new IFile[] { ((FileEditorInput) input).getFile() });
						}
					}
				}
			}

			if (!handled) {
				MessageDialog.openInformation(window.getShell(), Messages.messageDialogTitle,
						Messages.selectionIsNotSupportedError);
			}
		} catch (Exception e) {
			MessageDialog.openError(window.getShell(), Messages.messageDialogTitle, e.getMessage());
		}
		return null;
	}

	protected boolean handleTreeSelection(ExecutionEvent event, IWorkbenchWindow window,
			ITreeSelection currentSelection) throws Exception {
		List<IFile> list = new ArrayList<>();

		TreePath[] paths = ((ITreeSelection) currentSelection).getPaths();
		if (paths != null) {
			for (TreePath path : paths) {
				Object lastSegment = path.getLastSegment();

				if (lastSegment instanceof IFile) {
					list.add((IFile) lastSegment);
				}
			}
		}

		return handleDocument(event, window, list.toArray(new IFile[list.size()]));
	}

	protected boolean handleDocument(ExecutionEvent event, IWorkbenchWindow window, IFile[] files) throws Exception {

		List<IFile> xmlFiles = new ArrayList<>();
		for (IFile f : files) {
			if (f.getName().toLowerCase().endsWith(".xml")) {
				xmlFiles.add(f);
			}
		}

		if (xmlFiles.isEmpty()) {
			return false;
		}

		errorMessage = null;

		// post files one by one and show progress
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		ps.busyCursorWhile(new IRunnableWithProgress() {
			public void run(IProgressMonitor pm) {
				deployFiles(event, window, pm, xmlFiles);
			}
		});

		if (errorMessage != null) {
			MessageDialog.openError(window.getShell(), Messages.messageDialogTitle, errorMessage);
		}

		return true;
	}

	void deployFiles(ExecutionEvent event, IWorkbenchWindow window, IProgressMonitor pm, List<IFile> xmlFiles) {

		MessageConsole console = JdtUtil.getConsole("IIQ Tools");
		MessageConsoleStream out = console.newMessageStream();

		SubMonitor subMonitor = SubMonitor.convert(pm, xmlFiles.size());

		try {
			Properties targetProperties = new Properties();

			IProject project = xmlFiles.get(0).getProject();

			String targetPropertiesFileName = IIQToolsPlugin.getDefault().getTargetPropertiesPreference();
			Duration timeout = Duration.ofSeconds(IIQToolsPlugin.getDefault().getConnectionTimeoutPreference());

			// TODO: show dialog to select a target
			// get all *.target.properties
//			List<String> targets = new ArrayList<String>();
//			project.accept(new IResourceVisitor() {
//				@Override
//				public boolean visit(IResource r) throws CoreException {
//					if (r.getName().endsWith(".target.properties")) {
//						targets.add(r.getName());
//					}
//					return true;
//				}
//			}, 1, false);

			IFile targetPropertiesFile = project.getFile(targetPropertiesFileName);
			try (InputStream is = targetPropertiesFile.getContents()) {
				targetProperties.load(is);
			}

			String eclipseUrl = targetProperties.getProperty("%%ECLIPSE_URL%%");
			if (eclipseUrl == null) {
				throw new Exception("Unresolved variable: %%ECLIPSE_URL%%");
			}
			String eclipseUser = targetProperties.getProperty("%%ECLIPSE_USER%%");
			if (eclipseUser == null) {
				throw new Exception("Unresolved variable: %%ECLIPSE_USER%%");
			}
			String eclipsePass = targetProperties.getProperty("%%ECLIPSE_PASS%%");
			if (eclipsePass == null) {
				throw new Exception("Unresolved variable: %%ECLIPSE_PASS%%");
			}

			String url = null;
			if (eclipseUrl.endsWith("/")) {
				url = eclipseUrl + "rest/workflows/Importer/launch";
			} else {
				url = eclipseUrl + "/rest/workflows/Importer/launch";
			}

			HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
					// Always redirect, except from HTTPS URLs to HTTP URLs.
					.followRedirects(HttpClient.Redirect.NORMAL).proxy(ProxySelector.getDefault()).build();

			HttpResponse.BodyHandler<String> AS_STRING = HttpResponse.BodyHandlers.ofString();

			for (IFile file : xmlFiles) {
				out.println("Processing " + file.getName() + " ...");

				String xml = null;
				try (InputStream is = file.getContents()) {
					xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
				}

				List<String> unresolvedVars = new ArrayList<>();
				StringSubstitutor ss = new StringSubstitutor(new StringLookup() {
					@Override
					public String lookup(String name) {
						String varName = "%%" + name + "%%";
						String val = targetProperties.getProperty(varName);
						if (val == null) {
							unresolvedVars.add(varName);
						}
						return val;
					}
				}, "%%", "%%", '\\');

				String newXml = ss.replace(xml);

				if (!unresolvedVars.isEmpty()) {
					// throw new Exception("Unseloved variables: " + unresolvedVars);
				}

				for (String varname : unresolvedVars) {
					out.println("   !!! WARN: Unresolved variable: " + varname);
				}

				String jsonString = StringEscapeUtils.escapeJson(newXml);
				String payload = "{\"workflowArgs\": {\"operation\": \"Import\", \"resource\": \"" + jsonString
						+ "\"}}";

				var request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(timeout)
						.POST(HttpRequest.BodyPublishers.ofString(payload)).header("Content-Type", "application/json")
						.header("Authorization", "Basic "
								+ Base64.getEncoder().encodeToString((eclipseUser + ":" + eclipsePass).getBytes()))
						.build();

				var resDeploy = httpClient.send(request, AS_STRING);
				var resDeployStatusCode = resDeploy.statusCode();

				if (resDeployStatusCode == 200) {
					out.println("OK");
				} else {
					out.println("FAILED: HTTP Response Status Code " + resDeployStatusCode);
				}

				subMonitor.split(1);
			}
		} catch (Exception e) {
			errorMessage = "Failed to deploy artefacts: " + e.getMessage();
			out.println(errorMessage);
		} finally {
			pm.clearBlocked();
			pm.done();

			try {
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
