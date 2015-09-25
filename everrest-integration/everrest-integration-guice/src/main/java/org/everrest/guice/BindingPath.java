package org.everrest.guice;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
* @author andrew00x
*/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface BindingPath {
    String value();
}
