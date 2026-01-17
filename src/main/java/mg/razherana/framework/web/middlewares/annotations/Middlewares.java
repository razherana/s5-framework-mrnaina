package mg.razherana.framework.web.middlewares.annotations;

import mg.razherana.framework.web.middlewares.Middleware;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation adds middlewares to a controller's method.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Middlewares {
  /**
   * The classes of Middleware to initialize.
   */
  Class<? extends Middleware>[] value();
}
