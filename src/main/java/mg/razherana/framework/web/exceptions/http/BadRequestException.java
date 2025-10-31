package mg.razherana.framework.web.exceptions.http;

public class BadRequestException extends HttpException {
  Object data;

  public BadRequestException(String message) {
    super(message, 400);
  }

  public BadRequestException(String message, Object data) {
    super(message, 400);
    this.data = data;
  }

  public BadRequestException(String message, Throwable cause, Object data) {
    super(message, cause, 400);
    this.data = data;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }
}
