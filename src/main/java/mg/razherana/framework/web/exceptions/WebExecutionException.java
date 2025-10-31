package mg.razherana.framework.web.exceptions;

public class WebExecutionException extends WebException {
  public WebExecutionException(String message) {
    super(message);
  }

  public WebExecutionException(String string, Throwable thr) {
    super(string, thr);
  }
}
