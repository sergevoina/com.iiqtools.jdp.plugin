package com.iiqtools.jdp.util;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaModelException;

import com.iiqtools.jdp.annotation.EOL;

public class ArtefactInfo {
	public final String target;
	public final String xpath;
	public final boolean header;
	public final EOL eol;

	public ArtefactInfo(String target, String xpath, boolean header, EOL eol) {
		this.target = target;
		this.xpath = xpath;
		this.header = header;
		this.eol = eol;
	}

	public static ArtefactInfo parse(Object typeObject) throws JavaModelException {

		ArtefactInfo info = null;
		if (typeObject instanceof IAnnotatable) {

			IAnnotation annotation = ((IAnnotatable) typeObject).getAnnotation("Artefact");
			if (annotation != null && annotation.exists()) {
				// mandatory attributes
				String target = null;
				String xpath = null;
				// set default values
				boolean header = true;
				EOL eol = EOL.Target;

				IMemberValuePair[] pairs = annotation.getMemberValuePairs();
				if (pairs != null) {

					for (IMemberValuePair pair : pairs) {
						String memberName = pair.getMemberName();
						Object value = pair.getValue();

						if ("target".equals(memberName)) {
							target = (String) value;
						} else if ("xpath".equals(memberName)) {
							xpath = (String) value;
						} else if ("eol".equals(memberName)) {
							String val = (String) value;
							if (val != null && val.startsWith("EOL.")) {
								val = val.substring(4);
							}
							eol = EOL.valueOf(val);
						} else if ("header".equals(memberName)) {
							header = (Boolean) value;
						}
					}
				}

				info = new ArtefactInfo(target, xpath, header, eol);
			}
		}
		return info;
	}
}
