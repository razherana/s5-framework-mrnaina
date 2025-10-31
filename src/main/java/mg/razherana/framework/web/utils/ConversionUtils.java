package mg.razherana.framework.web.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ConversionUtils {
  private ConversionUtils() {
  }

  public static Object convertStringToType(String value, Class<?> targetType) {
    if (value == null) {
      return null;
    }
    if (targetType == String.class) {
      return value;
    } else if (targetType == int.class || targetType == Integer.class) {
      return Integer.parseInt(value);
    } else if (targetType == long.class || targetType == Long.class) {
      return Long.parseLong(value);
    } else if (targetType == double.class || targetType == Double.class) {
      return Double.parseDouble(value);
    } else if (targetType == boolean.class || targetType == Boolean.class) {
      return Boolean.parseBoolean(value);
    } else if (targetType == float.class || targetType == Float.class) {
      return Float.parseFloat(value);
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

  private static Object convertStringToTypeArrays(String value, Class<?> targetType) {
    String[] stringValues = value.split(",");
    Class<?> componentType = targetType.getComponentType();

    if (componentType == String.class) {
      String[] array = stringValues;
      return array;
    } else if (componentType == int.class || componentType == Integer.class) {
      Integer[] array = new Integer[stringValues.length];
      for (int i = 0; i < stringValues.length; i++) {
        array[i] = Integer.parseInt(stringValues[i].trim());
      }
      return array;
    } else if (componentType == long.class || componentType == Long.class) {
      Long[] array = new Long[stringValues.length];
      for (int i = 0; i < stringValues.length; i++) {
        array[i] = Long.parseLong(stringValues[i].trim());
      }
      return array;
    } else if (componentType == double.class || componentType == Double.class) {
      Double[] array = new Double[stringValues.length];
      for (int i = 0; i < stringValues.length; i++) {
        array[i] = Double.parseDouble(stringValues[i].trim());
      }
      return array;
    } else if (componentType == boolean.class || componentType == Boolean.class) {
      Boolean[] array = new Boolean[stringValues.length];
      for (int i = 0; i < stringValues.length; i++) {
        array[i] = Boolean.parseBoolean(stringValues[i].trim());
      }
      return array;
    } else if (componentType == float.class || componentType == Float.class) {
      Float[] array = new Float[stringValues.length];
      for (int i = 0; i < stringValues.length; i++) {
        array[i] = Float.parseFloat(stringValues[i].trim());
      }
      return array;
    } else if (componentType == LocalDate.class) {
      LocalDate[] array = new LocalDate[stringValues.length];
      for (int i = 0; i < stringValues.length; i++) {
        try {
          array[i] = LocalDate.parse(stringValues[i].trim());
        } catch (Exception e) {
          array[i] = null;
        }
      }
      return array;
    } else if (componentType == LocalDateTime.class) {
      LocalDateTime[] array = new LocalDateTime[stringValues.length];
      for (int i = 0; i < stringValues.length; i++) {
        try {
          array[i] = LocalDateTime.parse(stringValues[i].trim());
        } catch (Exception e) {
          array[i] = null;
        }
      }
      return array;
    }

    throw new IllegalArgumentException("Unsupported array component type: " + componentType.getName());
  }
}
