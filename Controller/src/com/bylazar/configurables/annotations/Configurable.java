package com.bylazar.configurables.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Virtual Robot's approximation of Panels' @Configurable.
 *
 * On a real robot this exposes a class's static fields for live editing in the Panels
 * web dashboard. The simulator has no such dashboard, so the annotation is inert -
 * it exists only so annotated OpModes compile and run.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Configurable {
}
