package com.iiqtools.jdp.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(CLASS)
@Target(TYPE)
public @interface IIQArtefact {
	public String name();

	public String xpath();
	
	public EOL eol() default EOL.Target;
	
	// public boolean shiftLeft() default true;
}
