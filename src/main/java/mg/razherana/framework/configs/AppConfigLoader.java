package mg.razherana.framework.configs;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.routing.argsresolver.ArgResolver;
import mg.razherana.framework.web.routing.argsresolver.providers.ArgProvider;
import mg.razherana.framework.web.utils.ConversionUtils;
import mg.razherana.framework.web.utils.http.RequestBody;

public class AppConfigLoader {

  private static final ArgResolver CONFIG_ARG_RESOLVER = new ArgResolver();

  static {
    CONFIG_ARG_RESOLVER.registerProvider(new ConfigServletContextProvider());
  }

  private AppConfigLoader() {
  }

  public static Map<String, Object> loadConfigs(ServletContext servletContext, List<Class<?>> configClasses) {
    Map<String, Object> configs = new HashMap<>();
    Map<Class<?>, Object> configInstances = new HashMap<>();

    if (configClasses == null || configClasses.isEmpty()) {
      return configs;
    }

    for (Class<?> configClass : configClasses) {
      System.out.println("[Fruits] : Processing config class: " + configClass);

      if (!configClass.isAnnotationPresent(AppConfig.class)) {
        continue;
      }

      for (Method method : configClass.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(Config.class)) {
          continue;
        }

        Config config = method.getAnnotation(Config.class);

        method.setAccessible(true);

        String configName = config.value();
        if (configName == null || configName.isBlank())
          configName = method.getName();

        boolean override = config.override();

        Object contextValue = resolveFromContext(servletContext, configName, method.getReturnType());
        Object methodValue = null;

        if (override || contextValue == null) {
          methodValue = invokeConfigMethod(servletContext, configInstances, configClass, method);
        }

        Object finalValue;
        if (override) {
          finalValue = methodValue != null ? methodValue : contextValue;
        } else {
          finalValue = contextValue != null ? contextValue : methodValue;
        }

        if (finalValue != null) {
          System.out.println("[Fruits] : Registering config '" + configName + "' with value: " + finalValue);
          configs.put(configName, finalValue);
          servletContext.setAttribute(configName, finalValue);
        }
      }
    }

    return configs;
  }

  private static Object resolveFromContext(ServletContext servletContext, String name, Class<?> targetType) {
    Object attributeValue = servletContext.getAttribute(name);
    if (attributeValue != null) {
      try {
        return ConversionUtils.convertStringOrArrToType(attributeValue, targetType, null);
      } catch (Exception e) {
        throw new IllegalStateException(
            "Failed to convert context attribute '" + name + "' to " + targetType.getName(), e);
      }
    }

    String initParam = servletContext.getInitParameter(name);
    if (initParam != null) {
      try {
        return ConversionUtils.convertStringToType(initParam, targetType);
      } catch (Exception e) {
        throw new IllegalStateException(
            "Failed to convert context init parameter '" + name + "' to " + targetType.getName(), e);
      }
    }

    return null;
  }

  private static Object invokeConfigMethod(ServletContext servletContext, Map<Class<?>, Object> configInstances,
      Class<?> configClass, Method method) {
    try {
      if (method.getReturnType().equals(Void.TYPE)) {
        invokeMethod(servletContext, configInstances, configClass, method);
        return null;
      }

      return invokeMethod(servletContext, configInstances, configClass, method);
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to invoke config method " + method.getName() + " on " + configClass.getName(), e);
    }
  }

  private static Object invokeMethod(ServletContext servletContext, Map<Class<?>, Object> configInstances,
      Class<?> configClass, Method method) throws Exception {
    Object instance = null;
    if (!Modifier.isStatic(method.getModifiers())) {
      instance = configInstances.get(configClass);
      if (instance == null) {
        instance = configClass.getDeclaredConstructor().newInstance();
        configInstances.put(configClass, instance);
      }
    }

    Object[] args = resolveMethodArgs(servletContext, method);
    return method.invoke(instance, args);
  }

  private static Object[] resolveMethodArgs(ServletContext servletContext, Method method) {
    if (method.getParameterCount() == 0) {
      return new Object[0];
    }

    var parameters = method.getParameters();
    Object[] args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
      var param = parameters[i];
      args[i] = CONFIG_ARG_RESOLVER.resolveArgument(
          null,
          param,
          param.getType(),
          method,
          Collections.emptyMap(),
          null,
          null,
          null,
          null,
          Collections.emptyMap(),
          Map.of("servletContext", servletContext));
    }

    return args;

  }

  private static class ConfigServletContextProvider implements ArgProvider {
    @Override
    public boolean supports(mg.razherana.framework.web.routing.WebExecutor executor, java.lang.reflect.Parameter arg,
        Class<?> argType, Method method, Map<String, String> pathParameters,
        jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response,
        RequestBody requestBody, mg.razherana.framework.web.utils.ModelView mv,
        Map<Class<?>, mg.razherana.framework.web.givers.Giver> givers,
        Map<String, Object> additionalContext) {
      return argType.equals(ServletContext.class);
    }

    @Override
    public Object provide(mg.razherana.framework.web.routing.WebExecutor executor, java.lang.reflect.Parameter arg,
        Class<?> argType, Method method, Map<String, String> pathParameters,
        jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response,
        RequestBody requestBody, mg.razherana.framework.web.utils.ModelView mv,
        Map<Class<?>, mg.razherana.framework.web.givers.Giver> givers,
        Map<String, Object> additionalContext) {
      ServletContext servletContext = additionalContext.get("servletContext") instanceof ServletContext sc ? sc
          : null;
      if (servletContext == null) {
        throw new IllegalStateException("ServletContext is not available for config argument resolution.");
      }
      return servletContext;
    }
  }

}
