package mg.razherana.framework.web.annotations.parameters;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method parameter as a context variable.
 * This allows the parameter to be injected with init parameters from the
 * servlet context.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.PARAMETER })
public @interface ContextVar {
  /**
   * Name of the context variable to inject.
   * This should match the name of an init parameter defined in the servlet
   * context.
   */
  String value();
}
