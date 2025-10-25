package mg.razherana.framework.exceptions;

public class HttpException extends FrameworkException {
  int statusCode;

  public HttpException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public HttpException(String message, Throwable cause, int statusCode) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
}
