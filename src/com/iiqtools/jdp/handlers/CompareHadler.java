package com.iiqtools.jdp.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.iiqtools.jdp.util.ArtefactInfo;
import com.iiqtools.jdp.util.ArtefactUtil;
import com.iiqtools.jdp.util.BshScriptBuilder;

public class CompareHadler extends ArtefactHandler {

	static class CompareItem implements IStreamContentAccessor, ITypedElement, IModificationDate {
		private String contents, name;
		private long time;

		CompareItem(String name, String contents, long time) {
			this.name = name;
			this.contents = contents;
			this.time = time;
		}

		@Override
		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(contents.getBytes());
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public long getModificationDate() {
			return time;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getType() {
			return ITypedElement.TEXT_TYPE;
		}
	}

	static class CompareInput extends CompareEditorInput {
		private final String left;
		private final String right;

		public CompareInput(CompareConfiguration config, String left, String right) {
			super(config);

			this.left = left;
			this.right = right;
		}

		@Override
		protected Object prepareInput(IProgressMonitor pm) {
			CompareItem left = new CompareItem("Left", this.left, 0);
			CompareItem right = new CompareItem("Right", this.right, 0);
			return new DiffNode(left, right);
		}
	}

	@Override
	protected boolean handleTreeSelection(ExecutionEvent event, ITreeSelection currentSelection) throws Exception {
		boolean handled = false;

		TreePath[] paths = ((ITreeSelection) currentSelection).getPaths();
		if (paths != null && paths.length == 1) {
			Object lastSegment = paths[0].getLastSegment();
			if (lastSegment instanceof ICompilationUnit) {
				handled = handleCompilationUnit(event, (ICompilationUnit) lastSegment);
			}
		}

		return handled;
	}

	@Override
	protected boolean handleCompilationUnit(ExecutionEvent event, ICompilationUnit compilationUnit) throws Exception {
		BshScriptBuilder bshScript = BshScriptBuilder.parse(compilationUnit);

		IFile targetFile = bshScript.getTargetFile();
		String newScript = bshScript.getScript();
		ArtefactInfo artefactInfo = bshScript.getArtefactInfo();

		Document document = ArtefactUtil.getDocument(targetFile);
		if (document == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Cannot open xml artefact: ").append(System.lineSeparator()).append(targetFile.getName())
					.append(System.lineSeparator());
			throw new Exception(sb.toString());
		}

		Node node = ArtefactUtil.getDocumentNode(document, artefactInfo.xpath);
		if (node == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Cannot find unique match for xpath: ").append(System.lineSeparator()).append(artefactInfo.xpath)
					.append(System.lineSeparator());
			throw new Exception(sb.toString());
		}

		String oldScript = node.getTextContent();

		CompareConfiguration configuration = new CompareConfiguration();

		String leftLabel = compilationUnit.getPath().toFile().getName();
		configuration.setLeftLabel(leftLabel);

		String rightLabel = new StringBuilder().append(targetFile.getName()).append("::").append(artefactInfo.xpath)
				.toString();
		configuration.setRightLabel(rightLabel);

		CompareUI.openCompareEditor(new CompareInput(configuration, removeCr(newScript), removeCr(oldScript)));
		
		return true;
	}
	
	protected String removeCr(String s) {
		if (s == null) {
			return null;
		}
		return s.replaceAll("\\r", "");
	}
}
