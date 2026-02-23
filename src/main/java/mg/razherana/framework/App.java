package mg.razherana.framework;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Stream;

import jakarta.servlet.ServletContext;
import mg.razherana.framework.configs.AppConfigLoader;
import mg.razherana.framework.scanners.ScanControllers;
import mg.razherana.framework.scanners.ScanConfigs;
import mg.razherana.framework.scanners.ScanResolvs;
import mg.razherana.framework.security.auth.middlewares.Authenticated;
import mg.razherana.framework.web.exceptions.WebExecutionException;
import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.handlers.responses.ErrorResponseHandler;
import mg.razherana.framework.web.handlers.responses.JspViewResponseHandler;
import mg.razherana.framework.web.handlers.responses.RedirectResponseHandler;
import mg.razherana.framework.web.handlers.responses.JsonResponseHandler;
import mg.razherana.framework.web.handlers.responses.ResponseHandler;
import mg.razherana.framework.web.handlers.responses.WriteResponseHandler;
import mg.razherana.framework.web.routing.WebFinder;
import mg.razherana.framework.web.routing.WebMapper;
import mg.razherana.framework.web.utils.jsp.JspFunctionBridge;
import mg.razherana.framework.web.utils.jsp.defaults.AttributeUtil;
import mg.razherana.framework.web.utils.jsp.defaults.RouteUtil;
import mg.razherana.framework.web.utils.jsp.preprocessor.AbstractJSPPreprocessor;
import mg.razherana.framework.web.utils.jsp.preprocessor.JSPUtilsPreprocessor;
import mg.razherana.framework.web.utils.proxies.MotherResolv;
import mg.razherana.framework.web.utils.sessionflash.SessionFlashMiddleware;

/**
 * Contains all core infos for the framework
 */
public class App {

  public static final List<Class<? extends AbstractJSPPreprocessor>> DEFAULT_JSP_PREPROCESSORS = List.of(
      JSPUtilsPreprocessor.class);

  public static final Set<Class<?>> FRAMEWORK_LOADED_RESOLV_CLASSES = Set.of(
      Authenticated.class,
      SessionFlashMiddleware.class);

  public static enum InitKey {
    BASE_PACKAGE("basePackage"),
    RESPONSE_HANDLERS("responseHandlers"),
    VIEWS_DIRECTORY("viewsDirectory"),
    JSP_UTILS("jspUtils"), JSP_PREPROCESSORS("jspPreprocessors");

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
  private List<Class<?>>[] resolvClasses;
  private Map<String, Object> configs = new HashMap<>();

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

  public void scanGivers(String basePackage) {
    this.resolvClasses = ScanResolvs.findResolvClasses(basePackage, new Class<?>[] {
        Giver.class,
        MotherResolv.class
    });
  }

  public void scanConfigs(String basePackage, ServletContext servletContext) {
    // Load init parameters from servlet context
    servletContext.getInitParameterNames().asIterator().forEachRemaining(paramName -> {
      String paramValue = servletContext.getInitParameter(paramName);
      if (paramValue != null) {
        servletContext.setAttribute(paramName, paramValue);
      }
    });

    // Scan for config classes and load configs
    List<Class<?>> configClasses = ScanConfigs.findConfigClasses(basePackage);
    this.configs = AppConfigLoader.loadConfigs(servletContext, configClasses);
  }

  public Map<Class<?>, List<Method>> getUrlControllerMap() {
    return urlControllerMap;
  }

  public void setUrlControllerMap(Map<Class<?>, List<Method>> urlControllerMap) {
    this.urlControllerMap = urlControllerMap;
  }

  public void initWeb() {
    webFinder = new WebFinder(urlControllerMap, resolvClasses, new Class<?>[] {
        Giver.class,
        MotherResolv.class
    });
    webMapper = new WebMapper(webFinder);
  }

  public Map<String, Object> getConfigs() {
    return configs;
  }

  public Map<String, ResponseHandler> getResponseHandlerMap() {
    return responseHandlerMap;
  }

