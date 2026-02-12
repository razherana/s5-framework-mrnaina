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
import mg.razherana.framework.web.utils.ReflectUtils;
import mg.razherana.framework.web.utils.http.RequestBody;

public class GiverProvider implements ArgProvider {

  @Override
  public boolean supports(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers,
      Map<String, Object> additionalContext) {
    return Giver.class.isAssignableFrom(argType);
  }

  @Override
  public Object provide(
      WebExecutor executor,
      Parameter arg, Class<?> argType, Method method, Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response, RequestBody requestBody, ModelView mv,
      Map<Class<?>, Giver> givers,
      Map<String, Object> additionalContext) {
    // Find the giver instance from the givers map

    Class<?> youngestChildClass = ReflectUtils.getYoungestChildClass(argType, givers.keySet());

    System.out.println("[Fruits] : Youngest child class for " + argType.getName() + " is "
        + (youngestChildClass != null ? youngestChildClass.getName()
            : "null. Givers available: "
                + givers.keySet().stream().map(Class::getName).reduce((a, b) -> a + ", " + b).orElse("none")));

    // We find the first giver that is assignable to the argType
    // So if we have a giver of type AuthGiver and the argType is AbstractAuthGiver,
    // it will match
    Giver giver = givers.get(youngestChildClass);

    if (giver == null) {
      if (method.getName().equals("before")) {
        System.err.println("[Fruits] : Available givers: "
            + givers.keySet().stream().map(Class::getName).reduce((a, b) -> a + ", " + b).orElse("none"));
      }
      // Should not happen
      throw new MalformedWebAnnotationException(
          "Giver of type " + argType.getName()
              + " cannot be provided for method: " + method.getName());
    }

    return giver;
  }

}
