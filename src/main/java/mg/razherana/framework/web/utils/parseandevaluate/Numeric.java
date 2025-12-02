package mg.razherana.framework.web.utils.parseandevaluate;

import java.math.BigDecimal;

import mg.razherana.framework.web.utils.validation.ValidationEvaluator;

public final class Numeric {
  private Numeric() {
  }

  static int compare(Object left, Object right) {
    if (left == null || right == null) {
      throw new IllegalArgumentException("Cannot compare null values");
    }
    if (left instanceof Number && right instanceof Number) {
      return toBigDecimal(left).compareTo(toBigDecimal(right));
    }
    if (left instanceof Comparable<?> comparable && comparable.getClass().isAssignableFrom(right.getClass())) {
      @SuppressWarnings("unchecked")
      Comparable<Object> comparableLeft = (Comparable<Object>) comparable;
      return comparableLeft.compareTo(right);
    }
    if (right instanceof Comparable<?> comparable && comparable.getClass().isAssignableFrom(left.getClass())) {
      @SuppressWarnings("unchecked")
      Comparable<Object> comparableRight = (Comparable<Object>) comparable;
      return -comparableRight.compareTo(left);
    }
    throw new IllegalArgumentException("Cannot compare values of different types: "
        + describeType(left) + " and " + describeType(right));
  }

  static Object add(Object left, Object right) {
    if (left instanceof Number && right instanceof Number) {
      return toBigDecimal(left).add(toBigDecimal(right), ValidationEvaluator.DEFAULT_MATH_CONTEXT);
    }
    if (left instanceof String || right instanceof String) {
      return String.valueOf(left) + String.valueOf(right);
    }
    throw new IllegalArgumentException("Unsupported operands for '+': " + describeType(left) + ", "
        + describeType(right));
  }

  static Object subtract(Object left, Object right) {
    if (left instanceof Number && right instanceof Number) {
      return toBigDecimal(left).subtract(toBigDecimal(right), ValidationEvaluator.DEFAULT_MATH_CONTEXT);
    }
    throw new IllegalArgumentException("Unsupported operands for '-': " + describeType(left) + ", "
        + describeType(right));
  }

  static Object multiply(Object left, Object right) {
    if (left instanceof Number && right instanceof Number) {
      return toBigDecimal(left).multiply(toBigDecimal(right), ValidationEvaluator.DEFAULT_MATH_CONTEXT);
    }
    throw new IllegalArgumentException("Unsupported operands for '*': " + describeType(left) + ", "
        + describeType(right));
  }

  static Object divide(Object left, Object right) {
    if (left instanceof Number && right instanceof Number) {
      BigDecimal divisor = toBigDecimal(right);
      if (BigDecimal.ZERO.compareTo(divisor) == 0) {
        throw new IllegalArgumentException("Division by zero");
      }
      return toBigDecimal(left).divide(divisor, ValidationEvaluator.DEFAULT_MATH_CONTEXT);
    }
    throw new IllegalArgumentException("Unsupported operands for '/': " + describeType(left) + ", "
        + describeType(right));
  }

  static Object modulus(Object left, Object right) {
    if (left instanceof Number && right instanceof Number) {
      BigDecimal divisor = toBigDecimal(right);
      if (BigDecimal.ZERO.compareTo(divisor) == 0) {
        throw new IllegalArgumentException("Division by zero");
      }
      return toBigDecimal(left).remainder(divisor, ValidationEvaluator.DEFAULT_MATH_CONTEXT);
    }
    throw new IllegalArgumentException("Unsupported operands for '%': " + describeType(left) + ", "
        + describeType(right));
  }

  static Object negate(Object value) {
    if (value instanceof Number) {
      return toBigDecimal(value).negate(ValidationEvaluator.DEFAULT_MATH_CONTEXT);
    }
    throw new IllegalArgumentException("Unsupported operand for unary '-': " + describeType(value));
  }

  public static BigDecimal toBigDecimal(Object value) {
    if (value instanceof BigDecimal bigDecimal) {
      return bigDecimal;
    }
    if (value instanceof Number number) {
      return new BigDecimal(number.toString(), ValidationEvaluator.DEFAULT_MATH_CONTEXT);
    }
    throw new IllegalArgumentException("Cannot convert value to number: " + value);
  }

  private static String describeType(Object value) {
    return value == null ? "null" : value.getClass().getSimpleName();
  }
}