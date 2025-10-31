package mg.razherana.framework.web.containers;

import java.util.List;

import mg.razherana.framework.web.annotations.Controller;

public class ControllerContainer {
  private Class<?> controllerClass;
  private Controller controllerAnnotation;
  private Object controllerInstance;
  private List<RoutingContainer> routingContainers;

  public ControllerContainer(Class<?> controllerClass, Object controllerInstance,
      List<RoutingContainer> routingContainers, Controller controllerAnnotation) {
    this.controllerClass = controllerClass;
    this.controllerInstance = controllerInstance;
    this.routingContainers = routingContainers;
    this.controllerAnnotation = controllerAnnotation;
  }

  public Controller getControllerAnnotation() {
    return controllerAnnotation;
  }

  public void setControllerAnnotation(Controller controllerAnnotation) {
    this.controllerAnnotation = controllerAnnotation;
  }

  public Class<?> getControllerClass() {
    return controllerClass;
  }

  public void setControllerClass(Class<?> controllerClass) {
    this.controllerClass = controllerClass;
  }

  public Object getControllerInstance() {
    return controllerInstance;
  }

  public void setControllerInstance(Object controllerInstance) {
    this.controllerInstance = controllerInstance;
  }

  public List<RoutingContainer> getRoutingContainers() {
    return routingContainers;
  }

  public void setRoutingContainers(List<RoutingContainer> routingContainers) {
    this.routingContainers = routingContainers;
  }

  @Override
  public String toString() {
    return "ControllerContainer [controllerClass=" + controllerClass + ", controllerAnnotation=" + controllerAnnotation
        + ", controllerInstance=" + controllerInstance + ", routingContainers=" + routingContainers + "]";
  }

  
}
