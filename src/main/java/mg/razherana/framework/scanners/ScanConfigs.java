package mg.razherana.framework.scanners;

import java.util.ArrayList;
import java.util.List;

import mg.razherana.framework.configs.AppConfig;

public class ScanConfigs {
  private static List<Class<?>> configClasses = null;

  public static List<Class<?>> findConfigClasses(String packageName) {
    if (configClasses != null)
      return configClasses;

    configClasses = new ArrayList<>();

    try {
      List<Class<?>> classes = ClassScanner.getClassesInPackage(packageName);

      for (Class<?> clazz : classes) {
        if (clazz.isAnnotationPresent(AppConfig.class)) {
          configClasses.add(clazz);
        }
      }
    } catch (Exception e) {
      System.err.println("Error scanning package " + packageName + ": " + e.getMessage());
    }

    return configClasses;
  }
}
