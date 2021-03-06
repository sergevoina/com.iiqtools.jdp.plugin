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
	 * A target XML Artefact. The path must be relative to the project's root.
	 * 
	 * @return
	 */
	public String target();

	/**
	 * The XPath that represents the target XML Node to update with generated
	 * BeanShell script
	 * 
	 */
	public String xpath();

	/**
	 * End of line setting.
	 * 
	 * @return
	 */
	public EOL eol() default EOL.Target;

	/**
	 * Prints the headerText at the top in the target BeanShell script.
	 * 
	 * @return
	 */
	public boolean header() default true;

	/**
	 * Prints the 'Generated by' header in the target BeanShell script.
	 * 
	 * @return
	 */
	public String headerText() default "Generated by IIQ Tools JDP on ${date} from${eol}${className}";
}
