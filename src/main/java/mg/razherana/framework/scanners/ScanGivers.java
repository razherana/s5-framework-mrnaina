package mg.razherana.framework.scanners;

import java.util.ArrayList;
import java.util.List;

import mg.razherana.framework.web.givers.Giver;

public class ScanGivers {
  private static List<Class<?>> giverClasses = null;

  public static List<Class<?>> findGiverClasses(String packageName) {
    if (giverClasses != null)
      return giverClasses;

    giverClasses = new ArrayList<>();

    try {
      // Get all classes in the package
      List<Class<?>> classes = ClassScanner.getClassesInPackage(packageName);

      for (Class<?> clazz : classes)
        if (Giver.class.isAssignableFrom(clazz) && !clazz.equals(Giver.class)) {
          giverClasses.add(clazz);
        }
    } catch (Exception e) {
      System.err.println("Error scanning package " + packageName + ": " + e.getMessage());
    }

    return giverClasses;
  }
}
