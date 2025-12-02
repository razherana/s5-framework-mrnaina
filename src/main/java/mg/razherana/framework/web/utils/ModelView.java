package mg.razherana.framework.web.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResponseContainer;

public class ModelView {
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private ResponseContainer responseContainer;

  public ModelView(HttpServletRequest request, HttpServletResponse response) {
    this.request = request;
    this.response = response;
    this.responseContainer = new ResponseContainer(null, null);
  }

  /**
   * Retrieves the value of the specified header from the request.
   * 
   * @param name the header name to retrieve from the request
   * @return the header value, or {@code null} if not found
   */
  public String header(String name) {
    return request.getHeader(name);
  }

  /**
   * Put header in response.
   * 
   * @param name  the name of the header to set
   * @param value the value of the header to set
   * @return this ModelView instance for method chaining
   */
  public ModelView header(String name, String value) {
    response.setHeader(name, value);
    return this;
  }
  
  @SuppressWarnings("unchecked")
  public <T> T attribute(String name) {
    return (T) request.getAttribute(name);
  }

  public ModelView attribute(String name, Object value) {
    request.setAttribute(name, value);
    return this;
  }

  public ResponseContainer view(final String viewName) {
    responseContainer.setReturnType("view");
    responseContainer.setReturnObject(viewName);

    return responseContainer;
  }

  public ResponseContainer write(final String value) {
    responseContainer.setReturnType("write");
    responseContainer.setReturnObject(value);

    return responseContainer;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

}
