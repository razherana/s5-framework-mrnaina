package mg.razherana.framework.web.routing;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
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
import mg.razherana.framework.web.utils.jsp.JspFunctionBridge;
import mg.razherana.framework.web.utils.jsp.JspUtil;
import mg.razherana.framework.web.utils.objectconversion.ConversionObjectUtils;

public class WebExecutor {
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

    Object[] methodArgs = resolveMethodArgs(method, pathParameters,
        request, response);

    ResponseContainer rc = null;

    try {
      Object responseObject = method.invoke(controllerInstance, methodArgs);

      if (responseObject instanceof String) {
        ModelView mv = new ModelView(request, response);
        rc = mv.write((String) responseObject);
      } else if (responseObject instanceof ResponseContainer) {
        rc = (ResponseContainer) responseObject;
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
      HttpServletRequest request, HttpServletResponse response) {
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

        String varValue = request.getParameter(varName);
        String[] varValues = request.getParameterValues(varName);

        Object varValueObj = null;

        if (arg.getType().isArray() && !paramVar.forceString()) {
          varValueObj = varValues;
        } else {
          varValueObj = varValue;
        }

        if (varValueObj == null) {
          if (paramVar.required()) {
            // The request object is being stored as additional data in the exception.
            throw new BadRequestException("Missing required parameter: " + varName, request);
          }

          // Use default value
          varValueObj = paramVar.defaultValue();
        }

        // Convert to appropriate type
        Object convertedValue = ConversionUtils
            .convertStringOrArrToType(varValueObj, argType);

        argInstances[i] = convertedValue;
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
        System.out.println("[Fruits] : Type of @ParamBody is " + arg.getParameterizedType().getTypeName() + " - "
            + arg.getParameterizedType());

        Object returnObject = null;
        Map<String, Object> paramBody = new HashMap<>();

        var normalParamNames = request.getParameterNames();

        normalParamNames.asIterator().forEachRemaining((e) -> {
          var parameterValue = request.getParameter(e);
          var parameterValues = request.getParameterValues(e);

          System.out.println("[Fruits] : Name of parameter is " + e);
          System.out.println("[Fruits] : parameterValue is " + parameterValue);
          System.out.println("[Fruits] : parameterValues is "
              + (parameterValues == null ? "Tsisy" : Arrays.deepToString(parameterValues)));

          Object resultObject = null;

          if (parameterValue == null && parameterValues == null)
            return;

          if (parameterValues != null)
            resultObject = parameterValues;
          else if (parameterValue != null && !parameterValue.isBlank())
            resultObject = parameterValue;
          else
            return;

          if (parameterValues.length == 1 && !e.endsWith("[]"))
            resultObject = parameterValue;

          paramBody.put(e, resultObject);
        });

        // Check param type if Map
        if (argType == Map.class
            && arg.getParameterizedType().getTypeName().equals(
                "java.util.Map<java.lang.String, java.lang.Object>")) {
          System.out.println("[Fruits] : Returning param body as Map<String, Object>");
          returnObject = paramBody;
        }

        // Else, try to convert to the appropriate object
        else {
          System.out.println("[Fruits] : Converting param body to object of type "
              + arg.getType().getName());
          returnObject = ConversionObjectUtils
              .convertMapToObject(paramBody, arg.getType(), getControllerInstance());
        }

        argInstances[i] = returnObject;
        continue;
      }

      // Throw exception for unsupported parameter types
      throw new MalformedWebAnnotationException(
          "Unsupported parameter type: " + argType.getName()
              + " in method: " + method.getName());

    }

    return argInstances;
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
}
