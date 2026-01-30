package mg.razherana.framework.web.utils.proxies.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a method is an implementation method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Impl {
  /**
   * The alias or identifier for the implementation.
   * Default is the method's name.
   * @return
   */
  String value() default "";
}
