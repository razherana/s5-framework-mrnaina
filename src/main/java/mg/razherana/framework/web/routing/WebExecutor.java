package mg.razherana.framework.web.routing;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.App;
import mg.razherana.framework.web.annotations.JsonUrl;
import mg.razherana.framework.web.annotations.controllers.Prototype;
import mg.razherana.framework.web.annotations.controllers.Stateful;
import mg.razherana.framework.web.containers.ResponseContainer;
import mg.razherana.framework.web.containers.WebRouteContainer;
import mg.razherana.framework.web.exceptions.MalformedWebAnnotationException;
import mg.razherana.framework.web.exceptions.WebExecutionException;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.givers.proxies.GiverMethodInterceptor;
import mg.razherana.framework.web.handlers.responses.ResponseHandler;
import mg.razherana.framework.web.middlewares.Middleware;
import mg.razherana.framework.web.middlewares.annotations.Middlewares;
import mg.razherana.framework.web.routing.argsresolver.ArgResolver;
import mg.razherana.framework.web.routing.argsresolver.providers.GiverProvider;
import mg.razherana.framework.web.routing.argsresolver.providers.HttpServletRequestProvider;
import mg.razherana.framework.web.routing.argsresolver.providers.HttpServletResponseProvider;
import mg.razherana.framework.web.routing.argsresolver.providers.HttpSessionProvider;
import mg.razherana.framework.web.routing.argsresolver.providers.ModelViewProvider;
import mg.razherana.framework.web.routing.argsresolver.providers.ParamBodyProvider;
import mg.razherana.framework.web.routing.argsresolver.providers.ParamVarProvider;
import mg.razherana.framework.web.routing.argsresolver.providers.PathVarProvider;
import mg.razherana.framework.web.routing.argsresolver.providers.PathVarsProvider;
import mg.razherana.framework.web.routing.argsresolver.providers.RequestBodyProvider;
import mg.razherana.framework.web.routing.argsresolver.providers.ServletContextProvider;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;
import mg.razherana.framework.web.utils.http.ResponseBody;
import mg.razherana.framework.web.utils.json.types.JsonElement;
import mg.razherana.framework.web.utils.jsp.JspFunctionBridge;
import mg.razherana.framework.web.utils.jsp.JspUtil;

public class WebExecutor {
  private static final ArgResolver argResolver = new ArgResolver();

  static {
    // Register default providers
    argResolver.registerProvider(new GiverProvider());
    argResolver.registerProvider(new PathVarProvider());
    argResolver.registerProvider(new HttpServletRequestProvider());
    argResolver.registerProvider(new HttpServletResponseProvider());
    argResolver.registerProvider(new PathVarsProvider());
    argResolver.registerProvider(new ParamVarProvider());
    argResolver.registerProvider(new ServletContextProvider());
    argResolver.registerProvider(new HttpSessionProvider());
    argResolver.registerProvider(new ModelViewProvider());
    argResolver.registerProvider(new RequestBodyProvider());
    argResolver.registerProvider(new ParamBodyProvider());
  }

  public static void sendException(HttpServletRequest request,
      HttpServletResponse response,
      Exception e,
      Map<String, ResponseHandler> respMap) {
    ResponseContainer rc = new ResponseContainer(e, "error");

    String type = rc.getReturnType();
    ResponseHandler responseHandler = respMap.get(type);

    if (responseHandler == null)
      throw new WebExecutionException("No handler found for response type: " + type);

    try {
      responseHandler.handleResponse(rc, request, response);
    } catch (Exception ex) {
      throw new WebExecutionException("Error when handling the error exception", ex);
    }
  }

  public static Object[] resolveArgs(
      WebExecutor webExecutor,
      Method method,
      Map<String, String> pathParameters,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestBody requestBody,
      ModelView mv,
      Map<Class<?>, Giver> givers) throws IOException, ServletException {
    Parameter[] args = method.getParameters();

    Object[] argInstances = new Object[args.length];

    for (int i = 0; i < args.length; i++) {
      Parameter arg = args[i];
      Class<?> argType = arg.getType();

      argInstances[i] = argResolver.resolveArgument(
          webExecutor,
          arg,
          argType,
          method,
          pathParameters,
          request,
          response,
          requestBody,
          mv,
          givers);
    }

    return argInstances;
  }

