package mg.razherana.framework.web.givers.proxies;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.GiverContainer;
import mg.razherana.framework.web.exceptions.WebExecutionException;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.givers.annotations.Resolve;
import mg.razherana.framework.web.routing.WebExecutor;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.http.RequestBody;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

public class GiverMethodInterceptor {
  @RuntimeType
  public static Object intercept(
      @This Giver self,
      @Origin Method method,
      @FieldValue("context____________") GiverInterceptorContext context) throws Exception {

    System.out.println("[Fruits] : Superclass is " + self.getClass().getSuperclass().getName());

    // Get the parameterized login method
    List<GiverContainer> containers = context
        .executor()
        .getWebMapper()
        .getWebFinder()
        .getGiverContainers()
        .get(self
            .getClass()
            .getSuperclass());

    if (containers != null) {
      GiverContainer giverContainer = null;
      for (GiverContainer container : containers)
        // Find the matching container for this giver instance
        if (container.getResolveMethod().equals(method)) {
          giverContainer = container;
          break;
        }

      if (giverContainer == null)
        throw new IllegalStateException("No matching GiverContainer found for method " + method.getName());

      Method implMethod = giverContainer.getImplMethod();

      Object[] args = WebExecutor.resolveArgs(context.executor(), implMethod, context.pathParameters(),
          context.request(), context.response(), context.requestBody(), context.mv(), context.givers());

      return implMethod.invoke(self, args);
    }

    throw new IllegalStateException("No GiverContainer found for " + self.getClass().getSuperclass().getName());
  }

  public static Giver getGiverInstance(
      Class<?> giverProxyClass,
      WebExecutor executor,
      Map<String, String> pathParameters,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestBody requestBody,
      ModelView mv,
      Map<Class<?>, Giver> givers) {
    GiverInterceptorContext context = new GiverInterceptorContext(
        executor,
        pathParameters,
        request,
        response,
        requestBody,
        mv,
        givers);

    Class<?> proxyClass = new ByteBuddy()
        .subclass(giverProxyClass)
        .defineField("context____________", GiverInterceptorContext.class, Modifier.PUBLIC)
        // Inject context into the field
        .method(ElementMatchers.isAnnotatedWith(Resolve.class) // Methods with @Resolve
            .and(ElementMatchers.isAbstract()) // And are abstract
            .and(ElementMatchers.takesNoArguments())) // No arguments
        .intercept(MethodDelegation.to(GiverMethodInterceptor.class))
        .make()
        .load(Giver.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
        .getLoaded();

    try {
      Giver giver = (Giver) proxyClass.getDeclaredConstructor().newInstance();

      // Set the context field
      Field setContext = proxyClass.getDeclaredField("context____________");
      setContext.setAccessible(true);
      setContext.set(giver, context);

      return giver;
    } catch (Exception e) {
      // Should not happen
      throw new WebExecutionException("Failed to create Giver proxy instance for " + giverProxyClass.getName(), e);
    }
  }
}
