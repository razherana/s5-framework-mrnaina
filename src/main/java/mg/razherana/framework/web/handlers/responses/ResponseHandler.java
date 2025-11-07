package mg.razherana.framework.web.handlers.responses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResponseContainer;

public interface ResponseHandler {
  /**
   * Handles the response based on the result given.
   * 
   * @param rc       the response container holding the result data
   * @param request  the HTTP servlet request
   * @param response the HTTP servlet response
   * @throws Exception if an error occurs while handling the response
   */
  public void handleResponse(ResponseContainer rc, HttpServletRequest request, HttpServletResponse response)
      throws Exception;
}
