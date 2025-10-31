package mg.razherana.framework.web.exceptions;

public class WebMappingException extends WebException {

  public WebMappingException(String string, Throwable thr) {
    super(string, thr);
  }

  public WebMappingException(String string) {
    super(string);
  }
}
