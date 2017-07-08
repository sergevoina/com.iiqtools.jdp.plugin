package com.iiqtools.jdp.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author Serge Voina
 *
 */
@Retention(CLASS)
@Target(METHOD)
public @interface ArtefactBody {

}