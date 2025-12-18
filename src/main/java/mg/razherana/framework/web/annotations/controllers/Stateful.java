package mg.razherana.framework.web.annotations.controllers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Stateful {
  public static final String SESSION_ATTRIBUTE_KEY = "MRNAINA_FRAMEWORK_STATEFUL_CONTROLLER_CONTAINER_KEY";
  public long timeout() default 18_000_000; // Default 5 hours
}
