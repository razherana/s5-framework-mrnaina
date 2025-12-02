package mg.razherana.framework.web.utils.objectconversion;

import java.util.Map;

import mg.razherana.framework.web.utils.ConversionUtils;

public class ConversionObjectUtils {
  private ConversionObjectUtils() {
  }

  public static boolean isPrimitiveOrWrapper(Class<?> type) {
    return type.isPrimitive() || type == Boolean.class || type == Byte.class || type == Character.class
        || type == Short.class || type == Integer.class || type == Long.class || type == Float.class
        || type == Double.class;
  }

  public static <T> T convertMapToObject(Map<String, Object> parametersMap, Class<T> targetType, Object outerInstance) {

    Object _instance = null;
    if (targetType.getEnclosingClass() != null && !targetType.isAnonymousClass())
      _instance = instanciate(targetType, outerInstance);
    else
      _instance = instanciate(targetType, null);

    @SuppressWarnings("unchecked")
    final T instance = (T) _instance;

    parametersMap.forEach((key, value) -> {
      try {
        var field = targetType.getDeclaredField(key);
        field.setAccessible(true);

        Object convertedValue = ConversionUtils.convertStringOrArrToType(value, field.getType());

        field.set(instance, convertedValue);
      } catch (NoSuchFieldException e) {
        System.out.println("[Fruits] : Field not found: " + key + " on type: " + targetType.getName());
      } catch (IllegalAccessException e) {
        throw new ConversionObjectException("Failed to set field value: " + key + " on type: " + targetType.getName(),
            e);
      }
    });

    return instance;
  }

  private static <T> T instanciate(Class<T> targetType, Object outerInstance) {
    try {
      if (outerInstance != null)
        return targetType.getDeclaredConstructor(outerInstance.getClass()).newInstance(outerInstance);

      return targetType.getDeclaredConstructor().newInstance();
    } catch (NoSuchMethodException e) {
      throw new ConversionObjectException("No default constructor found for type: " + targetType.getName(), e);
    } catch (Exception e) {
      throw new ConversionObjectException("Failed to instantiate object of type: " + targetType.getName(), e);
    }
  }
}