  private WebRouteContainer webRouteContainer;

  private Map<String, ResponseHandler> responseHandlerMap;

  private final WebMapper webMapper;

  public WebExecutor(WebRouteContainer webRouteContainer, Map<String, ResponseHandler> responseHandlerMap,
      WebMapper webMapper) {
    this.webRouteContainer = webRouteContainer;
    this.responseHandlerMap = responseHandlerMap;
    this.webMapper = webMapper;
  }

  /**
   * @return the webMapper
   */
  public WebMapper getWebMapper() {
    return webMapper;
  }

  public void execute(HttpServletRequest request,
      HttpServletResponse response, App app) throws Exception {
    // Set everything needed in request attributes
    request.setAttribute("app", app);
    request.setAttribute("webMapper", webMapper);
    request.setAttribute("webRouteContainer", webRouteContainer);
    request.setAttribute("webExecutor", this);
    request.setAttribute("response", response);

    // Instanciate JSPUtils instances
    Map<String, Class<? extends JspUtil>> jspUtilMap = JspFunctionBridge.getJspUtilMap();

    JspFunctionBridge jspFunctionBridge = instantiateJspUtils(jspUtilMap, request, response, app);

    request.setAttribute("jspFunctionBridge", jspFunctionBridge);

    Method method = webRouteContainer.getMethod();

    // Decide to get the controller instance
    Object controllerInstance = getControllerInstance(request);

    Map<String, String> pathParameters = webRouteContainer.getPathParameters();

    System.out.println("[Fruits] : Executing method "
        + method.getName() + " of controller "
        + controllerInstance.getClass().getName());

    System.out
        .println("[Fruits] : Path parameters: " + pathParameters);

    RequestBody requestBody = RequestBody.from(request);
    request.setAttribute("requestBody", requestBody);

    ModelView mv = new ModelView(request, response, requestBody, webRouteContainer);

    Map<Class<?>, Middleware> middlewares = initMiddlewares(request, response, mv);
    Map<Class<?>, Giver> givers = initGivers(request, response, mv, pathParameters, requestBody);

    Object[] methodArgs = WebExecutor.resolveArgs(this, method, pathParameters,
        request, response, requestBody, mv, givers);

    Object middlewareBeforeResult = null;

    // Execute middlewares before method execution
    for (Middleware middleware : middlewares.values()) {
      Object beforeResult = middleware.before(request, response, mv);
      middlewareBeforeResult = beforeResult;

      if (middlewareBeforeResult != null)
        break;
    }

    Object responseObject = null;
    ResponseContainer rc = null;

    try {
      if (middlewareBeforeResult != null) {
        responseObject = middlewareBeforeResult;
      } else {
        responseObject = method.invoke(controllerInstance, methodArgs);

        // Execute middlewares after method execution
        for (Middleware middleware : middlewares.values()) {
          Object afterResult = middleware.after(request, response, mv, responseObject);
          if (afterResult != null) {
            responseObject = afterResult;
            break;
          }
        }
      }

      if (method.isAnnotationPresent(JsonUrl.class)) {
        rc = new ResponseContainer(responseObject, "json");
      } else if (responseObject instanceof String) {
        rc = mv.write((String) responseObject);
      } else if (responseObject instanceof ResponseContainer) {
        rc = (ResponseContainer) responseObject;
      } else if (responseObject instanceof JsonElement jsonElement) {
        rc = new ResponseContainer(jsonElement, "json");
      } else if (responseObject instanceof ResponseBody responseBody) {
        rc = new ResponseContainer(responseBody, "json");
      }

      if (rc == null)
        return;

    } catch (Exception e) {
      rc = new ResponseContainer(e, "error");
    }

    String type = rc.getReturnType();
    ResponseHandler responseHandler = responseHandlerMap.get(type);

    if (responseHandler == null)
      throw new WebExecutionException("No handler found for response type: " + type);

    responseHandler.handleResponse(rc, request, response);
  }

  public WebRouteContainer getWebRouteContainer() {
    return webRouteContainer;
  }

  public void setWebRouteContainer(
      WebRouteContainer webRouteContainer) {
    this.webRouteContainer = webRouteContainer;
  }

  public Object getControllerInstance() {
    return webRouteContainer.getControllerInstance();
  }

