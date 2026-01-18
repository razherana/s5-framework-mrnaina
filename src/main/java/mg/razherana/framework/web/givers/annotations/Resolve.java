package mg.razherana.framework.web.givers.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a method needs to be resolved.
 * <p>The method <b>MUST</b> be an abstract method.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Resolve {
  /**
   * The alias of the Implementation to resolve.
   * <p>If not specified, the method name will be used as the alias.</p>
   * @return
   */
  String value() default "";
}
