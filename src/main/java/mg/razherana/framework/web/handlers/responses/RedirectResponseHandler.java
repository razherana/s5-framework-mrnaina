package mg.razherana.framework.web.handlers.responses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResponseContainer;

public class RedirectResponseHandler implements ResponseHandler {

  @Override
  public void handleResponse(ResponseContainer rc, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    if (!(rc.getReturnObject() instanceof String)) {
      throw new IllegalArgumentException(
          "RedirectResponseHandler expects a String return object representing the location to redirect to.");
    }

    String location = (String) rc.getReturnObject();
    response.sendRedirect(location);
  }

}
