package mg.razherana.framework.web.utils.jsp.exceptions;

import mg.razherana.framework.web.exceptions.WebExecutionException;

public class RouteNotFoundException extends WebExecutionException {

  public RouteNotFoundException(String message) {
    super(message);
  }
  
  
}
