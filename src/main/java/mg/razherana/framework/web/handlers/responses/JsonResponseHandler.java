package mg.razherana.framework.web.handlers.responses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.framework.web.containers.ResponseContainer;
import mg.razherana.framework.web.utils.http.ResponseBody;
import mg.razherana.framework.web.utils.json.types.JsonElement;

public class JsonResponseHandler implements ResponseHandler {

  @Override
  public void handleResponse(ResponseContainer rc, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    Object payload = rc.getReturnObject();
    JsonElement jsonElement = toJsonElement(payload);

    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json;charset=UTF-8");
    response.setHeader("Content-Type", "application/json;charset=UTF-8");
    response.getWriter().write(jsonElement.toString());
  }

  private JsonElement toJsonElement(Object payload) {
    if (payload == null) {
      return JsonElement.NULL;
    }

    if (payload instanceof JsonElement element) {
      return element;
    }

    if (payload instanceof ResponseBody responseBody) {
      return responseBody.toJsonElement();
    }

    return JsonElement.of(payload);
  }
}
