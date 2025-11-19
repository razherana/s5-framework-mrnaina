package mg.razherana.framework;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletContext;
import mg.razherana.framework.scanners.ScanControllers;
import mg.razherana.framework.web.exceptions.WebExecutionException;
import mg.razherana.framework.web.handlers.responses.ErrorResponseHandler;
import mg.razherana.framework.web.handlers.responses.JspViewResponseHandler;
import mg.razherana.framework.web.handlers.responses.ResponseHandler;
import mg.razherana.framework.web.handlers.responses.WriteResponseHandler;
import mg.razherana.framework.web.routing.WebFinder;
import mg.razherana.framework.web.routing.WebMapper;
import mg.razherana.framework.web.utils.jsp.JspFunctionBridge;
import mg.razherana.framework.web.utils.jsp.defaults.AttributeUtil;
import mg.razherana.framework.web.utils.jsp.defaults.RouteUtil;
import mg.razherana.framework.web.utils.jsp.preprocessor.ManualJSPPreprocessor;

/**
 * Contains all core infos for the framework
 */
public class App {

  public static enum InitKey {
    BASE_PACKAGE("basePackage"),
    RESPONSE_HANDLERS("responseHandlers"),
    VIEWS_DIRECTORY("viewsDirectory"),
    JSP_UTILS("jspUtils");

    private final String key;

    InitKey(String key) {
      this.key = key;
    }

    public String getKey() {
      return key;
    }
  }

  private List<Class<?>> controllerClasses;
  private Map<Class<?>, List<Method>> urlControllerMap;
  private WebFinder webFinder;
  private WebMapper webMapper;
  private final Map<String, ResponseHandler> responseHandlerMap = new HashMap<>();

  public App(List<Class<?>> controllerClasses, Map<Class<?>, List<Method>> urlControllerMap) {
    this.controllerClasses = controllerClasses;
    this.urlControllerMap = urlControllerMap;
  }

  public WebMapper getWebMapper() {
    return webMapper;
  }

  public List<Class<?>> getControllerClasses() {
    return controllerClasses;
  }

  public void setControllerClasses(List<Class<?>> controllerClasses) {
    this.controllerClasses = controllerClasses;
  }

  public void scanControllers(String basePackage) {
    this.controllerClasses = ScanControllers.findControllerClasses(basePackage);
    this.urlControllerMap = ScanControllers.getControllerUrlsMethod();
  }

  public Map<Class<?>, List<Method>> getUrlControllerMap() {
    return urlControllerMap;
  }

  public void setUrlControllerMap(Map<Class<?>, List<Method>> urlControllerMap) {
    this.urlControllerMap = urlControllerMap;
  }

  public void initWeb() {
    webFinder = new WebFinder(urlControllerMap);
    webMapper = new WebMapper(webFinder);
  }

  public Map<String, ResponseHandler> getResponseHandlerMap() {
    return responseHandlerMap;
  }

  public void initResponseHandlers(ServletContext servletContext) {
    // Init the default ones
    responseHandlerMap.put("view", new JspViewResponseHandler());
    responseHandlerMap.put("write", new WriteResponseHandler());
    responseHandlerMap.put("error", new ErrorResponseHandler());

    // Set the custom ones
    try {
      String classNamesStr = servletContext.getInitParameter(InitKey.RESPONSE_HANDLERS.getKey());

      if (classNamesStr == null)
        return;

      classNamesStr = classNamesStr.trim();

      String[] classNames = classNamesStr.split(",");

      for (String value : classNames) {
        value = value.trim();

        String[] valueSplitted = value.split(":");

        if (valueSplitted.length != 2)
          throw new WebExecutionException("Invalid response handler format: " + value);

        String type = valueSplitted[0].trim();
        String className = valueSplitted[1].trim();

        Class<?> clazz = Class.forName(className);

        if (!ResponseHandler.class.isAssignableFrom(clazz))
          throw new WebExecutionException("Class does not implement ResponseHandler: " + className);

        ResponseHandler responseObject = (ResponseHandler) clazz.getConstructor().newInstance();

        responseHandlerMap.put(type, responseObject);
      }
    } catch (Exception e) {
      throw new WebExecutionException(e);
    }
  }

  public void initJspUtils(ServletContext servletContext) {
    String viewsDirectory = servletContext.getInitParameter(InitKey.VIEWS_DIRECTORY.getKey());

    if (viewsDirectory == null || viewsDirectory.isEmpty()) {
      System.out.println("[Fruits] : No views directory specified. Using default '/WEB-INF/views'");
      viewsDirectory = "/WEB-INF/views";
    }

    String jspUtilsConfig = servletContext.getInitParameter(InitKey.JSP_UTILS.getKey());

    JspFunctionBridge.registerJspUtil(defaultJspUtilMappings(), parseJspUtilConfiguration(jspUtilsConfig),
        servletContext);

    try {
      int result = ManualJSPPreprocessor.processDirectory(servletContext.getRealPath(viewsDirectory));
      System.out.println("[Fruits] : Preprocessed " + result + " JSP files.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void destroy(ServletContext servletContext) {
    String viewsDirectory = servletContext.getInitParameter(InitKey.VIEWS_DIRECTORY.getKey());

    if (viewsDirectory == null || viewsDirectory.isEmpty()) {
      System.out.println("[Fruits] : No views directory specified. Using default '/WEB-INF/views'");
      viewsDirectory = "/WEB-INF/views";
    }

    try {
      if (new File(viewsDirectory).exists())
        ManualJSPPreprocessor.restoreBackups(servletContext.getRealPath(viewsDirectory));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<String> parseJspUtilConfiguration(String configuration) {
    List<String> mappings = new ArrayList<>();

    if (configuration == null || configuration.isBlank()) {
      return mappings;
    }

    String[] entries = configuration.split(",");
    for (String entry : entries) {
      if (entry == null) {
        continue;
      }

      String className = entry.trim();
      if (className.isEmpty()) {
        continue;
      }

      mappings.add(className);
    }

    return mappings;
  }

  private List<String> defaultJspUtilMappings() {
    return List.of(
        RouteUtil.class.getName(),
        AttributeUtil.class.getName());
  }

}
