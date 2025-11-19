package mg.razherana.framework.web.utils.jsp;

import java.util.HashMap;
import java.util.Map;

/**
 * All extending class must have a no args constructor
 */
public abstract class JspUtil implements JspUtilInterface {
  private final Map<String, Object> data = new HashMap<>();

  /**
   * Run the util
   * 
   * @param args The arguments of the method call
   * @return
   */
  public abstract Object run(Object... args);

  public JspUtil() {
  }

  /**
   * @return the data
   */
  public Map<String, Object> getData() {
    return data;
  }
}
