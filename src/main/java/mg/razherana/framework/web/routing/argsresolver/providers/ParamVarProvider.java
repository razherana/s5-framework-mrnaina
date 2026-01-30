package mg.razherana.framework.web.routing.argsresolver.providers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.annotations.parameters.ParamVar;
import mg.razherana.framework.web.exceptions.http.BadRequestException;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.utils.ConversionUtils;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;

public class ParamVarProvider implements ArgProvider {

  @Override
  public boolean supports(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers) {
    return arg.isAnnotationPresent(ParamVar.class);
  }

  @Override
  public Object provide(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers) {
    ParamVar paramVar = arg.getAnnotation(ParamVar.class);
    String varName = paramVar.value();

    Object rawValue = requestBody.get(varName);

    if (rawValue == null) {
      if (paramVar.required()) {
        // The request object is being stored as additional data in the exception.
        throw new BadRequestException("Missing required parameter: " + varName, request);
      }

      // Use default value if not Part
      if (!(rawValue instanceof jakarta.servlet.http.Part))
        rawValue = paramVar.defaultValue();
    } else if (paramVar.forceString() && rawValue instanceof String[] values) {
      rawValue = values.length > 0 ? values[0] : "";
    }

    try {
      Object convertedValue = ConversionUtils
          .convertStringOrArrToType(rawValue, argType, null);

      return convertedValue;
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Type mismatch for parameter: " + varName, request);
    }
  }

}
