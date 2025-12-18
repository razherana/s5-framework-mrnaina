package mg.razherana.framework.web.routing;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;
import mg.razherana.framework.web.annotations.controllers.Stateful;
import mg.razherana.framework.web.containers.ControllerContainer;
import mg.razherana.framework.web.containers.RoutingContainer;
import mg.razherana.framework.web.containers.StatefulControllerContainer;
import mg.razherana.framework.web.containers.WebRouteContainer;
import mg.razherana.framework.web.containers.RoutingContainer.HttpMethod;
import mg.razherana.framework.web.exceptions.MalformedUserRouteException;

public class WebMapper {
  /**
   * Normalize the path by removing leading and trailing slashes.
   * This is useful for ensuring consistent path matching.
   * 
   * @param path
   * @return
   */
  public static String normalizePath(String path) {
    // Remove trailing and leading slashes for consistency
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    return path;
  }

  /**
   * Combine two paths and normalize the result.
   * This is useful for constructing full paths from base and sub-paths.
   * 
   * @param basePath
   * @param subPath
   * @return
   */
  public static String combineAndNormalizePaths(String basePath, String subPath) {
    String combinedPath = normalizePath(basePath) + "/" + normalizePath(subPath);
    return normalizePath(combinedPath);
  }

  private WebFinder webFinder;

  public WebMapper(WebFinder webFinder) {
    this.webFinder = webFinder;
  }

  /**
   * Get a stateful instance of the controller for the given request.
   * If the session already has a stateful controller container, it will return
   * the instance from that container.
   * If not, it will create a new stateful controller container and return the
   * instance from that container.
   * 
   * @param request             the HTTP request
   * @param controllerContainer the controller container to use for creating the
   *                            stateful instance
   * @return the stateful controller instance
   * @throws IllegalArgumentException if the request or controllerContainer is
   *                                  null
   */
  public Object getStatefulInstance(HttpServletRequest request, ControllerContainer controllerContainer) {
    // Check nulls
    if (request == null || controllerContainer == null) {
      throw new IllegalArgumentException("Request and controller container cannot be null");
    }

    var session = request.getSession(true);
    if (session != null && session.getId() != null && session.getAttribute(Stateful.SESSION_ATTRIBUTE_KEY) != null) {
      return ((StatefulControllerContainer) session.getAttribute(Stateful.SESSION_ATTRIBUTE_KEY))
          .fetchControllerInstance(request);
    }
    // Init a new stateful controller container
    StatefulControllerContainer container = new StatefulControllerContainer(
        controllerContainer,
        request);
    session.setAttribute(Stateful.SESSION_ATTRIBUTE_KEY, container);
    return container.fetchControllerInstance(request);
  }

  /**
   * Find the route method for the given HTTP request.
   * This method extracts the path and HTTP method from the request,
   * normalizes the path, and then searches for a matching route in the web
   * finder.
   * 
   * @param request
   * @return
   */
  public WebRouteContainer findRouteMethod(HttpServletRequest request) {
    String path = request.getRequestURI();
    path = path.replace(request.getContextPath(), "");
    path = normalizePath(path);

    String method = request.getMethod();

    HttpMethod httpMethod;
    try {
      httpMethod = HttpMethod.valueOf(method);
    } catch (IllegalArgumentException e) {
      return null;
    }

    System.out.println("[Fruits] : Finding route for [" + httpMethod + "] " + path);

    return findRouteMethod(httpMethod, path);
  }

  public WebFinder getWebFinder() {
    return webFinder;
  }

  public void setWebFinder(WebFinder webFinder) {
    this.webFinder = webFinder;
  }

  // Find the route method for the given HTTP method and path.
  // This method iterates through all controllers and their routing containers,
  // checking if the path matches any of the routing paths.
  // If a match is found, it extracts the path parameters and returns a
  // WebRouteContainer containing the method reflection and controller instance.
  // If no match is found, it returns null.
  private WebRouteContainer findRouteMethod(HttpMethod httpMethod, String path) {
    for (ControllerContainer controller : webFinder.getControllerContainers()) {
      String controllerPath = normalizePath(controller.getControllerAnnotation().value());

      System.out.println("[Fruits] : Testing for " + controllerPath);

      // Check if the controller can be used for this path
      if (!path.startsWith(controllerPath))
        continue;

      // Check each routing in the controller
      for (RoutingContainer routing : controller.getRoutingContainers()) {
        if (!httpMethod.equals(HttpMethod.ALL) && Arrays.stream(routing.getHttpMethods())
            .noneMatch(method -> method.equals(httpMethod)))
          continue;

        String fullRoutingPath = normalizePath(controllerPath + "/" + normalizePath(routing.getPath()));

        System.out.println("[Fruits] : Checking " + fullRoutingPath + " method");

        WebRouteContainer dataMatch = checkPathMatchAndExtractParameters(fullRoutingPath, path,
            routing.getMethodReflection(),
            controller.getControllerInstance(),
            controller);

        System.out.println("[Fruits] : Data match is " + dataMatch);

        if (dataMatch != null)
          return dataMatch;
      }
    }

    return null;
  }

  /**
   * Extract path parameters from the given path + check if it matches the routing
   * path
   * 
   * @param fullRoutingPath the full routing path (with parameters)
   * @param path            the actual request path
   * @return map of path parameters if matched, null otherwise
   */
  private HashMap<String, String> extractPathParameters(String fullRoutingPath, String path) {
    HashMap<String, String> pathParameters = new HashMap<>();

    String[] routingSegments = fullRoutingPath.split("/");
    String[] pathSegments = path.split("/");

    if (routingSegments.length != pathSegments.length) {
      return null;
    }

    for (int i = 0; i < routingSegments.length; i++) {
      String routingSegment = routingSegments[i];
      String pathSegment = pathSegments[i];

      String matchedSegment = matchSegmentAndReturn(routingSegment, pathSegment);

      if (matchedSegment == null)
        return null;

      // If we have a path parameter, extract it
      if (routingSegment.startsWith("[") && routingSegment.endsWith("]")) {
        String paramName = routingSegment.substring(1, routingSegment.length() - 1).split(":", 2)[0];

        if (paramName.isEmpty())
          throw new MalformedUserRouteException(
              "Path parameter name cannot be empty in segment: " + routingSegment + " of route: " + fullRoutingPath);

        pathParameters.put(paramName, matchedSegment);
      }
    }

    return pathParameters;
  }

  private String matchSegmentAndReturn(String routingSegment, String pathSegment) {
    if (routingSegment.startsWith("[") && routingSegment.endsWith("]")) {
      // We have a path parameter
      // Check if there is regex constraint
      String[] splitted = routingSegment.substring(1, routingSegment.length() - 1).split(":", 2);
      if (splitted.length == 2) {
        String regex = splitted[1];
        return pathSegment.matches(regex) ? pathSegment : null;
      }

      // Else it's a simple parameter
      return pathSegment;
    }

    return routingSegment.equals(pathSegment) ? pathSegment : null;
  }

  private WebRouteContainer checkPathMatchAndExtractParameters(String fullRoutingPath, String path, Method method,
      Object controllerInstance, ControllerContainer controllerContainer) {

    HashMap<String, String> pathParameters = extractPathParameters(fullRoutingPath, path);

    System.out.println("[Fruits] : Path param is " + pathParameters);

    if (pathParameters != null) {
      return new WebRouteContainer(method,
          controllerInstance,
          pathParameters,
          controllerContainer);
    }

    return null;
  }
}
