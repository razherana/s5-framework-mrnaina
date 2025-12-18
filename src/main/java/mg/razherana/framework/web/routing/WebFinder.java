package mg.razherana.framework.web.routing;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mg.razherana.framework.web.annotations.Controller;
import mg.razherana.framework.web.annotations.Url;
import mg.razherana.framework.web.containers.ControllerContainer;
import mg.razherana.framework.web.containers.RoutingContainer;
import mg.razherana.framework.web.exceptions.WebMappingException;

public final class WebFinder {
  private List<ControllerContainer> controllerContainers;

  public WebFinder(Map<Class<?>, List<Method>> controllerMethods) {
    initFromControllerMethods(controllerMethods);
  }

  public List<ControllerContainer> getControllerContainers() {
    return this.controllerContainers;
  }

  public static Object instanciateController(Class<?> controllerClass) {
    Object controllerInstance = null;
    // Check if record or not a class
    if (controllerClass.isRecord())
      throw new WebMappingException(
          "The controller " + controllerClass + " must be a normal class. A record is not supported.");

    if (controllerClass.isEnum())
      throw new WebMappingException(
          "The controller " + controllerClass + " must be a normal class. An enum is not supported.");

    try {
      controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
    } catch (NoSuchMethodException e) {
      throw new WebMappingException(
          "The controller " + controllerClass + " has not empty args constructor. Please make one.", e);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return controllerInstance;
  }

  private void initFromControllerMethods(Map<Class<?>, List<Method>> controllerMethods) {
    this.controllerContainers = new ArrayList<>();

    for (Class<?> controllerClass : controllerMethods.keySet()) {
      Object controllerInstance = instanciateController(controllerClass);

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
