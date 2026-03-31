package mg.razherana.framework.web.utils.proxies;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResolvContainer;
import mg.razherana.framework.web.exceptions.WebExecutionException;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.ReflectUtils;
import mg.razherana.framework.web.utils.http.RequestBody;
import mg.razherana.framework.web.utils.proxies.annotations.Resolve;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatcher;

public class MethodInterceptor {
  public static class InHierarchyMatcher implements ElementMatcher<MethodDescription> {
    private final Class<?> startClass;
    private final Class<?> stopAt;

    public InHierarchyMatcher(Class<?> startClass, Class<?> stopAt) {
      this.startClass = startClass;
      this.stopAt = stopAt;
    }

    @Override
    public boolean matches(MethodDescription target) {
      Class<?> current = startClass;
      while (current != null && current != stopAt) {
        if (target.isAbstract()
            && target.getDeclaredAnnotations().isAnnotationPresent(Resolve.class))
          return true;

        current = current.getSuperclass();
      }
      return false;
    }
  }

  @RuntimeType
  public static Object intercept(
      @This Object self,
      @Origin Method method,
      @FieldValue("context____________") InterceptorContext context,
      @AllArguments Object[] originalArgs) throws Exception {

    Class<?> resolvClass = self.getClass().getSuperclass();

    List<ResolvContainer> containers = context
        .executor()
        .getWebMapper()
        .getWebFinder()
        .getResolvContainers()
        .get(resolvClass);

    if (containers != null) {
      ResolvContainer resolvContainer = null;
      for (ResolvContainer container : containers)
        // Find the matching container for this resolv instance
        if (container.getResolveMethod().equals(method)) {
          resolvContainer = container;
          break;
        }

      if (resolvContainer == null)
        throw new IllegalStateException("No matching ResolvContainer found for method " + method.getName());

      Method implMethod = resolvContainer.getImplMethod();

      if (implMethod == null)
        throw new IllegalStateException("No implementation method found for " + method.getName());

      Parameter[] implParams = implMethod.getParameters();
      Parameter[] toResolve = new Parameter[implParams.length - originalArgs.length];

      for (int i = originalArgs.length; i < implParams.length; i++)
        toResolve[i - originalArgs.length] = implParams[i];

      Object[] newArgs = WebExecutor.resolveArgs(context.executor(), implMethod, context.pathParameters(),
          context.request(), context.response(), context.requestBody(), context.mv(), context.givers(), toResolve);

      Object[] allArgs = new Object[originalArgs.length + newArgs.length];

      for (int i = 0; i < originalArgs.length; i++)
        allArgs[i] = originalArgs[i];

      for (int i = originalArgs.length; i < allArgs.length; i++)
        allArgs[i] = newArgs[i - originalArgs.length];

      return implMethod.invoke(self, allArgs);
    }

    throw new IllegalStateException("No ResolvContainer found for " + self.getClass().getSuperclass().getName());
  }

  @SuppressWarnings("unchecked")
  public static <T> T getInstance(
      Class<T> toResolvClass,
      Class<?> originClass,
      WebExecutor executor,
      Map<String, String> pathParameters,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestBody requestBody,
      ModelView mv,
      Map<Class<?>, Giver> givers) {
    InterceptorContext context = new InterceptorContext(
        executor,
        pathParameters,
        request,
        response,
        requestBody,
        mv,
        givers,
        toResolvClass);

    // Get the parameterized login method
    Set<Class<?>> resolvClasses = context
        .executor()
        .getWebMapper()
        .getWebFinder()
        .getResolvContainers()
        .keySet();

    Class<?> youngestChild = ReflectUtils.getYoungestChildClass(toResolvClass, resolvClasses);

    toResolvClass = (Class<T>) (youngestChild != null ? youngestChild : toResolvClass);

    Class<? extends T> proxyClass = new ByteBuddy()
        .subclass(toResolvClass)
        .defineField("context____________", InterceptorContext.class, Modifier.PUBLIC)
        // Inject context into the field
        .method(new InHierarchyMatcher(toResolvClass, originClass))
        .intercept(MethodDelegation.to(MethodInterceptor.class))
        .make()
        .load(toResolvClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
        .getLoaded();

    try {
      T resolvedInstance = proxyClass.getDeclaredConstructor().newInstance();

      // Set the context field
      Field setContext = proxyClass.getDeclaredField("context____________");
      setContext.setAccessible(true);
      setContext.set(resolvedInstance, context);

      return resolvedInstance;
    } catch (Exception e) {
      // Should not happen
      throw new WebExecutionException("Failed to create Resolv proxy instance for " + toResolvClass.getName(), e);
    }
  }
}
