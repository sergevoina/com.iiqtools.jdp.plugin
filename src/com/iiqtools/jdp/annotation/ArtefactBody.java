package com.iiqtools.jdp.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker annotation that indicates the target BeanShell main script. The method
 * declaration is removed and only the method body is copied to the target
 * script.
 * 
 * @author Serge Voina
 *
 */
@Retention(CLASS)
@Target(METHOD)
public @interface ArtefactBody {
}
