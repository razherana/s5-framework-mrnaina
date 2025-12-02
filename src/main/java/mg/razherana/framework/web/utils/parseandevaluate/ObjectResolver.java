package mg.razherana.framework.web.utils.parseandevaluate;

import java.util.HashMap;
import java.util.Map;

import mg.razherana.framework.web.utils.validation.ValidationRuleFunction;

public final class ObjectResolver {
  private final Map<String, ValidationRuleFunction> functions = new HashMap<>();
  private final Map<String, Object> variables = new HashMap<>();

  public ObjectResolver(Map<String, Object> context) {
    indexContext(context);
  }

  ValidationRuleFunction resolveFunction(String name) {
    return functions.get(name);
  }

  Object resolveVariable(String name) {
    return variables.get(name);
  }

  Object resolve(String name) {
    Object variable = resolveVariable(name);
    if (variable != null) {
      return variable;
    }
    ValidationRuleFunction function = resolveFunction(name);
    if (function != null) {
      return function;
    }
    return null;
  }

  private void indexContext(Map<String, Object> context) {
    if (context == null) {
      return;
    }
    for (Map.Entry<String, Object> entry : context.entrySet()) {
      registerEntry(entry.getKey(), entry.getValue());
    }
  }

  private void registerEntry(String alias, Object value) {
    if (value instanceof ValidationRuleFunction function) {
      registerAlias(alias, function);
      registerAlias(function.getName(), function);
      registerAlias(function.getFunctionName(), function);
      return;
    }

    variables.put(alias, value);
  }

  private void registerAlias(String alias, ValidationRuleFunction function) {
    if (alias == null || alias.isBlank() || function == null) {
      return;
    }
    functions.putIfAbsent(alias, function);
  }
}