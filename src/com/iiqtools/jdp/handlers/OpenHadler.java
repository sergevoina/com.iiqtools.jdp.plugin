package com.iiqtools.jdp.handlers;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.Region;

import com.iiqtools.jdp.hyperlinkDetector.ArtefactHyperlink;
import com.iiqtools.jdp.util.ArtefactInfo;

public class OpenHadler extends ArtefactHandler {

	@Override
	protected void processScript(ICompilationUnit compilationUnit, IFile targetFile, String newScript,
			ArtefactInfo artefactInfo) throws Exception {
		new ArtefactHyperlink(new Region(0, 0), targetFile, artefactInfo.xpath).open();
	}

}
