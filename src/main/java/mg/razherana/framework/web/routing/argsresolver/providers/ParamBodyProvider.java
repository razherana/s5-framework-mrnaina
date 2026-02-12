package mg.razherana.framework.web.routing.argsresolver.providers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.annotations.parameters.ParamBody;
import mg.razherana.framework.web.exceptions.http.BadRequestException;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;
import mg.razherana.framework.web.utils.json.types.JsonElement;
import mg.razherana.framework.web.utils.objectconversion.ConversionObjectUtils;

public class ParamBodyProvider implements ArgProvider {

  @Override
  public boolean supports(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers,
      Map<String, Object> additionalContext) {
    return arg.isAnnotationPresent(ParamBody.class);
  }

  @Override
  public Object provide(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers,
      Map<String, Object> additionalContext) {
    Map<String, ?> bodyMap = requestBody.asMap();

    if (argType == Map.class
        && arg.getParameterizedType().getTypeName().equals(
            "java.util.Map<java.lang.String, java.lang.Object>")) {
      return new java.util.HashMap<>(bodyMap);
    }

    if (argType == JsonElement.class || JsonElement.class.isAssignableFrom(argType)) {
      var jsonElement = requestBody.getJsonElement().orElse(null);

      if (jsonElement == null) {
        throw new BadRequestException("Request body is not JSON", request);
      }

      return jsonElement;
    }

    Object convertedObject = ConversionObjectUtils
        .convertMapToObject(bodyMap, arg.getType(), executor.getControllerInstance());

    return convertedObject;
  }

}
