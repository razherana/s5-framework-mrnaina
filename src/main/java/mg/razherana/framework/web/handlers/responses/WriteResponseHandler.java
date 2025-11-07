package mg.razherana.framework.web.handlers.responses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResponseContainer;

public class WriteResponseHandler implements ResponseHandler {
  @Override
  public void handleResponse(ResponseContainer rc, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    response.getWriter().write(rc.getReturnObject() + "");
  }
}
