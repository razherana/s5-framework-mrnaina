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
   * Method to be executed after the controller's method.
   * 
   * @return Null to continue the execution chain, or an object to short-circuit
   *         the execution and return that object as the response.
   */
  @Resolve
  abstract public Object after();
}
