package mg.razherana.framework.web.routing;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.razherana.framework.web.annotations.parameters.CreateSession;
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

public class WebExecutor {
  private WebRouteContainer webRouteContainer;
  private Map<String, ResponseHandler> responseHandlerMap;

  public WebExecutor(WebRouteContainer webRouteContainer, Map<String, ResponseHandler> responseHandlerMap) {
    this.webRouteContainer = webRouteContainer;
    this.responseHandlerMap = responseHandlerMap;
  }

  public void execute(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
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

    responseHandler.handleResponse(rc, request, response);
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

        if (varValue == null) {
          if (paramVar.required()) {
            // The request object is being stored as additional data in the exception.
            throw new BadRequestException("Missing required parameter: " + varName, request);
          }

          // Use default value
          varValue = paramVar.defaultValue();
        }

        // Convert to appropriate type
        Object convertedValue = ConversionUtils
            .convertStringToType(varValue, argType);

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

  public static void sendException(HttpServletRequest request, HttpServletResponse response, Exception e, Map<String, ResponseHandler> respMap) {
    ResponseContainer rc = new ResponseContainer(e, "error");

    String type = rc.getReturnType();
    ResponseHandler responseHandler = respMap.get(type);

    try {
      responseHandler.handleResponse(rc, request, response);
    } catch (Exception ex) {
      throw new WebExecutionException("Error when handling the error exception : ", ex);
    }
  }
}
