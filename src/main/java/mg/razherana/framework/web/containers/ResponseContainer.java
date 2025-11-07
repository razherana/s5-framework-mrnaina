package mg.razherana.framework.web.containers;

import java.util.HashMap;
import java.util.Map;

final public class ResponseContainer {
  /**
   * Contains the result of the controller url method call
   */
  private Object returnObject = null;

  /**
   * The return type to use so the plugin works
   */
  private String returnType = null;

  /**
   * Contains more parameters
   */
  private final Map<String, Object> otherParameters = new HashMap<>();

  public ResponseContainer(Object returnObject, String returnType) {
    this.returnObject = returnObject;
    this.returnType = returnType;
  }

  public Object getReturnObject() {
    return returnObject;
  }

  public void setReturnObject(Object returnObject) {
    this.returnObject = returnObject;
  }

  public String getReturnType() {
    return returnType;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  public Map<String, Object> getOtherParameters() {
    return otherParameters;
  }
}