  private Object getControllerInstance(HttpServletRequest request) {
    // Check if the controller is stateful
    Class<?> controllerClass = webRouteContainer.getControllerClass();

    var stateful = controllerClass.getDeclaredAnnotation(Stateful.class);
    if (stateful != null) {
      // Get the session from the request
      return webMapper.getStatefulInstance(request, webRouteContainer.getControllerContainer());
    }

    if (controllerClass.isAnnotationPresent(Prototype.class)) {
      return WebFinder.instanciateController(controllerClass);
    }

    // Default to singleton instance
    return webRouteContainer.getControllerInstance();
  }

  private JspFunctionBridge instantiateJspUtils(
      Map<String, Class<? extends JspUtil>> jspUtilMap,
      HttpServletRequest request,
      HttpServletResponse response, App app) {
    JspFunctionBridge jspFunctionBridge = new JspFunctionBridge();

    for (String jspUtilViewName : jspUtilMap.keySet()) {
      JspUtil jspUtil;
      try {
        jspUtil = jspUtilMap.get(jspUtilViewName).getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        // Should not happen
        throw new RuntimeException(e);
      }

      jspUtil.getData().put("request", request);
      jspUtil.getData().put("response", response);
      jspUtil.getData().put("app", app);
      jspUtil.getData().put("webExecutor", this);

      jspFunctionBridge.registerFunction(jspUtilViewName, (Object... args) -> jspUtil.run(args));
    }

    return jspFunctionBridge;
  }

  private Map<Class<?>, Giver> initGivers(HttpServletRequest request, HttpServletResponse response,
      ModelView mv, Map<String, String> pathParameters, RequestBody requestBody) throws ServletException {
    Map<Class<?>, Giver> givers = new HashMap<>();

    Method method = getWebRouteContainer().getMethod();

    Parameter[] parameters = method.getParameters();

    for (Parameter parameter : parameters) {
      Class<?> paramType = parameter.getType();

      if (Giver.class.isAssignableFrom(paramType)) {
        Giver giverInstance = GiverMethodInterceptor.getGiverInstance(paramType,
            this,
            pathParameters,
            request,
            response,
            requestBody,
            mv,
            givers);

        giverInstance.init(request, response, mv);

        givers.put(paramType, giverInstance);
      }
    }

    return givers;
  }

  private Map<Class<?>, Middleware> initMiddlewares(HttpServletRequest request,
      HttpServletResponse response, ModelView mv) {
    Map<Class<?>, Middleware> middlewares = new HashMap<>();

    Class<?> controllerClass = getWebRouteContainer().getControllerClass();

    // Get all middlewares for this controller
    Middlewares middlewaresAnnot = controllerClass.getAnnotation(Middlewares.class);

    // Get all middlewares for this method
    Method method = getWebRouteContainer().getMethod();
    Middlewares methodMiddlewaresAnnot = method.getAnnotation(Middlewares.class);

    Class<?>[] middlewareClasses = {};

    if (middlewaresAnnot != null) {
      middlewareClasses = middlewaresAnnot.value();
    }

    if (methodMiddlewaresAnnot != null) {
      Class<?>[] methodMiddlewareClasses = methodMiddlewaresAnnot.value();
      Class<?>[] combined = new Class<?>[middlewareClasses.length + methodMiddlewareClasses.length];
      System.arraycopy(middlewareClasses, 0, combined, 0, middlewareClasses.length);
      System.arraycopy(methodMiddlewareClasses, 0, combined, middlewareClasses.length, methodMiddlewareClasses.length);
      middlewareClasses = combined;
    }

    for (Class<?> middlewareClass : middlewareClasses) {
      Constructor<?> constructor;
      try {
        constructor = middlewareClass.getDeclaredConstructor();
      } catch (NoSuchMethodException e) {
        throw new MalformedWebAnnotationException(
            "Middleware class " + middlewareClass.getName()
                + " must have a constructor with no parameters");
      }

      Middleware middlewareInstance;
      try {
        middlewareInstance = (Middleware) constructor.newInstance();
      } catch (Exception e) {
        throw new WebExecutionException(
            "Error instantiating middleware: " + middlewareClass.getName(), e);
      }

      middlewares.put(middlewareClass, middlewareInstance);
    }

    return middlewares;
  }
}
