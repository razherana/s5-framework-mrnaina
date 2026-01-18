package mg.razherana.framework.scanners;

import java.util.ArrayList;
import java.util.List;

import mg.razherana.framework.App;

public class ScanResolvs {
  private static List<Class<?>>[] resolvClasses = null;

  @SuppressWarnings("unchecked")
  public static List<Class<?>>[] findResolvClasses(String packageName, Class<?>[] resolvToFind) {
    if (resolvClasses != null)
      return resolvClasses;

    resolvClasses = (List<Class<?>>[]) new ArrayList[resolvToFind.length];

    for (int i = 0; i < resolvToFind.length; i++) {
      resolvClasses[i] = new ArrayList<>();
    }

    try {
      // Get all classes in the package
      List<Class<?>> classes = ClassScanner.getClassesInPackage(packageName);
      classes.addAll(App.FRAMEWORK_LOADED_RESOLV_CLASSES);

      for (Class<?> clazz : classes)
        for (int i = 0; i < resolvToFind.length; i++) {
          Class<?> originResolv = resolvToFind[i];
          if (originResolv.isAssignableFrom(clazz) && !clazz.equals(originResolv)) {
            resolvClasses[i].add(clazz);
          }
        }
    } catch (Exception e) {
      System.err.println("Error scanning package " + packageName + ": " + e.getMessage());
    }

    return resolvClasses;
  }
}
