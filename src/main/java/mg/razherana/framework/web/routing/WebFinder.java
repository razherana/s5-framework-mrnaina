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
import mg.razherana.framework.web.containers.ResolvContainer;
import mg.razherana.framework.web.containers.RoutingContainer;
import mg.razherana.framework.web.exceptions.WebMappingException;
import mg.razherana.framework.web.utils.ReflectUtils;
import mg.razherana.framework.web.utils.proxies.annotations.Impl;
import mg.razherana.framework.web.utils.proxies.annotations.Resolve;

public final class WebFinder {
  private List<ControllerContainer> controllerContainers;

  private Map<Class<?>, List<ResolvContainer>> resolvContainers;

  public WebFinder(Map<Class<?>, List<Method>> controllerMethods, List<Class<?>>[] resolvClasses,
      Class<?>[] originClasses) {
    initFromControllerMethods(controllerMethods);

    if (resolvClasses.length != originClasses.length)
      throw new WebMappingException(
          "The number of resolv classes and origin classes must be the same.");

    for (int i = 0; i < resolvClasses.length; i++) {
      initResolvs(resolvClasses[i], originClasses[i]);
    }
  }

  public List<ControllerContainer> getControllerContainers() {
    return this.controllerContainers;
  }

  public Map<Class<?>, List<ResolvContainer>> getResolvContainers() {
    return this.resolvContainers;
  }

  private void initResolvs(List<Class<?>> resolvClasses, Class<?> originClass) {
    if (resolvClasses == null)
      resolvClasses = new ArrayList<>();

    if (resolvContainers == null)
      resolvContainers = new HashMap<>();

    System.out.println("[Fruits] : Initializing resolvs for " + originClass.getName() + " with "
        + resolvClasses);

    for (Class<?> resolvClass : resolvClasses) {
      // Check if record or not a class
      if (resolvClass.isRecord())
        throw new WebMappingException(
            "The resolv " + resolvClass + " must be a normal class. A record is not supported.");

      if (resolvClass.isEnum())
        throw new WebMappingException(
            "The resolv " + resolvClass + " must be a normal class. An enum is not supported.");

      if (!originClass.isAssignableFrom(resolvClass))
        throw new WebMappingException(
            "The resolv " + resolvClass + " must implement the " + originClass + " interface.");

      if (!Modifier.isAbstract(resolvClass.getModifiers()))
        throw new WebMappingException(
            "The resolv " + resolvClass + " must be an abstract class.");

      // Check if it has a no-args constructor
      try {
        resolvClass.getDeclaredConstructor();
      } catch (NoSuchMethodException e) {
        throw new WebMappingException(
            "The resolv " + resolvClass + " has not empty args constructor. Please make one.", e);
      }

      // Then get all the methods
      List<Method> methods = ReflectUtils.getAllMethods(resolvClass, originClass);
      Map<String, Method> implMethods = new HashMap<>();
      Map<String, Method> resolveMethods = new HashMap<>();

      for (Method method : methods) {
        // Check if Impl
        Impl implAnnotation = method.getAnnotation(Impl.class);
        if (implAnnotation != null) {
          // Check first if not abstract
          if (Modifier.isAbstract(method.getModifiers())) {
            throw new WebMappingException(
                "The @Impl method " + method.getName() + " in resolv " + resolvClass
                    + " must not be abstract.");
          }

          String name = implAnnotation.value().isEmpty() ? method.getName() : implAnnotation.value();
          if (implMethods.containsKey(name)) {
            // Get the method from the youngest class
            Method existingMethod = implMethods.get(name);

            System.out.println("[Fruits] : Checking existing method " + existingMethod + " and new method " + method);
            System.out.println("[Fruits] : Existing method class " + existingMethod.getDeclaringClass().getName()
                + ", new method class " + method.getDeclaringClass().getName());

            System.out.println("[Fruits] : Is existing method class assignable from new method class? "
                + existingMethod.getDeclaringClass().isAssignableFrom(method.getDeclaringClass()));

            if (existingMethod.getDeclaringClass().isAssignableFrom(method.getDeclaringClass()) &&
                !existingMethod.getDeclaringClass().equals(method.getDeclaringClass())) {
              // Replace with the new method
              implMethods.put(name, method);
              continue;
            }

            if (existingMethod.getDeclaringClass().equals(method.getDeclaringClass()))
              throw new WebMappingException(
                  "Duplicate @Impl name or alias '" + name + "' found in resolv " + resolvClass +
                      ". Each @Impl method must have a unique name. The method " + method + " is a duplicate to "
                      + implMethods.get(name) + ".");

            // Else we keep the existing method
          }

          implMethods.put(name, method);
        }

        // Check if Resolve
        Resolve resolveAnnotation = method.getAnnotation(Resolve.class);
        if (resolveAnnotation != null) {
          // Check first if abstract
          if (!Modifier.isAbstract(method.getModifiers())) {
            throw new WebMappingException(
                "The @Resolve method " + method.getName() + " in resolv " + resolvClass
                    + " must be abstract.");
          }

          // Check if there are parameters to the @Resolve method
          if (method.getParameterCount() != 0) {
            throw new WebMappingException(
                "The @Resolve method " + method.getName() + " in resolv " + resolvClass
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
              "The @Resolve name or alias '" + resolveName + "' in resolv class " + resolvClass
                  + " does not have a corresponding @Impl method. Please make a method with an annotation @Impl(\""
                  + resolveName + "\").");
        }

        Method implMethod = implMethods.get(resolveName);
        Method resolveMethod = resolveMethods.get(resolveName);

        // Check return types
        if (!resolveMethod.getReturnType().isAssignableFrom(implMethod.getReturnType())) {
          throw new WebMappingException(
              "The return type of @Impl method " + implMethod + " does not match the return type of @Resolve method "
                  + resolveMethod + " in resolv class " + resolvClass + ".");
        }

        ResolvContainer resolvContainer = new ResolvContainer(
            implMethod,
            implMethod.getAnnotation(Impl.class),
            resolveMethod,
            resolveMethod.getAnnotation(Resolve.class),
            resolvClass);

        if (!resolvContainers.containsKey(resolvClass))
          resolvContainers.put(resolvClass, new ArrayList<>());

        resolvContainers.get(resolvClass).add(resolvContainer);

        System.out.println("[Fruits] : Registered resolv container " + resolvContainer.getResolveMethod() + " - " + resolvContainer.getImplMethod() + " for resolv "
            + resolvClass.getName());
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
