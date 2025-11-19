package mg.razherana.framework.web.utils.jsp.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import mg.razherana.framework.web.utils.jsp.JspUtil;

public class AttributeUtil extends JspUtil {
  private static final String VIEW_NAME = "$attribute";

  @Override
  public String getViewName() {
    return VIEW_NAME;
  }

  @Override
  public Object run(Object... args) {
    if (args == null)
      Objects.requireNonNull(args, "The argument of " + getViewName() + " cannot be null");

    if (getData().get("request") == null || !(getData().get("request") instanceof HttpServletRequest)) {
      throw new IllegalStateException("Current request is not given properly to " + getViewName());
    }

    HttpServletRequest request = (HttpServletRequest) getData().get("request");

    if (args.length == 0) {
      Map<String, Object> mapOfAttributes = new HashMap<>();
      request.getAttributeNames()
          .asIterator()
          .forEachRemaining((name) -> {
            mapOfAttributes.put(name, request.getAttribute(name));
          });

      return mapOfAttributes;
    }

    if (args.length == 1) {
      if (!(args[0] instanceof String))
        throw new IllegalArgumentException("The first argument of " + getViewName() + " must be a String");

      return request.getAttribute((String) args[0]);
    }

    List<Object> objects = new ArrayList<>();

    for (Object object : args) {
      if (!(object instanceof String))
        throw new IllegalArgumentException("All of the arguments of " + getViewName() + " must all be string");

      objects.add(request.getAttribute(object + ""));
    }

    return objects;
  }

}
