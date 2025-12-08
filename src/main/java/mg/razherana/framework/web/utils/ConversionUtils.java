package mg.razherana.framework.web.utils;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mg.razherana.framework.web.utils.json.types.JsonArray;
import mg.razherana.framework.web.utils.json.types.JsonElement;
import mg.razherana.framework.web.utils.json.types.JsonObject;
import mg.razherana.framework.web.utils.json.types.JsonType;
import mg.razherana.framework.web.utils.objectconversion.ConversionObjectUtils;

public class ConversionUtils {
  private ConversionUtils() {
  }

  public static Object convertStringOrArrToType(Object value, Class<?> targetType, Object outerInstance) {
    if (value == null)
      return null;

    if (value instanceof String)
      return convertStringToType((String) value, targetType);

    if (value instanceof String[])
      return convertStringToTypeArrays((String[]) value, targetType);

    if (value instanceof Map && !targetType.isPrimitive()) {
      @SuppressWarnings("unchecked")
      Map<String, Object> paramMap = (Map<String, Object>) value;
      return ConversionObjectUtils.convertMapToObject(paramMap,
          targetType, outerInstance);
    }

    throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());

  }

  public static Object convertStringToType(String value, Class<?> targetType) {
    if (value == null || value.isEmpty()) {
      return null;
    }

    if (targetType == String.class) {
      return value;
    } else if (targetType == int.class || targetType == Integer.class) {
      Object o;
      try {
        o = Integer.parseInt(value);
      } catch (Exception e) {
        o = null;
      }
      return o;
    } else if (targetType == long.class || targetType == Long.class) {
      Object o;
      try {
        o = Long.parseLong(value);
      } catch (Exception e) {
        o = null;
      }
      return o;
    } else if (targetType == double.class || targetType == Double.class) {
      Object o;
      try {
        o = Double.parseDouble(value);
      } catch (Exception e) {
        o = null;
      }
      return o;
    } else if (targetType == boolean.class || targetType == Boolean.class) {
      return Boolean.parseBoolean(value);
    } else if (targetType == float.class || targetType == Float.class) {
      Object o;
      try {
        o = Float.parseFloat(value);
      } catch (Exception e) {
        o = null;
      }
      return o;
    } else if (targetType == LocalDate.class) {
      Object o;

      try {
        o = LocalDate.parse(value);
      } catch (Exception e) {
        o = null;
      }

      return targetType.cast(o);
    } else if (targetType == LocalDateTime.class) {
      Object o;

      try {
        o = LocalDateTime.parse(value);
      } catch (Exception e) {
        o = null;
      }

      return targetType.cast(o);
    } else if (targetType.isArray()) {
      return convertStringToTypeArrays(value, targetType);
    }
    // Add more type conversions as needed

    throw new IllegalArgumentException("Unsupported target type: " + targetType.getName());
  }

  private static Object convertStringToTypeArrays(String[] values, Class<?> targetType) {
    if (values == null || values.length == 0 || !targetType.isArray()) {
      return null;
    }

    Class<?> componentType = targetType.getComponentType();

    List<Object> elementList = new ArrayList<>();

    for (int i = 0; i < values.length; i++) {
      Object element = convertStringToType(values[i], componentType);

      if (element == null)
        continue;

      elementList.add(element);
    }

    Object array = Array.newInstance(componentType, elementList.size());

    for (int i = 0; i < elementList.size(); i++) {
      Object element = elementList.get(i);

      Array.set(array, i, element);
    }

    return array;
  }

  private static Object convertStringToTypeArrays(String value, Class<?> targetType) {
    String[] stringValues = value.split(",");

    return convertStringToTypeArrays(stringValues, targetType);
  }

  private static List<Object> jsonElementToList(JsonElement body) {
    if (body == null || body.getType() != JsonType.ARRAY) {
      throw new IllegalArgumentException("Provided JsonElement is null or not a JsonArray");
    }

    List<Object> resultList = new ArrayList<>();
    JsonArray jsonArray = (JsonArray) body;

    // Foreach element, put into list
    jsonArray.getElements().forEach(element -> {
      if (element.getType() == JsonType.OBJECT) {
        resultList.add(jsonElementToMap(element));
      } else if (element.getType() == JsonType.ARRAY) {
        resultList.add(jsonElementToList(element));
      } else if (element.getType().isPrimitive()) {
        resultList.add(element.getAsPrimitive());
      } else {
        throw new IllegalArgumentException("Unsupported JsonElement type in array: " + element.getType());
      }
    });

    return resultList;
  }

  public static Map<String, Object> jsonElementToMap(JsonElement body) {
    if (body == null || body.getType() != JsonType.OBJECT) {
      throw new IllegalArgumentException("Provided JsonElement is null or not a JsonObject");
    }

    Map<String, Object> resultMap = new java.util.HashMap<>();

    // Foreach entry, put into map
    JsonObject jsonObject = (JsonObject) body;
    jsonObject.getMembers().forEach((k, v) -> {
      if (v.getType() == JsonType.OBJECT) {
        resultMap.put(k, jsonElementToMap(v));
      } else if (v.getType() == JsonType.ARRAY) {
        resultMap.put(k, jsonElementToList(v));
      } else if (v.getType().isPrimitive()) {
        resultMap.put(k, v.getAsPrimitive());
      } else {
        throw new IllegalArgumentException("Unsupported JsonElement type in object: " + v.getType());
      }
    });

    return resultMap;
  }
}
