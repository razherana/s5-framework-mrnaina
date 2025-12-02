package mg.razherana.framework.web.utils.validation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;

import mg.razherana.framework.web.utils.parseandevaluate.ExpressionParser;
import mg.razherana.framework.web.utils.parseandevaluate.ObjectResolver;
import mg.razherana.framework.web.utils.parseandevaluate.Numeric;
import mg.razherana.framework.web.utils.parseandevaluate.Token;
import mg.razherana.framework.web.utils.parseandevaluate.Tokenizer;

public class ValidationEvaluator {
  public static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL64;
  public static final Map<String, Integer> OPERATOR_PRECEDENCE = Map.ofEntries(
      Map.entry("!", 5),
      Map.entry("*", 4),
      Map.entry("/", 4),
      Map.entry("%", 4),
      Map.entry("+", 3),
      Map.entry("-", 3),
      Map.entry("<", 2),
      Map.entry("<=", 2),
      Map.entry(">", 2),
      Map.entry(">=", 2),
      Map.entry("==", 2),
      Map.entry("!=", 2),
      Map.entry("&&", 1),
      Map.entry("||", 1));

  private final ValidationParser parser;

  /**
   * @param parser We suppose that the parser is already initialized with rule and
   *               context
   */
  public ValidationEvaluator(ValidationParser parser) {
    this.parser = parser;
  }

  public ValidationResult evaluate() {
    Map<String, Object> context = parser.getContext();
    List<Token> tokens = new Tokenizer(parser.parse()).tokenize();
    ExpressionParser expressionParser = new ExpressionParser(tokens, new ObjectResolver(context));
    Object evaluationResult = expressionParser.parse();

    System.out.println("[Fruits] : Evaluation result: " + evaluationResult);

    if (asBoolean(evaluationResult))
      return ValidationResult.SUCCESS("Validation for rule passed: " + parser.getRule());

    return new ValidationResult(false,
        "Validation rule failed: " + parser.getRule());

  }

  public static boolean asBoolean(Object value) {
    if (value == null) {
      return false;
    }
    if (value instanceof Boolean bool) {
      return bool;
    }
    if (value instanceof Number number) {
      return Numeric.toBigDecimal(number).compareTo(BigDecimal.ZERO) != 0;
    }
    if (value instanceof String string) {
      return !string.isEmpty();
    }
    return true;
  }
}
