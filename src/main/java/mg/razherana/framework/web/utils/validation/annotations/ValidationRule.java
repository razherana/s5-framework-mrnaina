package mg.razherana.framework.web.utils.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Repeatable(ValidationRulesAnnot.class)
public @interface ValidationRule {
  /**
   * Validation rule to apply.
   */
  String rule();

  /**
   * Custom message to return when validation fails
   */
  String message() default "";
}
