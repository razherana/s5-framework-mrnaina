package mg.razherana.framework.web.exceptions.http;

import jakarta.servlet.http.HttpServletResponse;

public class UnauthorizedException extends HttpException {
  public UnauthorizedException(String message) {
    super(message, HttpServletResponse.SC_UNAUTHORIZED);
  }
}
