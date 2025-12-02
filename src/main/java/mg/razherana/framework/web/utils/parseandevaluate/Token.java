package mg.razherana.framework.web.utils.parseandevaluate;

import java.math.BigDecimal;

public final class Token {
  final TokenType type;
  final String lexeme;
  final Object literal;

  private Token(TokenType type, String lexeme, Object literal) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
  }

  static Token operator(String lexeme) {
    return new Token(TokenType.OPERATOR, lexeme, null);
  }

  static Token lParen() {
    return new Token(TokenType.LPAREN, "(", null);
  }

  static Token rParen() {
    return new Token(TokenType.RPAREN, ")", null);
  }

  static Token comma() {
    return new Token(TokenType.COMMA, ",", null);
  }

  static Token stringLiteral(String value) {
    return new Token(TokenType.STRING, value, value);
  }

  static Token numberLiteral(BigDecimal value) {
    return new Token(TokenType.NUMBER, value.toString(), value);
  }

  static Token booleanLiteral(boolean value) {
    return new Token(TokenType.BOOLEAN, Boolean.toString(value), value);
  }

  static Token nullLiteral() {
    return new Token(TokenType.NULL, "null", null);
  }

  static Token identifier(String value) {
    return new Token(TokenType.IDENTIFIER, value, value);
  }

  static Token eof() {
    return new Token(TokenType.EOF, "", null);
  }
}