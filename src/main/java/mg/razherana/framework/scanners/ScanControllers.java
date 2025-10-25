package mg.razherana.framework.scanners;

import java.util.ArrayList;
import java.util.List;
import mg.razherana.framework.annotations.Controller;

public class ScanControllers {
  /**
   * Find all classes annotated with @Controller in the given package and
   * Haricot meta-annotations
   * 
   * @param packageName the package to scan
   * @return Set of classes annotated with @Controller
   */
  public static List<Class<?>> findControllerClasses(String packageName) {
    List<Class<?>> controllerClasses = new ArrayList<>();

    try {
      // Get all classes in the package
      List<Class<?>> classes = ClassScanner.getClassesInPackage(packageName);

      for (Class<?> clazz : classes) {
        if (clazz.isAnnotationPresent(Controller.class)) {
          controllerClasses.add(clazz);
        }
      }
    } catch (Exception e) {
      System.err.println("Error scanning package " + packageName + ": " + e.getMessage());
    }

    return controllerClasses;
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
}
