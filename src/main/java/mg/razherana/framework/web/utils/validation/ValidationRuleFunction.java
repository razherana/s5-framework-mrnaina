package mg.razherana.framework.web.utils.validation;

import java.util.Map;

public abstract class ValidationRuleFunction {
  private final String name;
  private final String functionName;
  private final Map<String, Object> context;

  public ValidationRuleFunction(String name, String functionName, Map<String, Object> context) {
    this.name = name;
    this.functionName = functionName;
    this.context = context;
  }

  public final String getName() {
    return name;
  }

  public final String getFunctionName() {
    return functionName;
  }

  public final Map<String, Object> getContext() {
    return context;
  }

  /**
   * Run the validation rule function
   * 
   * @param args The arguments of the validation rule
   * @return true if the value is valid, false otherwise
   */
  public abstract boolean run(Object... args);
}
