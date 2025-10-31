package mg.razherana.framework.web.exceptions;

public class MalformedUserRouteException extends WebMappingException {
  public MalformedUserRouteException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public MalformedUserRouteException(String message) {
    super(message);
  }
}
