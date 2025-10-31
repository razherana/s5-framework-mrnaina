package mg.razherana.framework.web.exceptions;

public class WebException extends RuntimeException {
  public WebException(String message) {
    super(message);
  }

  public WebException(String string, Throwable thr) {
    super(string, thr);
  }
}
