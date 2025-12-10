package mg.razherana.framework.web.routing;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.razherana.framework.App;
import mg.razherana.framework.web.annotations.parameters.CreateSession;
import mg.razherana.framework.web.annotations.parameters.ParamBody;
import mg.razherana.framework.web.annotations.parameters.ParamVar;
import mg.razherana.framework.web.annotations.parameters.PathVar;
import mg.razherana.framework.web.annotations.parameters.PathVars;
import mg.razherana.framework.web.containers.ResponseContainer;
import mg.razherana.framework.web.containers.WebRouteContainer;
import mg.razherana.framework.web.exceptions.MalformedWebAnnotationException;
import mg.razherana.framework.web.exceptions.WebExecutionException;
import mg.razherana.framework.web.exceptions.http.BadRequestException;
import mg.razherana.framework.web.handlers.responses.ResponseHandler;
import mg.razherana.framework.web.utils.ConversionUtils;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.RequestBody;
import mg.razherana.framework.web.utils.ResponseBody;
import mg.razherana.framework.web.utils.json.types.JsonElement;
import mg.razherana.framework.web.utils.jsp.JspFunctionBridge;
import mg.razherana.framework.web.utils.jsp.JspUtil;
import mg.razherana.framework.web.utils.objectconversion.ConversionObjectUtils;

public class WebExecutor {
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

  private WebRouteContainer webRouteContainer;

  private Map<String, ResponseHandler> responseHandlerMap;

  public WebExecutor(WebRouteContainer webRouteContainer, Map<String, ResponseHandler> responseHandlerMap) {
    this.webRouteContainer = webRouteContainer;
    this.responseHandlerMap = responseHandlerMap;
  }

  public void execute(HttpServletRequest request,
      HttpServletResponse response, App app) throws Exception {
    // Instanciate JSPUtils instances
    Map<String, Class<? extends JspUtil>> jspUtilMap = JspFunctionBridge.getJspUtilMap();

    JspFunctionBridge jspFunctionBridge = instantiateJspUtils(jspUtilMap, request, response, app);

    request.setAttribute("jspFunctionBridge", jspFunctionBridge);

    Method method = webRouteContainer.getMethod();
    Object controllerInstance = webRouteContainer
        .getControllerInstance();

    Map<String, String> pathParameters = webRouteContainer.getPathParameters();

    System.out.println("[Fruits] : Executing method "
        + method.getName() + " of controller "
        + controllerInstance.getClass().getName());

    System.out
        .println("[Fruits] : Path parameters: " + pathParameters);

    RequestBody requestBody = RequestBody.from(request);
    request.setAttribute("requestBody", requestBody);

    Object[] methodArgs = resolveMethodArgs(method, pathParameters,
      request, response, requestBody);

    ResponseContainer rc = null;

    try {
      Object responseObject = method.invoke(controllerInstance, methodArgs);

      if (responseObject instanceof String) {
        ModelView mv = new ModelView(request, response);
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

  private Object[] resolveMethodArgs(Method method,
      Map<String, String> pathParameters,
      HttpServletRequest request, HttpServletResponse response,
      RequestBody requestBody) {
    Parameter[] args = method.getParameters();
    Object[] argInstances = new Object[args.length];

    for (int i = 0; i < args.length; i++) {
      Parameter arg = args[i];
      Class<?> argType = arg.getType();

      // Check if annotated with @PathVar
      if (arg.isAnnotationPresent(PathVar.class)) {
        PathVar pathVar = arg.getAnnotation(PathVar.class);
        String varName = pathVar.value();

        String varValue = pathParameters.get(varName);

        // Convert to appropriate type
        Object convertedValue = ConversionUtils
            .convertStringToType(varValue, argType);
        argInstances[i] = convertedValue;
        continue;
      }

      // Check for HttpServletRequest and HttpServletResponse
      if (argType == HttpServletRequest.class) {
        argInstances[i] = request;
        continue;
      }

      if (argType == HttpServletResponse.class) {
        argInstances[i] = response;
        continue;
      }

      // Check for @PathVars for path parameters
      if (arg.isAnnotationPresent(PathVars.class)) {
        // Check if the type is Map<String, String>
        if (argType == Map.class
            && arg.getParameterizedType().getTypeName().equals(
                "java.util.Map<java.lang.String, java.lang.String>")) {
          argInstances[i] = pathParameters;
          continue;
        }

        // If not the correct type, throw an exception
        throw new MalformedWebAnnotationException(
            "@PathVars can only be applied to parameters of type Map<String, String> in method: "
                + method.getName());
      }

      // Check for @ParamVar
      if (arg.isAnnotationPresent(ParamVar.class)) {
        ParamVar paramVar = arg.getAnnotation(ParamVar.class);
        String varName = paramVar.value();

        Object rawValue = requestBody.get(varName);

        if (rawValue == null) {
          if (paramVar.required()) {
            // The request object is being stored as additional data in the exception.
            throw new BadRequestException("Missing required parameter: " + varName, request);
          }

          // Use default value
          rawValue = paramVar.defaultValue();
        } else if (paramVar.forceString() && rawValue instanceof String[] values) {
          rawValue = values.length > 0 ? values[0] : "";
        }

        try {
          Object convertedValue = ConversionUtils
              .convertStringOrArrToType(rawValue, argType, getControllerInstance());

          argInstances[i] = convertedValue;
        } catch (IllegalArgumentException e) {
          throw new BadRequestException("Type mismatch for parameter: " + varName, request);
        }
        continue;
      }

      // Check if ServletContext
      if (argType.equals(ServletContext.class)) {
        argInstances[i] = request.getServletContext();
        continue;
      }

      // Check if HttpSession
      if (argType.equals(HttpSession.class)) {
        argInstances[i] = request
            .getSession(arg.isAnnotationPresent(CreateSession.class));
        continue;
      }

      // Check if ModelView
      if (argType.equals(ModelView.class)) {
        argInstances[i] = new ModelView(request, response);
        continue;
      }

      // Check if ParamBody
      if (arg.isAnnotationPresent(ParamBody.class)) {
        Map<String, Object> bodyMap = requestBody.asMap();

        if (argType == RequestBody.class || RequestBody.class.isAssignableFrom(argType)) {
          argInstances[i] = requestBody;
          continue;
        }

        if (argType == Map.class
            && arg.getParameterizedType().getTypeName().equals(
                "java.util.Map<java.lang.String, java.lang.Object>")) {
          argInstances[i] = new java.util.HashMap<>(bodyMap);
          continue;
        }

        if (argType == JsonElement.class || JsonElement.class.isAssignableFrom(argType)) {
          var jsonElement = requestBody.getJsonElement().orElse(null);

          if (jsonElement == null) {
            throw new BadRequestException("Request body is not JSON", request);
          }

          argInstances[i] = jsonElement;
          continue;
        }

        Object convertedObject = ConversionObjectUtils
            .convertMapToObject(bodyMap, arg.getType(), getControllerInstance());

        argInstances[i] = convertedObject;
        continue;
      }

      // Throw exception for unsupported parameter types
      throw new MalformedWebAnnotationException(
          "Unsupported parameter type: " + argType.getName()
              + " in method: " + method.getName());

    }

    return argInstances;
  }
}
