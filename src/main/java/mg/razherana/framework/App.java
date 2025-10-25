package mg.razherana.framework;

import java.util.List;

import mg.razherana.framework.scanners.ScanControllers;

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

  public App(List<Class<?>> controllerClasses) {
    this.controllerClasses = controllerClasses;
  }

  public List<Class<?>> getControllerClasses() {
    return controllerClasses;
  }

  public void setControllerClasses(List<Class<?>> controllerClasses) {
    this.controllerClasses = controllerClasses;
  }

  public void scanControllers(String basePackage) {
    this.controllerClasses = ScanControllers.findControllerClasses(basePackage);
  }
}
