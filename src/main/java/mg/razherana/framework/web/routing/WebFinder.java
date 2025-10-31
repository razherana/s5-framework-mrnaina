package mg.razherana.framework.web.routing;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mg.razherana.framework.web.annotations.Controller;
import mg.razherana.framework.web.annotations.Url;
import mg.razherana.framework.web.containers.ControllerContainer;
import mg.razherana.framework.web.containers.RoutingContainer;

public class WebFinder {
  private List<ControllerContainer> controllerContainers;

  public WebFinder(Map<Class<?>, List<Method>> controllerMethods) {
    initFromControllerMethods(controllerMethods);

  }

  public List<ControllerContainer> getControllerContainers() {
    return this.controllerContainers;
  }

  private void initFromControllerMethods(Map<Class<?>, List<Method>> controllerMethods) {
    this.controllerContainers = new ArrayList<>();
    
    for (Class<?> controllerClass : controllerMethods.keySet()) {
      Object controllerInstance = null;
      try {
        controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        e.printStackTrace();
      }

      List<RoutingContainer> routingContainers = new ArrayList<>();

      ControllerContainer controllerContainer = new ControllerContainer(
          controllerClass,
          controllerInstance,
          routingContainers,
          controllerClass.getAnnotation(Controller.class));

      for (Method urlMethod : controllerMethods.get(controllerClass)) {
        Url urlAnnot = urlMethod.getAnnotation(Url.class);

        RoutingContainer routingContainer = new RoutingContainer(
            urlAnnot.method(),
            urlAnnot.value(),
            controllerContainer,
            urlAnnot,
            urlMethod);

        routingContainers.add(routingContainer);
      }

      controllerContainers.add(controllerContainer);
    }
  }
}
