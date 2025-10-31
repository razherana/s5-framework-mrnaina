package mg.razherana.framework.web.exceptions.http;

public class NotFoundException extends HttpException {
  public NotFoundException(String message) {
    super(message, 404);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause, 404);
  }
}
