package mg.razherana.framework.web.utils.validation.rulefunctions;

import java.util.Map;

import mg.razherana.framework.web.utils.validation.ValidationRuleFunction;

public class ValNullFun extends ValidationRuleFunction {

  public ValNullFun(Map<String, Object> context) {
    super("$null", "$null", context);
  }

  @Override
  public boolean run(Object... args) {
    if (args.length == 0) {
      throwNotMultiple(1);

      Object value = getContext(ValidationKey.VALUE);
      return value == null;
    }

    return args[0] == null;
  }
}
