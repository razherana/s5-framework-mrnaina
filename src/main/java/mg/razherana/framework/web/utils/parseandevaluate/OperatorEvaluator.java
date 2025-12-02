package mg.razherana.framework.web.utils.parseandevaluate;

import java.util.Objects;

import mg.razherana.framework.web.utils.validation.ValidationEvaluator;

public final class OperatorEvaluator {
  private OperatorEvaluator() {
  }

  static Object evaluate(String operator, Object left, Object right) {
    System.out.println("[Fruits] : Evaluating operator " + operator + " with left=" + left + " and right=" + right);
    return switch (operator) {
      case "||" -> ValidationEvaluator.asBoolean(left) || ValidationEvaluator.asBoolean(right);
      case "&&" -> ValidationEvaluator.asBoolean(left) && ValidationEvaluator.asBoolean(right);
      case "==" -> Objects.equals(left, right);
      case "!=" -> !Objects.equals(left, right);
      case "<" -> Numeric.compare(left, right) < 0;
      case "<=" -> Numeric.compare(left, right) <= 0;
      case ">" -> Numeric.compare(left, right) > 0;
      case ">=" -> Numeric.compare(left, right) >= 0;
      case "+" -> Numeric.add(left, right);
      case "-" -> Numeric.subtract(left, right);
      case "*" -> Numeric.multiply(left, right);
      case "/" -> Numeric.divide(left, right);
      case "%" -> Numeric.modulus(left, right);
      default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
    };
  }

  static Object applyUnary(String operator, Object operand) {
    return switch (operator) {
      case "!" -> !ValidationEvaluator.asBoolean(operand);
      case "-" -> Numeric.negate(operand);
      default -> throw new IllegalArgumentException("Unsupported unary operator: " + operator);
    };
  }
}