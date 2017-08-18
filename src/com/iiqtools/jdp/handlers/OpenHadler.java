package com.iiqtools.jdp.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.Region;

import com.iiqtools.jdp.hyperlinkDetector.ArtefactHyperlink;
import com.iiqtools.jdp.util.BshScriptBuilder;

public class OpenHadler extends ArtefactHandler {
	@Override
	protected boolean handleCompilationUnit(ExecutionEvent event, ICompilationUnit compilationUnit) throws Exception {
		BshScriptBuilder bshScript = BshScriptBuilder.parse(compilationUnit);
		new ArtefactHyperlink(new Region(0, 0), bshScript.getTargetFile(), bshScript.getArtefactInfo().xpath).open();
		return true;
	}
}
