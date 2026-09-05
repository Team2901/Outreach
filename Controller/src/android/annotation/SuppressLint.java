package android.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Virtual Robot's shim for Android's @SuppressLint.
 *
 * Suppresses Android Lint warnings when building for the robot. There is no Lint in the
 * simulator, so this is inert - it exists only so annotated team code compiles here.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,
         ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE})
public @interface SuppressLint {
    String[] value();
}
