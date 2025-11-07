package mg.razherana.framework.web.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResponseContainer;

public class ModelView {
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final ResponseContainer responseContainer = new ResponseContainer(null, null);

  public ModelView(HttpServletRequest request, HttpServletResponse response) {
    this.request = request;
    this.response = response;
  }

  /**
   * Get the header from request
   * 
   * @param name
   * @return
   */
  public String header(String name) {
    return request.getHeader(name);
  }

  /**
   * Put header in response
   * 
   * @param name
   * @param value
   * @return
   */
  public ModelView header(String name, String value) {
    response.setHeader(name, value);
    return this;
  }

  public Object attribute(String name) {
    return request.getAttribute(name);
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
