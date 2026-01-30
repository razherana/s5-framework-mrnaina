package mg.razherana.framework.web.routing.argsresolver.providers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.annotations.parameters.PathVar;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.utils.ConversionUtils;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;

public class PathVarProvider implements ArgProvider {

  @Override
  public boolean supports(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers) {
    return arg.isAnnotationPresent(PathVar.class);
  }

  @Override
  public Object provide(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers) {
    PathVar pathVar = arg.getAnnotation(PathVar.class);
    String varName = pathVar.value();

    String varValue = pathParameters.get(varName);

    // Convert to appropriate type
    Object convertedValue = ConversionUtils
        .convertStringToType(varValue, argType);

    return convertedValue;
  }

}
