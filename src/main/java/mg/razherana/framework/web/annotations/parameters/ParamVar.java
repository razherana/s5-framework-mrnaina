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
}
