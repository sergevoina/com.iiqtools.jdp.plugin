package com.iiqtools.jdp.util;

public class SyntaxError {
	public final String message;
	public final int line;
	public final int column;

	public SyntaxError(String message, int line, int column) {
		this.message = message;
		this.line = line;
		this.column = column;
	}
}
