package mg.razherana.framework.web.annotations.parameters;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter of a controller method should be bound to the request body.
 * <p>
 * <b>Parameter type:</b> The annotated parameter must be of type {@code Map&lt;String, Object&gt;}.
 * <p>
 * <b>Parameter processing:</b>
 * <ul>
 *   <li>Single values are mapped as-is to the corresponding key in the map.</li>
 *   <li>Array or list values are mapped using the '[]' suffix convention. For example, request parameters named {@code param[]} will be mapped to a {@code List} or array under the key {@code "param"}.</li>
 * </ul>
 * <p>
 * This allows controller methods to flexibly handle both single and multiple values from the request body.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.PARAMETER })
public @interface ParamBody {
}
