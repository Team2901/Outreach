package com.bylazar.configurables.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Virtual Robot's approximation of Panels' @IgnoreConfigurable.
 *
 * Marks a field that @Configurable should skip. Inert in the simulator - see
 * {@link Configurable}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface IgnoreConfigurable {
}
