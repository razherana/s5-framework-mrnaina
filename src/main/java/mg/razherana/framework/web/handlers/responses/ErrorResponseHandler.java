package mg.razherana.framework.web.handlers.responses;

import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResponseContainer;
import mg.razherana.framework.web.exceptions.http.HttpException;

public class ErrorResponseHandler implements ResponseHandler {

  @Override
  public void handleResponse(ResponseContainer rc, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    Exception e = (Exception) rc.getReturnObject();

    if (e instanceof HttpException) {
      HttpException httpEx = (HttpException) e;
      response.sendError(httpEx.getStatusCode(), httpEx.getMessage());
      return;
    }

    String htmlErrorPage = generateErrorHtml(e, request);

    response.setStatus(500);
    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().write(htmlErrorPage);
  }

  private String generateErrorHtml(Exception e, HttpServletRequest request) {
    Throwable jasperRootCause = extractJasperRootCause(e);

    String stackTrace = Arrays.stream(e.getStackTrace())
        .map(StackTraceElement::toString)
        .collect(Collectors.joining("\n"));

    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html>")
        .append("<html>")
        .append("<head>")
        .append("    <title>Error Page</title>")
        .append("    <meta charset=\"UTF-8\">")
        .append("    <style>")
        .append(
            "        .error-container { margin: 20px; padding: 20px; border: 1px solid #ccc; background-color: #f9f9f9; }")
        .append("        .error-header { color: #d9534f; font-size: 24px; margin-bottom: 20px; }")
        .append(
            "        .stack-trace { background: #f8f8f8; padding: 10px; overflow: auto; border: 1px solid #ddd; font-family: monospace; }")
        .append("        h3 { color: #333; margin-top: 20px; }")
        .append("        p { margin: 5px 0; }")
        .append("        strong { color: #555; }")
        .append("        pre { white-space: pre-wrap; word-wrap: break-word; margin: 0; }")
        .append("    </style>")
        .append("</head>")
        .append("<body>")
        .append("    <div class=\"error-container\">")
        .append("        <h1 class=\"error-header\">An Error Occurred</h1>")
        .append("        <h3>Exception Information:</h3>")
        .append("        <p><strong>Type:</strong> ").append(escapeHtml(e.getClass().getName())).append("</p>")
        .append("        <p><strong>Message:</strong> ")
        .append(escapeHtml(getMessageOrDefault(e))).append("</p>")
        .append("        <h3>Stack Trace:</h3>")
        .append("        <div class=\"stack-trace\">")
        .append("            <pre>").append(escapeHtml(stackTrace)).append("</pre>")
        .append("        </div>")
        .append(buildJasperRootCauseSection(jasperRootCause))
        .append("        <h3>Request Information:</h3>")
        .append("        <p><strong>URL:</strong> ")
        .append(escapeHtml(request.getRequestURL() != null ? request.getRequestURL().toString() : "")).append("</p>")
        .append("        <p><strong>Query String:</strong> ")
        .append(escapeHtml(request.getQueryString() != null ? request.getQueryString() : "")).append("</p>")
        .append("        <p><strong>Method:</strong> ")
        .append(escapeHtml(request.getMethod() != null ? request.getMethod() : "")).append("</p>")
        .append("    </div>")
        .append("</body>")
        .append("</html>");

    return html.toString();
  }

  private String escapeHtml(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  private String getMessageOrDefault(Throwable throwable) {
    if (throwable == null || throwable.getMessage() == null) {
      return "No message available";
    }
    return throwable.getMessage();
  }

  private String buildJasperRootCauseSection(Throwable jasperRootCause) {
    if (jasperRootCause == null) {
      return "";
    }

    String rootStackTrace = Arrays.stream(jasperRootCause.getStackTrace())
        .map(StackTraceElement::toString)
        .collect(Collectors.joining("\n"));

    StringBuilder section = new StringBuilder();
    section.append("        <h3>JSP Root Cause:</h3>")
        .append("        <p><strong>Type:</strong> ")
        .append(escapeHtml(jasperRootCause.getClass().getName()))
        .append("</p>")
        .append("        <p><strong>Message:</strong> ")
        .append(escapeHtml(getMessageOrDefault(jasperRootCause)))
        .append("</p>")
        .append("        <div class=\"stack-trace\">")
        .append("            <pre>")
        .append(escapeHtml(rootStackTrace))
        .append("</pre>")
        .append("        </div>");

    return section.toString();
  }

  private Throwable extractJasperRootCause(Exception e) {
    if (e == null) {
      return null;
    }

    boolean isJasperException = "JasperException".equals(e.getClass().getSimpleName())
        || "org.apache.jasper.JasperException".equals(e.getClass().getName());

    if (isJasperException) {
      return e.getCause();
    }

    if (e instanceof ServletException) {
      ServletException servletException = (ServletException) e;
      Throwable rootCause = servletException.getRootCause();
      if (rootCause != null && rootCause != e) {
        return rootCause;
      }
    }

    Throwable cause = e.getCause();
    if (cause != null && cause != e) {
      return cause;
    }

    return e;
  }
}
