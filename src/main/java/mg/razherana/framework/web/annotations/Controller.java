package mg.razherana.framework.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
  String value() default "";

  /**
   * Alias of the controller.
   * If not provided, the controller class name lowercased will be used as alias.
   * @return
   */
  String alias() default "";
}
