package mg.razherana.framework.web.containers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.razherana.framework.web.annotations.controllers.Stateful;
import mg.razherana.framework.web.routing.WebFinder;

public class StatefulControllerContainer {
  private long creationTime;
  private long lastAccessedTime;
  private final long timeout;
  private HttpSession httpSession;
  private ControllerContainer controllerContainer;
  private Object controllerInstance;

  /**
   * Create a new StatefulControllerContainer.
   * 
   * @param controllerClasses
   */
  public StatefulControllerContainer(
      ControllerContainer controllerContainer,
      HttpServletRequest request) {
    this.controllerContainer = controllerContainer;
    this.httpSession = request.getSession(true);
    this.creationTime = System.currentTimeMillis();
    this.lastAccessedTime = creationTime;
    this.timeout = controllerContainer.getControllerClass().getAnnotation(Stateful.class).timeout();
    this.controllerInstance = WebFinder.instanciateController(controllerContainer.getControllerClass());
  }

  public void forceReset(HttpServletRequest request) {
    this.httpSession = request.getSession(true);
    this.creationTime = System.currentTimeMillis();
    this.lastAccessedTime = creationTime;
    this.controllerInstance = WebFinder.instanciateController(controllerContainer.getControllerClass());
  }

  public void reset(HttpServletRequest request, long accessTime) {
    long delta = accessTime - creationTime;
    if(delta >= timeout) {
      forceReset(request);
    }
    this.lastAccessedTime = accessTime;
  }

  public Object fetchControllerInstance(HttpServletRequest request) {
    reset(request, lastAccessedTime);
    return controllerInstance;
  }

  /**
   * @return the creationDatetime
   */
  public long getCreationTime() {
    return creationTime;
  }

  /**
   * @param creationDatetime the creationDatetime to set
   */
  public void setCreationTime(long creationDatetime) {
    this.creationTime = creationDatetime;
  }

  /**
   * @return the httpSession
   */
  public HttpSession getHttpSession() {
    return httpSession;
  }

  /**
   * @param httpSession the httpSession to set
   */
  public void setHttpSession(HttpSession httpSession) {
    this.httpSession = httpSession;
  }

  /**
   * @return the controllerContainer
   */
  public ControllerContainer getControllerContainer() {
    return controllerContainer;
  }

  /**
   * @param controllerContainer the controllerContainer to set
   */
  public void setControllerContainer(ControllerContainer controllerContainer) {
    this.controllerContainer = controllerContainer;
  }

  /**
   * @return the controllerInstance
   */
  public Object getControllerInstance() {
    return controllerInstance;
  }

  /**
   * @param controllerInstance the controllerInstance to set
   */
  public void setControllerInstance(Object controllerInstance) {
    this.controllerInstance = controllerInstance;
  }

}
