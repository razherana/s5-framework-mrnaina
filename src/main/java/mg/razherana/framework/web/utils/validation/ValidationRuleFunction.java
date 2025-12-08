package mg.razherana.framework.web.utils.validation;

import java.util.Map;

public abstract class ValidationRuleFunction {
  private final String name;
  private final String functionName;
  private final Map<String, Object> context;

  protected enum ValidationKey {
    IS_MULTIPLE_VALIDATION("__multiple_validation__"),
    ATTRIBUTES("__attributes__"),
    VALUE("__value__"),
    MESSAGE("__message__"),
    NAME("__name__");

    private String key;

    public String key() {
      return key;
    }

    ValidationKey(String key) {
      this.key = key;
    }
  }

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

  protected final <T> T getContext(ValidationKey key) {
    return getContext(key.key());
  }

  @SuppressWarnings("unchecked")
  protected final <T> T getContext(String key) {
    return (T) getContext().get(key);
  }

  protected void throwNotMultiple(int neededArgs) {
    if (!(boolean) getContext(ValidationKey.IS_MULTIPLE_VALIDATION))
      throw new IllegalArgumentException(getFunctionName() + " requires at least " + neededArgs
          + " argument(s). The current context is not in a multiple validation.");
  }

  /**
   * Run the validation rule function
   * 
   * @param args The arguments of the validation rule
   * @return true if the value is valid, false otherwise
   */
  public abstract boolean run(Object... args);
}
