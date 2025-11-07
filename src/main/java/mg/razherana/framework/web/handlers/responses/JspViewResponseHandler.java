package mg.razherana.framework.web.handlers.responses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResponseContainer;
import mg.razherana.framework.web.handlers.responses.exceptions.ResponseHandlerException;

public class JspViewResponseHandler implements ResponseHandler {
  public class ViewResponseHandlerException extends ResponseHandlerException {
    private ViewResponseHandlerException(String message) {
      super(message);
    }
  }

  @Override
  public void handleResponse(ResponseContainer rc, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    if (!(rc.getReturnObject() instanceof String))
      throw new ViewResponseHandlerException("The return object is not a string");

    final String viewName = (String) rc.getReturnObject();

    request.getRequestDispatcher(viewName).forward(request, response);
  }
}
