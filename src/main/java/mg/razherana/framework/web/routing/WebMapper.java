package mg.razherana.framework.web.routing;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

      Map<String, String> matchedSegment = matchSegmentAndReturnParameters(routingSegment, pathSegment);

      if (matchedSegment == null)
        return null;

      pathParameters.putAll(matchedSegment);
    }

    return pathParameters;
  }

  private Map<String, String> matchSegmentAndReturnParameters(String routingSegment, String pathSegment) {
    if (!routingSegment.contains("[") && !routingSegment.contains("]")) {
      return routingSegment.equals(pathSegment) ? Map.of() : null;
    }

    Map<String, String> parameters = new HashMap<>();
    Vector<String> paramNames = new Vector<>();

    StringBuilder regexBuilder = new StringBuilder("^");
    StringBuilder literalBuilder = new StringBuilder();
    int openBrackets = 0;
    StringBuilder paramBuilderTemp = new StringBuilder();

    // [p1]-test-[p2]
    for (int charIndex = 0; charIndex < routingSegment.length(); charIndex++) {
      char c = routingSegment.charAt(charIndex);

      if (c == '[') {
        // If we are entering a parameter and we have a literal part before, add it to
        // the regex
        if (openBrackets == 0 && literalBuilder.length() > 0) {
          String literalPart = Pattern.quote(literalBuilder.toString());
          regexBuilder.append(literalPart);
          literalBuilder = new StringBuilder();

          System.out
              .println("[Fruits] : Added literal part to regex: " + literalPart + " for segment: " + routingSegment);
        }

        openBrackets++;

        if (openBrackets == 1)
          continue;
      }

      if (c == ']') {
        if (openBrackets <= 0) {
          throw new MalformedUserRouteException("Malformed route: " + routingSegment
              + ". Unmatched closing bracket ']' at index: " + charIndex);
        }

        openBrackets--;
      }

      System.out.println("[Fruits] : Processing char '" + c + "' in segment '" + routingSegment
          + "'. Open brackets count: " + openBrackets + " regex so far: " + regexBuilder.toString());

      if (openBrackets > 0)
        paramBuilderTemp.append(String.valueOf(c));

      // If we are outside of brackets, we want to match the literal part of the
      // segment
      if (openBrackets == 0) {
        // If end of a segment
        if (c == ']') {
          String paramRegex = paramBuilderTemp.toString();

          // Check if we have a custom regex for the parameter (eg: [id:\d+])
          String[] splitted = paramRegex.split(":", 2);

          // Default regex to match any segment except '/'
          String regexPart = splitted.length > 1 && !splitted[1].isBlank() ? splitted[1].trim() : ".+?";
          regexBuilder.append("(").append(regexPart).append(")");

          paramNames.add(splitted[0].trim());

          paramBuilderTemp = new StringBuilder();

          System.out.println("[Fruits] : End of parameter detected. Added regex part: " + regexPart + " for parameter: "
              + splitted[0].trim());

          continue;
        }

        // Else it's a literal string
        literalBuilder.append(c);
      }
    }

    regexBuilder.append("$");

    System.out.println("[Fruits] : Matching segment '" + routingSegment + "' against path segment '" + pathSegment
        + "' with regex: " + regexBuilder.toString());

    Pattern pattern = Pattern.compile(regexBuilder.toString());
    Matcher matcher = pattern.matcher(pathSegment);

    if (!matcher.matches())
      return null;

    for (int i = 0; i < paramNames.size(); i++)
      parameters.put(paramNames.get(i), matcher.group(i + 1));

    return parameters;
  }

  private WebRouteContainer checkPathMatchAndExtractParameters(String fullRoutingPath, String path, Method method,
      Object controllerInstance, ControllerContainer controllerContainer) {

    HashMap<String, String> pathParameters = extractPathParameters(fullRoutingPath, path);

    if (pathParameters != null) {
      return new WebRouteContainer(method,
          controllerInstance,
          pathParameters,
          controllerContainer);
    }

    return null;
  }
}
