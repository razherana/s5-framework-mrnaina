package mg.razherana.framework.web.handlers.responses;

import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResponseContainer;
import mg.razherana.framework.web.exceptions.http.HttpException;

public class ErrorResponseHandler implements ResponseHandler {

  @Override
  public void handleResponse(ResponseContainer rc, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    Exception e = (Exception) rc.getReturnObject();

    if (e instanceof HttpException) {
      HttpException httpEx = (HttpException) e;

      response.sendError(httpEx.getStatusCode(), httpEx.getMessage());
      return;
    }

    String stackTrace = Arrays.stream(e.getStackTrace())
        .map(StackTraceElement::toString)
        .reduce("", (a, b) -> a + "\n" + b);

    response.sendError(500, "Exception found : " +
        (e.getMessage() != null
            ? e.getMessage()
            : "")
        + "\n" + stackTrace);
  }
}
