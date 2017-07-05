package com.iiqtools.jdp;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * A very simple configuration page to make the IIQ Tools Classpath Container
 * show up in the libraries to add by the user. The page is actually empty,
 * besides some text, since there's nothing to configure anyway!
 * 
 * 
 */
public class IIQToolsClasspathContainerPage extends WizardPage implements IClasspathContainerPage {

	/**
	 * Default constructor for instantiation in Eclipse.
	 */
	public IIQToolsClasspathContainerPage() {
		super("IIQ Tools Annotations", "IIQ Tools library", null);
		setDescription("Add the IIQ Tools annotations library.");
	}

	@Override
	public void createControl(Composite aParent) {
		Composite tempComposite = new Composite(aParent, SWT.NULL);
		setControl(tempComposite);
	}

	@Override
	public boolean finish() {
		return true;
	}

	@Override
	public IClasspathEntry getSelection() {
		return JavaCore.newContainerEntry(new Path(IIQToolsClasspathContainerInitializer.PATH));
	}

	@Override
	public void setSelection(IClasspathEntry aClasspathEntry) {
	}

	// @Override
	// public boolean canFlipToNextPage() {
	// return false;
	// }
	//
	// @Override
	// public String getName() {
	// return "IIQ Tools Annotations";
	// }
	//
	// @Override
	// public IWizardPage getNextPage() {
	// return null;
	// }
	//
	// @Override
	// public IWizardPage getPreviousPage() {
	// return prevPage;
	// }
	//
	// @Override
	// public IWizard getWizard() {
	// return null;
	// }
	//
	// @Override
	// public boolean isPageComplete() {
	// return true;
	// }
	//
	// IWizardPage prevPage;
	// IWizard wizard;
	//
	// @Override
	// public void setPreviousPage(IWizardPage prevPage) {
	// this.prevPage = prevPage;
	// }
	//
	// @Override
	// public void setWizard(IWizard arg0) {
	// this.wizard = wizard;
	//
	// }
	//
	// @Override
	// public void createControl(Composite aParent) {
	// Composite tempComposite = new Composite(aParent, SWT.NULL);
	//
	// setControl(tempComposite);
	// }
	//
	// @Override
	// public void dispose() {
	//
	// }
	//
	// @Override
	// public Control getControl() {
	// return null;
	// }
	//
	// @Override
	// public String getDescription() {
	// return "IIQ Tools library";
	// }
	//
	// @Override
	// public String getErrorMessage() {
	// return null;
	// }
	//
	// @Override
	// public Image getImage() {
	// return null;
	// }
	//
	// @Override
	// public String getMessage() {
	// return null;
	// }
	//
	// @Override
	// public String getTitle() {
	// return "IIQ Tools";
	// }
	//
	// @Override
	// public void performHelp() {
	//
	// }
	//
	// @Override
	// public void setDescription(String arg0) {
	//
	// }
	//
	// @Override
	// public void setImageDescriptor(ImageDescriptor arg0) {
	//
	// }
	//
	// @Override
	// public void setTitle(String arg0) {
	//
	// }
	//
	// @Override
	// public void setVisible(boolean arg0) {
	//
	// }
	//
	// @Override
	// public boolean finish() {
	// return true;
	// }
	//
	// IClasspathEntry selection;
	//
	// @Override
	// public IClasspathEntry getSelection() {
	// return selection;
	// }
	//
	// @Override
	// public void setSelection(IClasspathEntry selection) {
	// this.selection = selection;
	//
	// }

}
