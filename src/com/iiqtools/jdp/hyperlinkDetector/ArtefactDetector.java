package com.iiqtools.jdp.hyperlinkDetector;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.JavaWordFinder;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.texteditor.ITextEditor;

import com.iiqtools.jdp.util.ArtefactInfo;
import com.iiqtools.jdp.util.PluginUtil;

/**
 * 
 * 
 * @author Serge Voina
 *
 */
@SuppressWarnings("restriction")
public class ArtefactDetector extends AbstractHyperlinkDetector implements IHyperlinkDetector {

	private static final String ARTEFACT_ID = "Artefact";

	// org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlinkDetector
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor textEditor = getAdapter(ITextEditor.class);
		if ((region == null) || !(textEditor instanceof JavaEditor))
			return null;

		ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(textEditor.getEditorInput());
		if (!(typeRoot instanceof ICompilationUnit))
			return null;

		// CompilationUnit astRoot = SharedASTProvider.getAST(typeRoot,
		// SharedASTProvider.WAIT_NO, null);
		// if (astRoot == null)
		// return null;
		//
		// ASTNode node = NodeFinder.perform(astRoot, region.getOffset(),
		// region.getLength());
		// if (!(node instanceof SimpleName))
		// return null;
		// if (!ARTEFACT_ID.equals(((SimpleName) node).getIdentifier()))
		// return null;
		//
		// ASTNode parent = node.getParent();
		// if (!(parent instanceof NormalAnnotation))
		// return null;
		// NormalAnnotation annotation = (NormalAnnotation) parent;
		//
		// // Get target property value
		// String target = null;
		// String xpath = null;
		//
		// for (Object obj : annotation.values()) {
		// if (obj instanceof MemberValuePair) {
		// MemberValuePair pair = (MemberValuePair) obj;
		// if ("target".equals(pair.getName().getIdentifier())) {
		// target = (String) pair.getValue().resolveConstantExpressionValue();
		// } else if ("xpath".equals(pair.getName().getIdentifier())) {
		// xpath = (String) pair.getValue().resolveConstantExpressionValue();
		// }
		// }
		// }
		//
		// IRegion targetRegion = new Region(annotation.getStartPosition(),
		// annotation.getLength());
		// // create path
		// IPath artefactPath =
		// typeRoot.getJavaProject().getPath().append(target);
		//
		// return new IHyperlink[] { new ArtefactHyperlink(targetRegion,
		// artefactPath, xpath) };

		try {
			// Get Java elements corresponding to the given selected text in
			// this compilation unit
			IJavaElement[] selectedElements = typeRoot.codeSelect(region.getOffset(), 0);
			if (selectedElements == null || selectedElements.length == 0)
				return null;

			IJavaElement selectedElement = selectedElements[0];
			if (!ARTEFACT_ID.equals(selectedElement.getElementName()))
				return null;

			ICompilationUnit cu = (ICompilationUnit) typeRoot;

			// Get the innermost Java element enclosing a given source position
			IJavaElement element = cu.getElementAt(region.getOffset());
			if (!(element instanceof IAnnotatable))
				return null;

			// Get target property value
			ArtefactInfo artefactInfo = ArtefactInfo.parse(element);

			if (artefactInfo == null)
				return null;

			if (PluginUtil.isNullOrEmpty(artefactInfo.target))
				return null;

			// calculate the hyperlink region
			IRegion wordRegion = JavaWordFinder.findWord(textViewer.getDocument(), region.getOffset());
			if (wordRegion == null || wordRegion.getLength() == 0)
				return null;

			// create path
			// IPath artefactPath =
			// element.getJavaProject().getPath().append(target);

			IFile targetFile = element.getJavaProject().getProject().getFile(artefactInfo.target);
			return new IHyperlink[] { new ArtefactHyperlink(wordRegion, targetFile, artefactInfo.xpath) };
		} catch (JavaModelException ex) {
		}

		return null;
	}
}
