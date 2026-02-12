package mg.razherana.framework.web.routing.argsresolver.providers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;

public interface ArgProvider {
  public boolean supports(
      WebExecutor executor,
      Parameter arg,
      Class<?> argType,
      Method method,
      Map<String, String> pathParameters,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestBody requestBody,
      ModelView mv,
      Map<Class<?>, Giver> givers,
      Map<String, Object> additionalContext
    );

  public Object provide(
      WebExecutor executor,
      Parameter arg,
      Class<?> argType,
      Method method,
      Map<String, String> pathParameters,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestBody requestBody,
      ModelView mv,
      Map<Class<?>, Giver> givers,
      Map<String, Object> additionalContext
    );
}