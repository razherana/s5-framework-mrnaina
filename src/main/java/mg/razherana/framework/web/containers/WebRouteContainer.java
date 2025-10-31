package mg.razherana.framework.web.containers;

import java.lang.reflect.Method;
import java.util.HashMap;

public class WebRouteContainer {
  private Method method;
  private HashMap<String, String> pathParameters;
  private Object controllerInstance;

  public WebRouteContainer(Method method, Object controllerInstance) {
    this.method = method;
    this.pathParameters = new HashMap<>();
    this.controllerInstance = controllerInstance;
  }

  public WebRouteContainer(Method method, Object controllerInstance, HashMap<String, String> pathParameters) {
    this.method = method;
    this.pathParameters = pathParameters;
    this.controllerInstance = controllerInstance;
  }

  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public HashMap<String, String> getPathParameters() {
    return pathParameters;
  }

  public void setPathParameters(HashMap<String, String> pathParameters) {
    this.pathParameters = pathParameters;
  }

  public Object getControllerInstance() {
    return controllerInstance;
  }

  public void setControllerInstance(Object controllerInstance) {
    this.controllerInstance = controllerInstance;
  }
}
