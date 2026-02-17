package mg.razherana.framework.scanners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.razherana.framework.web.annotations.Url;
import mg.razherana.framework.web.utils.ReflectUtils;

class ScanUrls {
  static List<Method> findUrlMethodsInController(Class<?> clazz) {
    List<Method> allMethods = ReflectUtils.getAllMethods(clazz, Object.class);
    Map<Integer, Method> methodMap = new HashMap<>();

    // Process in reverse order (subclasses first)
    List<Method> reversed = new ArrayList<>(allMethods);
    Collections.reverse(reversed);

    for (Method method : reversed) {
      if (!method.isAnnotationPresent(Url.class)) {
        continue;
      }

      // Create a hash based on method signature that accounts for overrides
      int methodHash = getMethodOverrideHash(method);

      // Only keep the first (most specific) occurrence
      methodMap.putIfAbsent(methodHash, method);
    }

    return new ArrayList<>(methodMap.values());
  }

  private static int getMethodOverrideHash(Method method) {
    // This creates a hash that will be the same for overriding methods
    int result = method.getName().hashCode();
    for (Class<?> paramType : method.getParameterTypes()) {
      result = 31 * result + paramType.getName().hashCode();
    }
    return result;
  }

}
