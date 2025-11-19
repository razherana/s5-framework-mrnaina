package mg.razherana.framework.web.utils.jsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import jakarta.servlet.ServletContext;

/**
 * Acts as a central dispatcher that exposes JSP utilities as plain function
 * calls
 * inside JSP pages. Utilities are registered via
 * {@link JspUtil#registerJspUtil}
 * and can then be invoked by name through this bridge.
 */
public final class JspFunctionBridge {

  private static final Set<String> REGISTERED_VIEW_NAMES = new HashSet<>();
  private static final Map<String, Class<? extends JspUtil>> JSP_UTILS = new HashMap<>();

  public static void registerJspUtil(List<String> defaultJspUtilClass, List<String> jspUtilClasses,
      ServletContext servletContext) {

    List<String> completeList = new ArrayList<>(defaultJspUtilClass);
    completeList.addAll(jspUtilClasses);

    for (String jspUtilClassName : completeList) {
      System.out.println("[Fruits] : Registering JSP Util: " + jspUtilClassName);

      // Check and instantiate
      Class<? extends JspUtil> jspUtilClass;

      try {
        @SuppressWarnings("unchecked")
        Class<? extends JspUtil> clazz = (Class<? extends JspUtil>) Class.forName(jspUtilClassName);
        jspUtilClass = clazz;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      JspUtil jspUtil = createInstanceJspUtil(jspUtilClass);

      String jspViewName = jspUtil.getViewName();

      JSP_UTILS.put(jspViewName, jspUtilClass);

      REGISTERED_VIEW_NAMES.add(jspViewName);
    }
  }

  public static Map<String, Class<? extends JspUtil>> getJspUtilMap() {
    return JSP_UTILS;
  }

  private static JspUtil createInstanceJspUtil(Class<?> jspUtilClass) {
    if (jspUtilClass == null) {
      throw new IllegalArgumentException("jspUtilClass cannot be null");
    }

    if (!JspUtil.class.isAssignableFrom(jspUtilClass)) {
      throw new IllegalArgumentException("jspUtilClass must be a subclass of JspUtil");
    }

    // Check if the class has a public no-arg constructor
    try {
      return (JspUtil) jspUtilClass.getDeclaredConstructor().newInstance();
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
          "jspUtilClass must have a public no-argument constructor");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Set<String> getRegisteredViewNames() {
    return new HashSet<>(REGISTERED_VIEW_NAMES);
  }

  private final Map<String, Function<Object[], Object>> functions = new HashMap<>();

  public void registerFunction(String name, Function<Object[], Object> function) {
    Objects.requireNonNull(name, "Function name cannot be null");
    Objects.requireNonNull(function, "Function cannot be null");
    functions.put(name, function);
  }

  public Object invoke(String functionName) {
    return invoke(functionName, new Object[] {});
  }

  public Object invoke(String functionName, Object[] args) {
    Objects.requireNonNull(functionName, "Function name cannot be null");
    Function<Object[], Object> function = functions.get(functionName);
    if (function == null) {
      throw new IllegalArgumentException("Function not registered: " + functionName);
    }
    Object[] effectiveArgs = args == null ? new Object[] {} : args;
    return function.apply(effectiveArgs);
  }

  public Set<String> getRegisteredFunctions() {
    return Collections.unmodifiableSet(functions.keySet());
  }
}
