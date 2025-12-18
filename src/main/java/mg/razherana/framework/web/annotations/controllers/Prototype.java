package mg.razherana.framework.web.annotations.controllers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
/**
 * Indicates that the annotated method's controller should be treated as a prototype,
 * meaning a new instance of the controller will be created for each request.
 */
public @interface Prototype {
  // Marker annotation - no fields required
}
