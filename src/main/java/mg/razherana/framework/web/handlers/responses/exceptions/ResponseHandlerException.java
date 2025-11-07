package mg.razherana.framework.web.handlers.responses.exceptions;

import mg.razherana.framework.web.exceptions.WebExecutionException;

public class ResponseHandlerException extends WebExecutionException {

  public ResponseHandlerException(String message) {
    super(message);
  }
  
  public ResponseHandlerException(String message, Throwable cause) {
    super(message, cause);
  }
}
