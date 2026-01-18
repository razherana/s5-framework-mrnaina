package mg.razherana.framework.web.routing.argsresolver;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.exceptions.MalformedWebAnnotationException;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.routing.argsresolver.providers.ArgProvider;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;

public class ArgResolver {
  private List<ArgProvider> providers = new ArrayList<>();

  public void registerProvider(ArgProvider provider) {
    this.providers.add(provider);
  }

  public Object resolveArgument(WebExecutor executor,
      Parameter arg, Class<?> argType, Method method,
      Map<String, String> pathParameters, HttpServletRequest request, HttpServletResponse response,
      RequestBody requestBody, ModelView mv, Map<Class<?>, Giver> givers) {
    for (ArgProvider provider : providers) {
      if (provider.supports(executor, arg, argType, method, pathParameters, request, response, requestBody, mv, givers)) {
        return provider.provide(executor, arg, argType, method, pathParameters, request, response, requestBody, mv, givers);
      }
    }

    // If no provider supports this argument, throw an exception
    throw new MalformedWebAnnotationException(
        "Unsupported parameter type: " + argType.getName() + " in method: " + method.getName());
  }
}
