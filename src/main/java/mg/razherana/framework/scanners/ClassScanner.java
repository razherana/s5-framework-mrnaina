package mg.razherana.framework.scanners;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class ClassScanner {
  /**
   * Get all classes in a package
   * 
   * @param packageName the package name
   * @return List of classes in the package
   */
  public static List<Class<?>> getClassesInPackage(String packageName) {
    List<Class<?>> classes = new ArrayList<>();

    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String path = packageName.replace('.', '/');
      URL resource = classLoader.getResource(path);

      if (resource != null) {
        File directory = new File(resource.getFile());
        if (directory.exists()) {
          scanDirectory(directory, packageName, classes);
        }
      }
    } catch (Exception e) {
      System.err.println("Error loading classes from package " + packageName + ": " + e.getMessage());
    }

    return classes;
  }

  /**
   * Recursively scan directory for class files
   * 
   * @param directory   the directory to scan
   * @param packageName the current package name
   * @param classes     the list to add found classes to
   */
  private static void scanDirectory(File directory, String packageName, List<Class<?>> classes) {
    File[] files = directory.listFiles();

    if (files == null)
      return;

    for (File file : files) {
      if (file.isDirectory()) {
        // Recursively scan subdirectories
        String subPackage = packageName + "." + file.getName();
        scanDirectory(file, subPackage, classes);
      } else if (file.getName().endsWith(".class")) {
        // Load class file
        String className = packageName + "." + file.getName().replace(".class", "");
        try {
          Class<?> clazz = Class.forName(className);
          classes.add(clazz);
        } catch (ClassNotFoundException e) {
          // Skip classes that can't be loaded
          System.err.println("Could not load class: " + className);
        }
      }
    }

  }
}
