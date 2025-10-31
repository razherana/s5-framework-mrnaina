package mg.razherana.framework.exceptions;

public class ControllerException extends FrameworkException {
  public ControllerException(String message) {
    super(message);
  }

  public ControllerException(String message, Throwable cause) {
    super(message, cause);
  }

  public ControllerException(String message, Class<?> controllerClass) {
    super("Controller Error in " + controllerClass.getName() + ": " + message);
  }
}
