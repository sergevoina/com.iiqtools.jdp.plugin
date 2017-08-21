package com.iiqtools.jdp.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to declare BeanShell script directly on source method. The method
 * body is ignored completely, and the value of the annotation is used instead.
 * 
 * 
 * @author Serge Voina
 *
 */
@Retention(CLASS)
@Target(METHOD)
public @interface ArtefactScript {
	public String value();
}
