package mg.razherana.framework;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import mg.razherana.framework.scanners.ScanControllers;
import mg.razherana.framework.web.routing.WebFinder;
import mg.razherana.framework.web.routing.WebMapper;

/**
 * Contains all core infos for the framework
 */
public class App {

  public static enum InitKey {
    BASE_PACKAGE("basePackage");

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
}
