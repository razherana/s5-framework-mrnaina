package mg.razherana.framework.exceptions;

public class NotFoundException extends HttpException {
  public NotFoundException(String message) {
    super(message, 404);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause, 404);
  }
}
