package mg.razherana.framework.web.middlewares;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.utils.ModelView;

public interface Middleware {
  /**
   * This method is ran before the controller's method is ran.
   * 
   * <p>
   * If we return a non-null object, the execution will be stopped and other
   * middlewares plus the controller's method will not be executed.
   * </p>
   * 
   * @param request
   * @param response
   * @param modelView
   * 
   * @return Object If you want to stop the execution and return a
   *         response directly or null to continue the execution.
   */
  default public Object before(HttpServletRequest request, HttpServletResponse response,
      ModelView modelView) {
    return null;
  }

  /**
   * This method is ran after the controller's method is ran.
   * 
   * <p>
   * If we return a non-null object, the execution will be stopped and other
   * middlewares
   * after methods will not be executed.
   * </p>
   * 
   * @param request
   * @param response
   * @param modelView
   * @param controllerResponse The response returned by the controller's method.
   * 
   * @return Object If you want to stop the execution
   *         and return a response directly or null to continue with the
   *         controller's response.
   */
  default public Object after(HttpServletRequest request, HttpServletResponse response,
      ModelView modelView, Object controllerResponse) {
    return null;
  }
}
