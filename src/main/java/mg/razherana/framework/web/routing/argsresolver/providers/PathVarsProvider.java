package mg.razherana.framework.web.routing.argsresolver.providers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.annotations.parameters.PathVars;
import mg.razherana.framework.web.exceptions.MalformedWebAnnotationException;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;

public class PathVarsProvider implements ArgProvider {

  @Override
  public boolean supports(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers) {
    return arg.isAnnotationPresent(PathVars.class);
  }

  @Override
  public Object provide(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers) {
    // Check if the type is Map<String, String>
    if (argType == Map.class
        && arg.getParameterizedType().getTypeName().equals(
            "java.util.Map<java.lang.String, java.lang.String>")) {
      return pathParameters;
    }

    // If not the correct type, throw an exception
    throw new MalformedWebAnnotationException(
        "@PathVars can only be applied to parameters of type Map<String, String> in method: "
            + method.getName());
  }

}
