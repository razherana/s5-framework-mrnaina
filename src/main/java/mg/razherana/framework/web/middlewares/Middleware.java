package mg.razherana.framework.web.middlewares;

import mg.razherana.framework.web.utils.proxies.MotherResolv;
import mg.razherana.framework.web.utils.proxies.annotations.Resolve;

/**
 * Middleware interface for web request processing.
 * Middlewares can use @Impl and @Resolve annotations to inject like Givers and
 * Controller's Urls.
 */
// Implementing MotherResolv to be used in WebFinder
public abstract class Middleware implements MotherResolv {

  /**
   * Method to be executed before the controller's method.
   * 
   * @return Null to continue the execution chain, or an object to short-circuit
   *         the execution and return that object as the response.
   */
  @Resolve
  abstract public Object before();

  /**
   * Method that should be ran in afterResponse.
   * 
   * @return The object to send to {@link #afterResponse(Object)}
   */
  @Resolve
  abstract public Object after();

  /**
   * Method to be executed after the controller's method.
   * 
   * @param controllerResponse The response from the controller's method.
   * @return The modified response to send back to the client. Null means no
   *         modification.
   */
  public Object afterResponse(Object controllerResponse) {
    return null;
  }
}
