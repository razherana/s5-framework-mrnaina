package mg.razherana.framework.scanners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import mg.razherana.framework.web.annotations.Url;

class ScanUrls {
  static List<Method> findUrlMethodsInController(Class<?> clazz) {
    // Get all methods of the class
    Method[] methods = clazz.getDeclaredMethods();
    List<Method> urlMethods = new ArrayList<>();

    // Iterate through methods and check for @Url annotation
    for (Method method : methods)
      if (method.isAnnotationPresent(Url.class))
        urlMethods.add(method);

    return urlMethods;
  }

}
