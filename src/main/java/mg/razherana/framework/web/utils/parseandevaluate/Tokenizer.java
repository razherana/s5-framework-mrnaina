package mg.razherana.framework.web.utils.parseandevaluate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import mg.razherana.framework.web.utils.validation.ValidationEvaluator;

public final class Tokenizer {
  private final List<String> rawTokens;
  private final List<Token> tokens = new ArrayList<>();

  public Tokenizer(List<String> rawTokens) {
    this.rawTokens = rawTokens;
  }

  public List<Token> tokenize() {
    for (String raw : rawTokens) {
      if (raw == null || raw.isBlank()) {
        continue;
      }
      scan(raw);
    }
    tokens.add(Token.eof());
    return tokens;
  }

  private void scan(String raw) {
    int index = 0;
    while (index < raw.length()) {
      char current = raw.charAt(index);
      if (Character.isWhitespace(current)) {
        index++;
        continue;
      }
      if (current == '\'') {
        index = scanStringLiteral(raw, index);
        continue;
      }
      if (current == '(') {
        tokens.add(Token.lParen());
        index++;
        continue;
      }
      if (current == ')') {
        tokens.add(Token.rParen());
        index++;
        continue;
      }
      if (current == ',') {
        tokens.add(Token.comma());
        index++;
        continue;
      }
      if (isOperatorChar(current)) {
        index = scanOperator(raw, index);
        continue;
      }
      index = scanIdentifierOrNumber(raw, index);
    }
  }

  private int scanStringLiteral(String raw, int start) {
    int index = start + 1;
    while (index < raw.length() && raw.charAt(index) != '\'') {
      index++;
    }
    String value;
    if (index < raw.length()) {
      value = raw.substring(start + 1, index);
      index++; // Skip closing quote
    } else {
      value = raw.substring(start + 1);
      index = raw.length();
    }
    tokens.add(Token.stringLiteral(value));
    return index;
  }

  private int scanOperator(String raw, int start) {
    int index = start;
    while (index < raw.length() && isOperatorChar(raw.charAt(index))) {
      index++;
    }
    splitOperatorSegment(raw.substring(start, index));
    return index;
  }

  private void splitOperatorSegment(String segment) {
    int cursor = 0;
    while (cursor < segment.length()) {
      if (cursor + 2 <= segment.length()) {
        String twoChars = segment.substring(cursor, cursor + 2);
        if (ValidationEvaluator.OPERATOR_PRECEDENCE.containsKey(twoChars)) {
          tokens.add(Token.operator(twoChars));
          cursor += 2;
          continue;
        }
      }
      String single = segment.substring(cursor, cursor + 1);
      if (ValidationEvaluator.OPERATOR_PRECEDENCE.containsKey(single)) {
        tokens.add(Token.operator(single));
        cursor++;
        continue;
      }
      throw new IllegalArgumentException("Unknown operator sequence: " + segment);
    }
  }

  private int scanIdentifierOrNumber(String raw, int start) {
    int index = start;
    while (index < raw.length()) {
      char ch = raw.charAt(index);
      if (Character.isWhitespace(ch) || ch == '(' || ch == ')' || ch == ',' || ch == '\'' || isOperatorChar(ch)) {
        break;
      }
      index++;
    }
    String lexeme = raw.substring(start, index);
    tokens.add(classify(lexeme));
    return index;
  }

  private Token classify(String lexeme) {
    if (ValidationEvaluator.OPERATOR_PRECEDENCE.containsKey(lexeme)) {
      return Token.operator(lexeme);
    }
    if ("true".equalsIgnoreCase(lexeme) || "false".equalsIgnoreCase(lexeme)) {
      return Token.booleanLiteral(Boolean.parseBoolean(lexeme));
    }
    if ("null".equalsIgnoreCase(lexeme)) {
      return Token.nullLiteral();
    }
    try {
      return Token.numberLiteral(new BigDecimal(lexeme));
    } catch (NumberFormatException ignored) {
      // Fall through to identifier
    }
    return Token.identifier(lexeme);
  }

  private boolean isOperatorChar(char ch) {
    return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%' || ch == '<' || ch == '>'
        || ch == '=' || ch == '!' || ch == '&' || ch == '|';
  }
}