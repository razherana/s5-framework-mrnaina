package mg.razherana.framework.web.utils.http.readers;

import java.util.ArrayList;
import java.util.Map;

import mg.razherana.framework.web.utils.json.types.JsonArray;
import mg.razherana.framework.web.utils.json.types.JsonElement;
import mg.razherana.framework.web.utils.json.types.JsonObject;
import mg.razherana.framework.web.utils.json.types.JsonType;

public class JsonDataToMap implements DataToMap<String> {
  private Object _toMap(JsonElement element) {
    switch (element.getType()) {
      case OBJECT:
        var obj = (JsonObject) element;
        Map<String, Object> result = new java.util.LinkedHashMap<>();

        for (String key : obj.getMembers().keySet())
          result.put(key, _toMap(obj.get(key)));
        return result;

      case ARRAY:
        var arr = new ArrayList<>();
        for (JsonElement item : ((JsonArray) element).getElements())
          arr.add(_toMap(item));
        return arr;
      
      default:
        return element.getAsPrimitive();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, ?> toMap(String data) {
    // To json first
    JsonElement element = JsonElement.from(data);

    if(element.getType() != JsonType.OBJECT) {
      throw new IllegalArgumentException("JSON data must represent an object at the root level.");
    }

    // Then to map
    var result = _toMap(element);

    return (Map<String, ?>) result;    
  }
}
