package com.iiqtools.jdp.util;

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
}
