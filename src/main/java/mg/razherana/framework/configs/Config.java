package mg.razherana.framework.configs;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The Config annotation is used to mark a method as a configuration method
 * within a class annotated with @AppConfig. Methods annotated with @Config
 * are typically used to define beans
 * or other configuration settings that will be
 * processed by the framework during application initialization.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.METHOD })
public @interface Config {

  /**
   * Name of the configuration bean. If not specified, the method name will be
   * used as * the bean name.
   * 
   * @return
   */
  String value() default "";

  /**
   * Indicates whether this configuration should override the configuration from the context.
   * @return
   */
  boolean override() default true;
}
