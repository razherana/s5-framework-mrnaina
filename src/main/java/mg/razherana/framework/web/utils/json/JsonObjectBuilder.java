package mg.razherana.framework.web.utils.json;

import java.util.HashMap;

import mg.razherana.framework.web.utils.json.annotations.JsonAttribute;
import mg.razherana.framework.web.utils.json.types.JsonElement;
import mg.razherana.framework.web.utils.json.types.JsonObject;

public class JsonObjectBuilder {

  public static JsonObject buildFromClassAttributes(Object obj) {
    JsonObject jsonObject = new JsonObject(new HashMap<>());
    try {
      var fields = obj.getClass().getDeclaredFields();
      for (var field : fields) {
        JsonAttribute jsonAttribute = field.getAnnotation(JsonAttribute.class);

        if (jsonAttribute != null && jsonAttribute.ignore())
          continue;

        // Skip synthetic fields
        if (field.isSynthetic())
          continue;

        field.setAccessible(true);

        Object value = field.get(obj);
        String name = field.getName();

        if (jsonAttribute != null && !jsonAttribute.value().isEmpty())
          name = jsonAttribute.value();

        jsonObject.add(name, JsonElement.of(value));
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return jsonObject;
  }

}
