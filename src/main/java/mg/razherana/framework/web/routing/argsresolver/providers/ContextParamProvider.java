package mg.razherana.framework.web.routing.argsresolver.providers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.utils.ConversionUtils;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;

public class ContextParamProvider implements ArgProvider {

  @Override
  public boolean supports(WebExecutor executor, Parameter arg, Class<?> argType, Method method,
      Map<String, String> pathParameters, HttpServletRequest request, HttpServletResponse response,
      RequestBody requestBody, ModelView mv, Map<Class<?>, Giver> givers,
      Map<String, Object> additionalContext) {
    return arg.isAnnotationPresent(mg.razherana.framework.web.annotations.parameters.ContextVar.class);
  }

  @Override
  public Object provide(WebExecutor executor, Parameter arg, Class<?> argType, Method method,
      Map<String, String> pathParameters, HttpServletRequest request, HttpServletResponse response,
      RequestBody requestBody, ModelView mv, Map<Class<?>, Giver> givers,
      Map<String, Object> additionalContext) {
    Object value = request.getServletContext().getAttribute(
        arg.getAnnotation(mg.razherana.framework.web.annotations.parameters.ContextVar.class).value());

    if (value instanceof String stringValue)
      return ConversionUtils.convertStringToType(stringValue, argType);

    return value;
  }

}
