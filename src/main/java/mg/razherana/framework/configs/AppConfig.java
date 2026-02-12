package mg.razherana.framework.configs;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The AppConfig annotation is used to mark a class as a configuration class for
 * the application.
 * Classes annotated with @AppConfig can contain bean definitions and other
 * configuration settings that will be processed by the framework during
 * application initialization.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.TYPE })
public @interface AppConfig {
}
