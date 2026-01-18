package mg.razherana.framework.web.utils.proxies;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;

public record InterceptorContext(
    WebExecutor executor,
    Map<String, String> pathParameters,
    HttpServletRequest request,
    HttpServletResponse response,
    RequestBody requestBody,
    ModelView mv,
    Map<Class<?>, Giver> givers,
    Class<?> resolvClass
  ) {
}
