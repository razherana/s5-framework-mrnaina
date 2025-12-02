package mg.razherana.framework.web.utils.parseandevaluate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mg.razherana.framework.web.utils.validation.ValidationRuleFunction;

public class ExpressionParser {
  private static final Set<String> OR_OPERATORS = Set.of("||");
  private static final Set<String> AND_OPERATORS = Set.of("&&");
  private static final Set<String> EQUALITY_OPERATORS = Set.of("==", "!=");
  private static final Set<String> COMPARISON_OPERATORS = Set.of("<", "<=", ">", ">=");
  private static final Set<String> ADDITIVE_OPERATORS = Set.of("+", "-");
  private static final Set<String> MULTIPLICATIVE_OPERATORS = Set.of("*", "/", "%");

  /** List of tokens to parse */
  private final List<Token> tokens;

  /** Function Resolver */
  private final ObjectResolver objectResolver;

  /** Current position in the token list */
  private int position;

  public ExpressionParser(List<Token> tokens, ObjectResolver objectResolver) {
    this.tokens = tokens;
    this.objectResolver = objectResolver;
  }

  public Object parse() {
    Object value = parseOr();
    if (!isAtEnd()) {
      throw new IllegalArgumentException("Unexpected token: " + peek().lexeme);
    }
    return value;
  }

  private Object parseOr() {
    Object left = parseAnd();
    String operator;
    while ((operator = matchAnyOperator(OR_OPERATORS)) != null) {
      Object right = parseAnd();
      left = OperatorEvaluator.evaluate(operator, left, right);
    }
    return left;
  }

  private Object parseAnd() {
    Object left = parseEquality();
    String operator;
    while ((operator = matchAnyOperator(AND_OPERATORS)) != null) {
      Object right = parseEquality();
      left = OperatorEvaluator.evaluate(operator, left, right);
    }
    return left;
  }

  private Object parseEquality() {
    Object left = parseComparison();
    String operator;
    while ((operator = matchAnyOperator(EQUALITY_OPERATORS)) != null) {
      Object right = parseComparison();
      left = OperatorEvaluator.evaluate(operator, left, right);
    }
    return left;
  }

  private Object parseComparison() {
    Object left = parseAdditive();
    String operator;
    while ((operator = matchAnyOperator(COMPARISON_OPERATORS)) != null) {
      Object right = parseAdditive();
      left = OperatorEvaluator.evaluate(operator, left, right);
    }
    return left;
  }

  private Object parseAdditive() {
    Object left = parseMultiplicative();
    String operator;
    while ((operator = matchAnyOperator(ADDITIVE_OPERATORS)) != null) {
      Object right = parseMultiplicative();
      left = OperatorEvaluator.evaluate(operator, left, right);
    }
    return left;
  }

  private Object parseMultiplicative() {
    Object left = parseUnary();
    String operator;
    while ((operator = matchAnyOperator(MULTIPLICATIVE_OPERATORS)) != null) {
      Object right = parseUnary();
      left = OperatorEvaluator.evaluate(operator, left, right);
    }
    return left;
  }

  private Object parseUnary() {
    if (matchOperator("!")) {
      return OperatorEvaluator.applyUnary("!", parseUnary());
    }
    if (matchOperator("-")) {
      return OperatorEvaluator.applyUnary("-", parseUnary());
    }
    return parsePrimary();
  }

  private Object parsePrimary() {
    if (match(TokenType.NUMBER)) {
      return previous().literal;
    }
    if (match(TokenType.STRING)) {
      return previous().literal;
    }
    if (match(TokenType.BOOLEAN)) {
      return previous().literal;
    }
    if (match(TokenType.NULL)) {
      return null;
    }
    if (match(TokenType.LPAREN)) {
      Object value = parseOr();
      consume(TokenType.RPAREN, "Expect ')' after expression");
      return value;
    }
    if (match(TokenType.IDENTIFIER)) {
      Token identifier = previous();
      if (match(TokenType.LPAREN)) {
        return finishFunctionCall(identifier.lexeme);
      }
      return resolveIdentifier(identifier.lexeme);
    }
    throw new IllegalArgumentException("Unexpected token: " + peek().lexeme);
  }

  private Object finishFunctionCall(String functionName) {
    List<Object> arguments = new ArrayList<>();
    if (!check(TokenType.RPAREN)) {
      do {
        arguments.add(parseOr());
      } while (match(TokenType.COMMA));
    }
    consume(TokenType.RPAREN, "Expect ')' after arguments for function '" + functionName + "'");
    ValidationRuleFunction function = objectResolver.resolveFunction(functionName);
    if (function == null) {
      throw new IllegalArgumentException("Unknown validation function: " + functionName);
    }
    return function.run(arguments.toArray());
  }

  private Object resolveIdentifier(String name) {
    Object variable = objectResolver.resolveVariable(name);

    if (variable != null) {
      System.out.println("[Fruits] : Resolved variable '" + name + "' with value: " + variable.getClass());
      return variable;
    }

    ValidationRuleFunction function = objectResolver.resolveFunction(name);
    if (function != null) {
      return function.run();
    }

    throw new IllegalArgumentException("Unknown identifier: " + name);
  }

  private boolean match(TokenType type) {
    if (check(type)) {
      advance();
      return true;
    }
    return false;
  }

  private boolean matchOperator(String operator) {
    if (check(TokenType.OPERATOR) && operator.equals(peek().lexeme)) {
      advance();
      return true;
    }
    return false;
  }

  private String matchAnyOperator(Set<String> candidates) {
    if (check(TokenType.OPERATOR) && candidates.contains(peek().lexeme)) {
      return advance().lexeme;
    }
    return null;
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) {
      return false;
    }
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) {
      position++;
    }
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == TokenType.EOF;
  }

  private Token peek() {
    return tokens.get(position);
  }

  private Token previous() {
    return tokens.get(position - 1);
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) {
      return advance();
    }
    throw new IllegalArgumentException(message);
  }
}