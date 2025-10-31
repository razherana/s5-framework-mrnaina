package mg.razherana.framework.exceptions;

public class ControllerInitException extends ControllerException {

  public ControllerInitException(String message, Class<?> controllerClass) {
    super(message, controllerClass);
  }

}
