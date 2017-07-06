package com.iiqtools.jdp.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker annotation that indicates that the annotated class is a source for
 * BeanShell script.
 * 
 * @author Serge Voina
 *
 */
@Retention(CLASS)
@Target(TYPE)
public @interface Artefact {
	/**
	 * Relative path to XML Artefact
	 * 
	 * @return
	 */
	public String target();

	/**
	 * Node path
	 * 
	 * @return
	 */
	public String xpath();

	/**
	 * End of line settings
	 * 
	 * @return
	 */
	public EOL eol() default EOL.Target;

	// public boolean shiftLeft() default true;
}
