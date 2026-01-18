package mg.razherana.framework.web.routing;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.razherana.framework.web.annotations.Controller;
import mg.razherana.framework.web.annotations.Url;
import mg.razherana.framework.web.containers.ControllerContainer;
import mg.razherana.framework.web.containers.GiverContainer;
import mg.razherana.framework.web.containers.RoutingContainer;
import mg.razherana.framework.web.exceptions.WebMappingException;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.givers.annotations.Impl;
import mg.razherana.framework.web.givers.annotations.Resolve;

public final class WebFinder {
  private List<ControllerContainer> controllerContainers;

  private Map<Class<?>, List<GiverContainer>> giverContainers;

  public WebFinder(Map<Class<?>, List<Method>> controllerMethods, List<Class<?>> giverClasses) {
    initFromControllerMethods(controllerMethods);
    initGivers(giverClasses);
  }

  public List<ControllerContainer> getControllerContainers() {
    return this.controllerContainers;
  }

  public Map<Class<?>, List<GiverContainer>> getGiverContainers() {
    return this.giverContainers;
  }

  private void initGivers(List<Class<?>> giverClasses) {
    giverContainers = new HashMap<>();

    for (Class<?> giverClass : giverClasses) {
      // Check if record or not a class
      if (giverClass.isRecord())
        throw new WebMappingException(
            "The giver " + giverClass + " must be a normal class. A record is not supported.");

      if (giverClass.isEnum())
        throw new WebMappingException(
            "The giver " + giverClass + " must be a normal class. An enum is not supported.");

      if (!Giver.class.isAssignableFrom(giverClass))
        throw new WebMappingException(
            "The giver " + giverClass + " must implement the Giver interface.");

      if (!Modifier.isAbstract(giverClass.getModifiers()))
        throw new WebMappingException(
            "The giver " + giverClass + " must be an abstract class.");

      // Check if it has a no-args constructor
      try {
        giverClass.getDeclaredConstructor();
      } catch (NoSuchMethodException e) {
        throw new WebMappingException(
            "The giver " + giverClass + " has not empty args constructor. Please make one.", e);
      }

      // Then get all the methods
      Method[] methods = giverClass.getDeclaredMethods();
      Map<String, Method> implMethods = new HashMap<>();
      Map<String, Method> resolveMethods = new HashMap<>();

      for (Method method : methods) {
        // Check if Impl
        Impl implAnnotation = method.getAnnotation(Impl.class);
        if (implAnnotation != null) {
          // Check first if not abstract
          if (Modifier.isAbstract(method.getModifiers())) {
            throw new WebMappingException(
                "The @Impl method " + method.getName() + " in giver " + giverClass
                    + " must not be abstract.");
          }

          String name = implAnnotation.value().isEmpty() ? method.getName() : implAnnotation.value();
          if (implMethods.containsKey(name)) {
            throw new WebMappingException(
                "Duplicate @Impl name or alias '" + name + "' found in giver " + giverClass +
                    ". Each @Impl method must have a unique name. The method " + method + " is a duplicate to "
                    + implMethods.get(name) + ".");
          }

          implMethods.put(name, method);
        }

        // Check if Resolve
        Resolve resolveAnnotation = method.getAnnotation(Resolve.class);
        if (resolveAnnotation != null) {
          // Check first if abstract
          if (!Modifier.isAbstract(method.getModifiers())) {
            throw new WebMappingException(
                "The @Resolve method " + method.getName() + " in giver " + giverClass
                    + " must be abstract.");
          }

          // Check if there are parameters to the @Resolve method
          if (method.getParameterCount() != 0) {
            throw new WebMappingException(
                "The @Resolve method " + method.getName() + " in giver " + giverClass
                    + " must not have parameters.");
          }

          String name = resolveAnnotation.value().isEmpty() ? method.getName() : resolveAnnotation.value();

          // We do not check for duplicate resolve names here because
          // a resolve can map to multiple impls (overloading)

          resolveMethods.put(name, method);
        }
      }

      // Now match impls and resolves
      for (String resolveName : resolveMethods.keySet()) {
        if (!implMethods.containsKey(resolveName)) {
          throw new WebMappingException(
              "The @Resolve name or alias '" + resolveName + "' in giver " + giverClass
                  + " does not have a corresponding @Impl method.");
        }

        Method implMethod = implMethods.get(resolveName);
        Method resolveMethod = resolveMethods.get(resolveName);

        // Check return types
        if (!resolveMethod.getReturnType().isAssignableFrom(implMethod.getReturnType())) {
          throw new WebMappingException(
              "The return type of @Impl method " + implMethod + " does not match the return type of @Resolve method "
                  + resolveMethod + " in giver " + giverClass + ".");
        }

        GiverContainer giverContainer = new GiverContainer(
            implMethod,
            implMethod.getAnnotation(Impl.class),
            resolveMethod,
            resolveMethod.getAnnotation(Resolve.class),
            giverClass);

        giverContainers.computeIfAbsent(giverClass, k -> new ArrayList<>()).add(giverContainer);
      }
    }
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
      throw new WebMappingException(
          "Failed to instantiate controller " + controllerClass
              + ". Ensure it has an accessible no-args constructor and that its initialization does not fail.",
          e);
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
