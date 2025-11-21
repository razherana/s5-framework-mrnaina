package mg.razherana.framework.web.utils.jsp.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import mg.razherana.framework.web.routing.WebMapper;
import mg.razherana.framework.App;
import mg.razherana.framework.web.containers.ControllerContainer;
import mg.razherana.framework.web.containers.RoutingContainer;
import mg.razherana.framework.web.utils.jsp.JspUtil;
import mg.razherana.framework.web.utils.jsp.exceptions.RouteNotFoundException;

public class RouteUtil extends JspUtil {

  private class DataDTO {
    String controllerAlias = "";
    String urlAlias = "";
    HashMap<String, String> pathParam = new HashMap<>();
    HashMap<String, String> queryParam = new HashMap<>();

    DataDTO(String controllerAlias) {
      this.controllerAlias = controllerAlias;
    }
  }

  private static final String VIEW_NAME = "$route";

  public RouteUtil() {
  }

  @Override
  public String getViewName() {
    return VIEW_NAME;
  }

  @Override
  public Object run(Object... args) {
    App app = (App) getData().get("app");
    List<ControllerContainer> controllerContainers = app.getWebMapper().getWebFinder().getControllerContainers();
    String contextPath = ((HttpServletRequest) getData().get("request")).getContextPath();

    DataDTO dataDTO = findVarsFromArgs(args);

    ControllerContainer controllerContainer = findControllerByAlias(dataDTO.controllerAlias, controllerContainers);

    if (controllerContainer == null) {
      throw new RouteNotFoundException(
          "The route with controller alias '" + dataDTO.controllerAlias + "' was not found");
    }

    RoutingContainer routingContainer = findUrlByAlias(controllerContainer, dataDTO.urlAlias);

    if (routingContainer == null) {
      throw new RouteNotFoundException("The route with url alias '" + dataDTO.urlAlias
          + "' was not found in controller with alias '" + dataDTO.controllerAlias + "'");
    }

    String controllerPath = controllerContainer.getControllerAnnotation().value();
    String methodPath = routingContainer.getPath();
    String fullPath = WebMapper.combineAndNormalizePaths(controllerPath, methodPath);

    String[] segments = fullPath.split("/");
    StringBuilder finalPath = new StringBuilder();

    if (!contextPath.isEmpty()) {
      contextPath = WebMapper.normalizePath(contextPath);

      finalPath.append("/" + contextPath + "/");
    }

    for (String segment : segments) {
      if (segment.isEmpty())
        continue;

      if (segment.startsWith("[") && segment.endsWith("]")) {
        String paramName = segment.substring(1, segment.length() - 1).split(":", 2)[0];
        if (!dataDTO.pathParam.containsKey(paramName)) {
          throw new RouteNotFoundException("In $route, missing path parameter: " + paramName);
        }
        String value = dataDTO.pathParam.get(paramName);
        String matched = matchSegmentAndReturn(segment, value);
        if (matched == null) {
          throw new RouteNotFoundException("In $route, parameter '" + paramName + "' value '" + value
              + "' does not match constraint in segment '" + segment + "'");
        }
        finalPath.append(matched);
      } else {
        finalPath.append(segment);
      }

      finalPath.append("/");
    }

    if (!dataDTO.queryParam.isEmpty()) {
      finalPath.append("?");
      List<String> queryParts = new ArrayList<>();
      for (Map.Entry<String, String> entry : dataDTO.queryParam.entrySet()) {
        queryParts.add(entry.getKey() + "=" + entry.getValue());
      }
      finalPath.append(String.join("&", queryParts));
    }

    return finalPath.toString();
  }

  ControllerContainer findControllerByAlias(String alias, List<ControllerContainer> controllerContainers) {
    for (ControllerContainer controllerContainer : controllerContainers) {
      String controllerAlias = controllerContainer.getControllerAnnotation().alias();

      if (controllerAlias.trim().isEmpty())
        controllerAlias = controllerContainer.getControllerClass().getSimpleName().toLowerCase();

      System.out.println("[Fruits] : Checking controller " + controllerContainer.getClass().getName() + ". Alias "
          + alias + " equals " + controllerAlias);
      if (controllerAlias.equals(alias)) {
        return controllerContainer;
      }
    }

    return null;
  }

  RoutingContainer findUrlByAlias(ControllerContainer controllerContainer, String alias) {
    List<RoutingContainer> routingContainers = controllerContainer.getRoutingContainers();

    for (RoutingContainer routingContainer : routingContainers) {
      String urlAlias = routingContainer.getRoutingAnnotation().alias();

      if (urlAlias.trim().isEmpty())
        urlAlias = routingContainer.getMethodReflection().getName().toLowerCase();

      if (urlAlias.equals(alias)) {
        return routingContainer;
      }
    }

    return null;
  }

  private DataDTO findVarsFromArgs(Object[] args) {
    if (args == null)
      Objects.requireNonNull(args, "The argument of " + getViewName() + " cannot be null");

    if (args.length <= 0)
      throw new IllegalArgumentException(getViewName() + " must have arguments");

    Object arg1 = args[0];
    String controllerAlias = "";
    String urlAlias = "";

    if (arg1 instanceof String) {
      String merged = (String) arg1;
      String[] splitted = merged.split("/", 2);
      if (splitted.length != 2)
        throw new IllegalArgumentException(
            "The first argument of $route must have the pattern of 'controllerAlias/urlAlias'");

      controllerAlias = splitted[0].trim();
      urlAlias = splitted[1].trim();
    } else
      throw new IllegalArgumentException(
          "The first argument of $route must be a String and have the pattern of 'controllerAlias/urlAlias'");

    DataDTO dataDTO = new DataDTO(controllerAlias);
    dataDTO.urlAlias = urlAlias;

    dataDTO.pathParam.clear();
    dataDTO.queryParam.clear();

    for (int i = 1; i < args.length; i++) {
      if (!(args[i] instanceof String)) {
        throw new IllegalArgumentException(
            "The path variable arguments must be String and must have the pattern of 'name=value'");
      }

      String argString = (String) args[i];

      /** Make it -2 so that trailing empty strings are included in the result */
      String[] splitted = argString.split("=", -2);

      if (splitted.length != 2) {
        throw new IllegalArgumentException(
            "The path variable arguments must have the pattern of 'name=value'");
      }

      if (splitted[0].trim().startsWith("?")) {
        dataDTO.queryParam.put(splitted[0].trim().substring(1), splitted[1]);
        continue;
      }

      dataDTO.pathParam.put(splitted[0].trim(), splitted[1]);
    }

    return dataDTO;
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
}
