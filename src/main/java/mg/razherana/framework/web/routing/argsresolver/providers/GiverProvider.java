package mg.razherana.framework.web.routing.argsresolver.providers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.exceptions.MalformedWebAnnotationException;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;

public class GiverProvider implements ArgProvider {

  @Override
  public boolean supports(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers) {
    return Giver.class.isAssignableFrom(argType);
  }

  @Override
  public Object provide(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers) {
    Giver giver = givers.get(argType.asSubclass(Giver.class));

    if (giver == null) {
      // Should not happen
      throw new MalformedWebAnnotationException(
          "Giver of type " + argType.getName()
              + " cannot be provided for method: " + method.getName());
    }

    return giver;
  }

}