  public void initResponseHandlers(ServletContext servletContext) {
    // Init the default ones
    responseHandlerMap.put("view", new JspViewResponseHandler());
    responseHandlerMap.put("write", new WriteResponseHandler());
    responseHandlerMap.put("json", new JsonResponseHandler());
    responseHandlerMap.put("error", new ErrorResponseHandler());
    responseHandlerMap.put("redirect", new RedirectResponseHandler());

    // Set the custom ones
    try {
      String classNamesStr = (String) servletContext.getAttribute(InitKey.RESPONSE_HANDLERS.getKey());

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
    String viewsDirectory = (String) servletContext.getAttribute(InitKey.VIEWS_DIRECTORY.getKey());

    if (viewsDirectory == null || viewsDirectory.isEmpty()) {
      System.out.println("[Fruits] : No views directory specified. Using default '/WEB-INF/views'");
      viewsDirectory = "/WEB-INF/views";
    }

    String jspUtilsConfig = (String) servletContext.getAttribute(InitKey.JSP_UTILS.getKey());

    JspFunctionBridge.registerJspUtil(defaultJspUtilMappings(), parseJspUtilConfiguration(jspUtilsConfig),
        servletContext);

    try {
      int result = preprocessJspFiles(servletContext.getRealPath(viewsDirectory), servletContext);
      System.out.println("[Fruits] : Preprocessed " + result + " JSP files in total.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void destroy(ServletContext servletContext) {
    String viewsDirectory = (String) servletContext.getAttribute(InitKey.VIEWS_DIRECTORY.getKey());

    if (viewsDirectory == null || viewsDirectory.isEmpty()) {
      System.out.println("[Fruits] : No views directory specified. Using default '/WEB-INF/views'");
      viewsDirectory = "/WEB-INF/views";
    }

    try {
      if (new File(servletContext.getRealPath(viewsDirectory)).exists()) {
        int restored = restoreJspBackups(servletContext.getRealPath(viewsDirectory));
        System.out.println("[Fruits] : Restored " + restored + " JSP backups.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private int preprocessJspFiles(String viewsDirectoryPath, ServletContext servletContext) throws IOException {
    Path viewsPath = Paths.get(viewsDirectoryPath);
    if (!Files.exists(viewsPath)) {
      throw new IOException("Directory does not exist: " + viewsDirectoryPath);
    }

    int count = 0;
    List<AbstractJSPPreprocessor> preprocessors = createJspPreprocessors(servletContext);

    try (Stream<Path> stream = Files.walk(viewsPath)) {
      for (Path jspPath : stream.filter(path -> path.toString().endsWith(".jsp")).toList()) {
        for (AbstractJSPPreprocessor preprocessor : preprocessors) {
          if (preprocessor.modify(jspPath.toString())) {
            count++;
          }
        }
      }
    }

    return count;
  }

  private int restoreJspBackups(String viewsDirectoryPath) throws IOException {
    Path viewsPath = Paths.get(viewsDirectoryPath);
    if (!Files.exists(viewsPath)) {
      return 0;
    }

    int restored = 0;
    try (Stream<Path> stream = Files.walk(viewsPath)) {
      for (Path backupPath : stream.filter(path -> path.toString().endsWith(".jsp.backup")).toList()) {
        if (AbstractJSPPreprocessor.restoreBackup(backupPath.toString())) {
          restored++;
        }
      }
    }

    return restored;
  }

  @SuppressWarnings("unchecked")
  private List<AbstractJSPPreprocessor> createJspPreprocessors(ServletContext servletContext) {
    List<AbstractJSPPreprocessor> preprocessors = new ArrayList<>();

    String preprocessorConfig = (String) servletContext
        .getAttribute(InitKey.JSP_PREPROCESSORS.getKey());

    Vector<Class<? extends AbstractJSPPreprocessor>> preprocessorClasses = new Vector<>();

    if (preprocessorConfig != null && !preprocessorConfig.trim().isEmpty()) {
      String[] classNames = preprocessorConfig.split(",");
      for (String className : classNames) {
        try {
          preprocessorClasses.add((Class<? extends AbstractJSPPreprocessor>) Class.forName(className.trim()));
        } catch (ClassNotFoundException e) {
          throw new WebExecutionException("Failed to load JSP preprocessor class: " + className, e);
        }
      }
      System.out.println("[Fruits] : Custom JSP preprocessors specified: " + preprocessorClasses);
    } else {
      System.out.println("[Fruits] : No custom JSP preprocessors specified. Using default preprocessors : "
          + DEFAULT_JSP_PREPROCESSORS);
    }

    preprocessorClasses = preprocessorConfig != null && !preprocessorConfig.isBlank() ? preprocessorClasses : new Vector<>(DEFAULT_JSP_PREPROCESSORS);

    // Add default preprocessors
    for (Class<? extends AbstractJSPPreprocessor> preprocessorClass : preprocessorClasses) {
      try {
        preprocessors.add(preprocessorClass.getDeclaredConstructor().newInstance());
      } catch (Exception e) {
        throw new WebExecutionException("Failed to instantiate JSP preprocessor: " + preprocessorClass.getName(), e);
      }
    }

    return preprocessors;
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
