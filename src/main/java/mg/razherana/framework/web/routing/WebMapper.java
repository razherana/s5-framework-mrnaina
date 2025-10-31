package mg.razherana.framework.web.routing;

import java.lang.reflect.Method;
import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;
import mg.razherana.framework.web.containers.ControllerContainer;
import mg.razherana.framework.web.containers.RoutingContainer;
import mg.razherana.framework.web.containers.WebRouteContainer;
import mg.razherana.framework.web.containers.RoutingContainer.HttpMethod;
import mg.razherana.framework.web.exceptions.MalformedUserRouteException;

public class WebMapper {
  private WebFinder webFinder;

  public WebMapper(WebFinder webFinder) {
    this.webFinder = webFinder;
  }

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

  public static String combineAndNormalizePaths(String basePath, String subPath) {
    String combinedPath = normalizePath(basePath) + "/" + normalizePath(subPath);
    return normalizePath(combinedPath);
  }

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

  private WebRouteContainer findRouteMethod(HttpMethod httpMethod, String path) {
    for (ControllerContainer controller : webFinder.getControllerContainers()) {
      String controllerPath = normalizePath(controller.getControllerAnnotation().value());

      System.out.println("[Fruits] : Testing for " + controllerPath);

      // Check if the controller can be used for this path
      if (!path.startsWith(controllerPath))
        continue;

      // Check each routing in the controller
      for (RoutingContainer routing : controller.getRoutingContainers()) {
        if (!routing.getHttpMethod().equals(httpMethod))
          continue;

        String fullRoutingPath = normalizePath(controllerPath + "/" + normalizePath(routing.getPath()));

        System.out.println("[Fruits] : Checking " + fullRoutingPath + " method");

        WebRouteContainer dataMatch = checkPathMatchAndExtractParameters(fullRoutingPath, path, routing.getMethodReflection(),
            controller.getControllerInstance());

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
      Object controllerInstance) {

    HashMap<String, String> pathParameters = extractPathParameters(fullRoutingPath, path);

    System.out.println("[Fruits] : Path param is " + pathParameters);

    if (pathParameters != null) {
      return new WebRouteContainer(method,
          controllerInstance,
          pathParameters);
    }

    return null;
  }

  public WebFinder getWebFinder() {
    return webFinder;
  }

  public void setWebFinder(WebFinder webFinder) {
    this.webFinder = webFinder;
  }
}
