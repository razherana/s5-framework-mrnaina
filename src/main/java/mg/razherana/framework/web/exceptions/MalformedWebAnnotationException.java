package mg.razherana.framework.web.exceptions;

public class MalformedWebAnnotationException extends WebMappingException {

  public MalformedWebAnnotationException(String string, Throwable thr) {
    super(string, thr);
  }

  public MalformedWebAnnotationException(String message) {
    super(message);
  }
}
