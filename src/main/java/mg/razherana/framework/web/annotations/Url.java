package mg.razherana.framework.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import mg.razherana.framework.web.containers.RoutingContainer.HttpMethod;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Url {
  String value();
  
  HttpMethod method() default HttpMethod.GET;
}
