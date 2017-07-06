package com.iiqtools.jdp.annotation;

import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;

/**
 * Marker annotation that indicates that the annotated method or field is to be
 * ignored by IIQ Tools JDP Plugin
 * 
 * @author Serge Voina
 *
 */
@Retention(CLASS)
public @interface ArtefactIgnore {

}
