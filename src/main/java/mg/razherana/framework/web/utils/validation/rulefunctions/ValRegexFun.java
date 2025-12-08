package mg.razherana.framework.web.utils.validation.rulefunctions;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import mg.razherana.framework.web.utils.validation.ValidationRuleFunction;

public class ValRegexFun extends ValidationRuleFunction {

  public ValRegexFun(Map<String, Object> context) {
    super("$regex", "$regex", context);
  }

  @Override
  public boolean run(Object... args) {
    if (args.length <= 0) {
      throw new IllegalArgumentException(getFunctionName() + " requires at least one argument (the pattern in string)");
    }

    if (args.length > 2) {
      throw new IllegalArgumentException("Too many arguments for " + getFunctionName() + "." + getFunctionName()
          + " requires at least 1 argument (the pattern in string) or 2 arguments (the pattern in string and the value to check)");
    }

    Object value = null;

    if (args.length == 1) {
      throwNotMultiple(2);
      value = getContext(ValidationKey.VALUE);
    } else {
      value = args[1];
    }
    
    if (!(value instanceof String valueStr))
      throw new IllegalArgumentException("The value to check with " + getFunctionName() + " is not a string");

    Object regexObj = args[0];

    if (!(regexObj instanceof String regexStr))
      throw new IllegalArgumentException("The regex argument with " + getFunctionName() + " is not a string");

    Pattern pat = null;

    try {
      pat = Pattern.compile(regexStr);
    } catch (PatternSyntaxException e) {
      throw new IllegalArgumentException(
          "The regex pattern for " + getFunctionName() + " is not valid. Additional message : " + e.getMessage(), e);
    }

    return pat.matcher(valueStr).matches();
  }

}
