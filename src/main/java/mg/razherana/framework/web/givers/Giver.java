package mg.razherana.framework.web.givers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.utils.ModelView;

/**
 * <p>
 * A giver is initialized before the controller's method is ran.
 * A Giver can be put into the controller method's parameter to be used.
 * It will be automatically initiated, and ran. Or you can use the annotation
 * Givers
 * </p>
 * 
 * <p>
 * Class that implements this <b>MUST</b> have an empty constructor
 * </p>
 * 
 * <p>
 * Giver's methods can be annotated with
 * {@link mg.razherana.framework.web.givers.annotations.Resolve} to be
 * dynamically
 * ran. It must be linked with a
 * {@link mg.razherana.framework.web.givers.annotations.Impl} annotation on
 * another method that contains the actual
 * parameters to be injected.
 * </p>
 */
public interface Giver {

  /**
   * This method is ran before the controller's method is ran.
   * 
   * @param request
   * @param response
   * @param modelView
   * 
   * @throws ServletException If any error occurs about servlet operations.
   */
  default public void init(HttpServletRequest request, HttpServletResponse response, ModelView modelView)
      throws ServletException {
    // Default implementation does nothing
  }
}
