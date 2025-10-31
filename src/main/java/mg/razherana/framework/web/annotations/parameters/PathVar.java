package mg.razherana.framework.web.annotations.parameters;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method parameter as a path variable.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.PARAMETER })
public @interface PathVar {
  /**
   * Name of the path variable.
   * @return
   */
  String value();
}
