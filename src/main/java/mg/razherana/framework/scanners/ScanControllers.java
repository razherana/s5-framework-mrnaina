package mg.razherana.framework.scanners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.razherana.framework.exceptions.ControllerInitException;
import mg.razherana.framework.web.annotations.Controller;

public class ScanControllers {

  private static List<Class<?>> controllerClasses = null;
  private static Map<Class<?>, List<Method>> controllerUrlMethods = null;

  /**
   * Find all classes annotated with @Controller in the given package and
   * Haricot meta-annotations
   * 
   * @param packageName the package to scan
   * @return Set of classes annotated with @Controller
   */
  public static List<Class<?>> findControllerClasses(String packageName) {
    if (controllerClasses != null)
      return controllerClasses;

    controllerClasses = new ArrayList<>();
    controllerUrlMethods = new HashMap<>();

    try {
      // Get all classes in the package
      List<Class<?>> classes = ClassScanner.getClassesInPackage(packageName);

      for (Class<?> clazz : classes)
        if (clazz.isAnnotationPresent(Controller.class)) {
          controllerClasses.add(clazz);

          // Scan for methods annotated with @Url
          List<Method> urlMethods = ScanUrls.findUrlMethodsInController(clazz);
          System.out.println("Found " + urlMethods.size() + " @Url methods in controller " + clazz.getName());

          controllerUrlMethods.put(clazz, urlMethods);
        }
    } catch (Exception e) {
      System.err.println("Error scanning package " + packageName + ": " + e.getMessage());
    }

    return controllerClasses;
  }

  public static List<Method> getControllerUrlMethods(Class<?> controllerClass) {
    if (controllerUrlMethods == null)
      throw new ControllerInitException("Controllers have not been scanned yet", null);

    List<Method> controller = controllerUrlMethods.get(controllerClass);

    if (controller == null)
      throw new ControllerInitException("Controller does not exist", controllerClass);

    return controller;
  }

  /**
   * Print information about all found Haricot classes
   * 
   * @param packageName the package to scan
   */
  public static void printControllers(String packageName) {
    List<Class<?>> controllerClasses = findControllerClasses(packageName);

    System.out.println("Found " + controllerClasses.size() + " Controller classes:");
    for (Class<?> clazz : controllerClasses) {
      System.out.println("- " + clazz.getName());
    }
  }

  public static Map<Class<?>, List<Method>> getControllerUrlsMethod() {
    return controllerUrlMethods;
  }

}
