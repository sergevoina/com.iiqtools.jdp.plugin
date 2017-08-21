package com.iiqtools.jdp.util;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaModelException;

public class ArtefactScriptInfo {
	public final String value;
	public final IAnnotation annotation;

	public ArtefactScriptInfo(IAnnotation annotation, String value) {
		this.annotation = annotation;
		this.value = value;
	}

	public static ArtefactScriptInfo parse(IAnnotatable annotatable) throws JavaModelException {
		ArtefactScriptInfo info = null;

		IAnnotation annotation = annotatable.getAnnotation("ArtefactScript");
		if (annotation != null && annotation.exists()) {
			// mandatory attributes
			String value = null;

			IMemberValuePair[] pairs = annotation.getMemberValuePairs();
			if (pairs != null) {

				for (IMemberValuePair pair : pairs) {
					String memberName = pair.getMemberName();
					Object val = pair.getValue();

					if ("value".equals(memberName)) {
						value = (String) val;
					}
				}
			}

			if(value != null) {
				info = new ArtefactScriptInfo(annotation, value);
			}
		}

		return info;
	}
}
