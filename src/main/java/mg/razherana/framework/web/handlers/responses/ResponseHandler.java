package mg.razherana.framework.web.handlers.responses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResponseContainer;

public interface ResponseHandler {
  /**
   * Handles the response based on the result given
   * 
   * @param mv
   * @param request
   * @param response
   */
  public void handleResponse(ResponseContainer rc, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
