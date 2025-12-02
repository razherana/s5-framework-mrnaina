package mg.razherana.framework.web.annotations.parameters;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method parameter as a parameter variable.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.PARAMETER })
public @interface ParamVar {

  /**
   * Name of the parameter variable.
   */
  String value();

  /**
   * Whether the parameter is required or not.
   */
  boolean required() default false;

  /**
   * Default value if the parameter is not present.
   */
  String defaultValue() default "";

  /**
   * Force the parameter to be treated as a String.
   * Useful if you want to get from getParameter which always returns String values.
   * So even if the target type is int[], the conversion will use the String.
   */
  boolean forceString() default false;
}
