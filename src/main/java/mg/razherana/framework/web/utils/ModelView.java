package mg.razherana.framework.web.utils;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mg.razherana.framework.web.containers.ResponseContainer;
import mg.razherana.framework.web.containers.WebRouteContainer;
import mg.razherana.framework.web.utils.http.RequestBody;
import mg.razherana.framework.web.utils.http.ResponseBody;
import mg.razherana.framework.web.utils.json.types.JsonElement;
import mg.razherana.framework.web.utils.jsp.defaults.RouteUtil;

public class ModelView {
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private ResponseContainer responseContainer;
  private final ResponseBody responseBody;
  private final RequestBody requestBody;
  private WebRouteContainer webRouteContainer;

  public ModelView(HttpServletRequest request, HttpServletResponse response, RequestBody requestBody) {
    this.request = request;
    this.response = response;
    this.responseContainer = new ResponseContainer(null, null);
    this.responseBody = new ResponseBody();
    this.requestBody = requestBody;
  }

  public ModelView(HttpServletRequest request, HttpServletResponse response, RequestBody requestBody,
      WebRouteContainer webRouteContainer) {
    this(request, response, requestBody);
    this.webRouteContainer = webRouteContainer;
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

  /**
   * Redirect to an absolute location.
   * 
   * @param location
   * @return
   * @throws IOException
   */
  public ResponseContainer redirectAbsolute(String location) throws IOException {
    return new ResponseContainer(location, "redirect");
  }

  /**
   * Redirect to a location relative to the context path.
   * 
   * @param location
   * @return
   * @throws IOException
   */
  public ResponseContainer redirectRelative(String location) throws IOException {
    String contextPath = request.getContextPath();
    if (!location.startsWith("/")) {
      location = "/" + location;
    }
    if (contextPath.endsWith("/")) {
      contextPath = contextPath.substring(0, contextPath.length() - 1);
    }

    return redirectAbsolute(contextPath + location);
  }

  /**
   * Redirect to a route relative to the controller.
   * Make sure to set path parameters if the route requires them.
   * 
   * Eg: routeRelative("viewProfile", "id=123", "?filter=value")
   * 
   * @param routeAlias
   * @param pathParameters
   * @return
   * @throws IOException
   */
  public ResponseContainer routeRelative(String routeAlias, String... pathParameters) throws IOException {
    var alias = webRouteContainer
        .getControllerContainer()
        .getControllerAnnotation()
        .alias();

    if (alias.isBlank())
      alias = webRouteContainer.getControllerClass().getSimpleName().toLowerCase();

    Object[] args = new Object[pathParameters.length + 1];
    args[0] = alias + "/" + routeAlias;
    System.arraycopy(pathParameters, 0, args, 1, pathParameters.length);

    String path = (String) new RouteUtil(request).run(args);

    return redirectAbsolute(path);
  }

  /**
   * Get the real path of a relative path in the server.
   * 
   * @param relativePath
   * @return
   */
  public String realPath(String relativePath) {
    return request.getServletContext().getRealPath(relativePath);
  }

  /**
   * Redirect to a route absolute to the application.
   * Eg: routeAbsolute("usercontroller/updateForm", "id=123", "?queryParam=value")
   * 
   * @param controllerAndRoute
   * @param pathParameters
   * @return
   * @throws IOException
   */
  public ResponseContainer routeAbsolute(String controllerAndRoute, String... pathParameters) throws IOException {
    Object[] args = new Object[pathParameters.length + 1];
    args[0] = controllerAndRoute;
    System.arraycopy(pathParameters, 0, args, 1, pathParameters.length);
    
    String path = (String) new RouteUtil(request).run(args);

    return redirectAbsolute(path);
  }

  @SuppressWarnings("unchecked")
  public <T> T attribute(String name) {
    return (T) request.getAttribute(name);
  }

  public ModelView attribute(String name, Object value) {
    request.setAttribute(name, value);
    responseBody.put(name, value);
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

  public ResponseContainer json() {
    responseContainer.setReturnType("json");
    responseContainer.setReturnObject(responseBody);

    return responseContainer;
  }

  public ResponseContainer json(JsonElement element) {
    responseContainer.setReturnType("json");
    responseContainer.setReturnObject(element);

    return responseContainer;
  }

  public void saveTo(Part part, String path) throws IOException {
    part.write(path);
  }

  public void saveTo(String partName, String path) throws IOException, ServletException {
    Part part = (Part) requestBody.get(partName);

    if (part != null)
      part.write(path);
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  /**
   * @return the responseContainer
   */
  public ResponseContainer getResponseContainer() {
    return responseContainer;
  }

  /**
   * @return the responseBody
   */
  public ResponseBody getResponseBody() {
    return responseBody;
  }

  /**
   * @return the requestBody
   */
  public RequestBody getRequestBody() {
    return requestBody;
  }

  /**
   * @return the webRouteContainer
   */
  public WebRouteContainer getWebRouteContainer() {
    return webRouteContainer;
  }

}
