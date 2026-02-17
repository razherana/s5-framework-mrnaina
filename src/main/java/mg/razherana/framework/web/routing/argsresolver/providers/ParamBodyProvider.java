package mg.razherana.framework.web.routing.argsresolver.providers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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

    // Get the actual type to convert to
    Class<?> targetType = resolveActualType(arg, method, executor.getControllerInstance().getClass());

    if (targetType == Map.class
        && arg.getParameterizedType().getTypeName().equals(
            "java.util.Map<java.lang.String, java.lang.Object>")) {
      return new java.util.HashMap<>(bodyMap);
    }

    if (targetType == JsonElement.class || JsonElement.class.isAssignableFrom(targetType)) {
      var jsonElement = requestBody.getJsonElement().orElse(null);

      if (jsonElement == null) {
        throw new BadRequestException("Request body is not JSON", request);
      }

      return jsonElement;
    }

    Object convertedObject = ConversionObjectUtils
        .convertMapToObject(bodyMap, targetType, executor.getControllerInstance());

    return convertedObject;
  }

  private Class<?> resolveActualType(Parameter param, Method method, Class<?> controllerClass) {
    Type parameterType = param.getParameterizedType();

    // If it's not a type variable, just return the class
    if (!(parameterType instanceof TypeVariable)) {
      if (parameterType instanceof Class) {
        return (Class<?>) parameterType;
      } else if (parameterType instanceof ParameterizedType) {
        return (Class<?>) ((ParameterizedType) parameterType).getRawType();
      }
      return param.getType();
    }

    // It's a type variable - need to resolve it from the class hierarchy
    TypeVariable<?> typeVar = (TypeVariable<?>) parameterType;

    // Get the generic superclass of the controller
    Type genericSuperclass = controllerClass.getGenericSuperclass();

    if (genericSuperclass instanceof ParameterizedType) {
      ParameterizedType parameterizedSuperclass = (ParameterizedType) genericSuperclass;
      Type[] actualTypeArguments = parameterizedSuperclass.getActualTypeArguments();
      TypeVariable<?>[] typeParameters = ((Class<?>) parameterizedSuperclass.getRawType()).getTypeParameters();

      // Find matching type parameter
      for (int i = 0; i < typeParameters.length; i++) {
        if (typeParameters[i].getName().equals(typeVar.getName())) {
          Type actualType = actualTypeArguments[i];
          if (actualType instanceof Class) {
            return (Class<?>) actualType;
          } else if (actualType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) actualType).getRawType();
          }
        }
      }
    }

    // Fallback to the erased type
    return param.getType();
  }
}