package mg.razherana.framework.web.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectUtils {
  public static List<Method> getAllMethods(Class<?> clazz, Class<?> stopAt) {
    List<Method> methods = new ArrayList<>();

    while (clazz != null && clazz != stopAt) {
      for (Method method : clazz.getDeclaredMethods())
        methods.add(method);

      clazz = clazz.getSuperclass();
    }

    return methods;
  }

  private static final Map<Class<?>, Class<?>> mapOfYoungest = new HashMap<>();

  public static Class<?> getYoungestChildClass(Class<?> parentClass, Iterable<Class<?>> classes) {
    Class<?> youngestChild = null;

    if (mapOfYoungest.containsKey(parentClass))
      return mapOfYoungest.get(parentClass);

    // Classes are always linear, so only one child can be the youngest

    for (Class<?> clazz : classes)
      if (parentClass.isAssignableFrom(clazz) && !parentClass.equals(clazz))
        if (youngestChild == null || (youngestChild.isAssignableFrom(clazz) && !clazz.equals(youngestChild)))
          youngestChild = clazz;

    if (youngestChild == null)
      // No child found, return the parent itself
      youngestChild = parentClass;

    mapOfYoungest.put(parentClass, youngestChild);

    return youngestChild;
  }
}
