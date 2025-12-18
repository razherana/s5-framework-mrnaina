package mg.razherana.framework.web.containers;

import java.lang.reflect.Method;
import java.util.HashMap;

public class WebRouteContainer {
  private Method method;
  private HashMap<String, String> pathParameters;
  private Object controllerInstance;
  private Class<?> controllerClass;
  private ControllerContainer controllerContainer;

  public WebRouteContainer(Method method, Object controllerInstance, HashMap<String, String> pathParameters, ControllerContainer controllerContainer) {
    this.method = method;
    this.pathParameters = pathParameters;
    this.controllerInstance = controllerInstance;
    this.controllerContainer = controllerContainer;
    this.controllerClass = controllerInstance.getClass();
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

  /**
   * @return the controllerClass
   */
  public Class<?> getControllerClass() {
    return controllerClass;
  }

  /**
   * @return the controllerContainer
   */
  public ControllerContainer getControllerContainer() {
    return controllerContainer;
  }
}
