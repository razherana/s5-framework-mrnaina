package mg.razherana.framework.web.servlets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.App;
import mg.razherana.framework.App.InitKey;
import mg.razherana.framework.web.containers.WebRouteContainer;
import mg.razherana.framework.web.exceptions.http.HttpException;
import mg.razherana.framework.web.exceptions.http.NotFoundException;
import mg.razherana.framework.web.routing.WebExecutor;

@WebServlet("/")
public class FrontServlet extends HttpServlet {

  private App app;

  @Override
  public void init() throws ServletException {
    super.init();

    // Initialize the application
    app = new App(null, null);

    // Get the base package from init parameters
    String basePackage = getServletContext().getInitParameter(InitKey.BASE_PACKAGE.getKey());

    if (basePackage == null || basePackage.isEmpty()) {
      throw new ServletException("Base package not specified in servlet init parameters. Please set '"
          + InitKey.BASE_PACKAGE.getKey() + "' parameter.");
    }

    // Find all controllers at startup
    app.scanControllers(basePackage);

    // Initialize WebFinder and WebMapper
    app.initWeb();
  }

  private boolean resourceExists(HttpServletRequest request) {
    try {
      URL resource = getServletContext().getResource(request.getRequestURI());
      return resource != null;
    } catch (MalformedURLException e) {
      return false;
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!resourceExists(req)) {
      handleRequest(req, resp);
      return;
    }
    super.doGet(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!resourceExists(req)) {
      handleRequest(req, resp);
      return;
    }
    super.doPost(req, resp);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!resourceExists(req)) {
      handleRequest(req, resp);
      return;
    }
    super.doPut(req, resp);
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!resourceExists(req)) {
      handleRequest(req, resp);
      return;
    }
    super.doOptions(req, resp);
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!resourceExists(req)) {
      handleRequest(req, resp);
      return;
    }
    super.doHead(req, resp);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!resourceExists(req)) {
      handleRequest(req, resp);
      return;
    }
    super.doDelete(req, resp);
  }

  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!resourceExists(req)) {
      handleRequest(req, resp);
      return;
    }
    super.doTrace(req, resp);
  }

  private void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      WebRouteContainer webRouteContainer = app.getWebMapper().findRouteMethod(request);

      // Check if route found
      if (webRouteContainer == null) {
        throw new NotFoundException("Resource not found: " + request.getRequestURI());
      }

      // Execute the route
      WebExecutor webExecutor = new WebExecutor(webRouteContainer);
      webExecutor.execute(request, response);
    } catch (HttpException httpEx) {
      response.sendError(httpEx.getStatusCode(), httpEx.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
