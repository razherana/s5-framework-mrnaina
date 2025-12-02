package mg.razherana.framework.web.utils.objectconversion;

import java.lang.reflect.Constructor;
import java.util.Map;

import mg.razherana.framework.web.utils.ConversionUtils;

public class ConversionObjectUtils {
  private ConversionObjectUtils() {
  }

  public static <T> T convertMapToObject(Map<String, Object> parametersMap, Class<T> targetType, Object outerInstance) {
    if (targetType == null) {
      throw new ConversionObjectException("Target type cannot be null");
    }

    if (targetType.isRecord()) {
      return convertMapToRecord(parametersMap, targetType, outerInstance);
    }

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

        if (field.getType().isPrimitive() && convertedValue == null) {
          // Skip setting primitive fields to null to avoid exceptions
          System.out.println(
              "[Fruits] : Skipping setting primitive field to null: " + key + " on type: " + targetType.getName());
          return;
        }

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

  private static <T> T convertMapToRecord(Map<String, Object> parametersMap, Class<T> targetType,
      Object outerInstance) {
    if (!targetType.isRecord()) {
      throw new ConversionObjectException("Target type is not a record: " + targetType.getName());
    }

    var recordComponents = targetType.getRecordComponents();
    Object[] constructorArgs = new Object[recordComponents.length];
    for (int i = 0; i < recordComponents.length; i++) {
      var component = recordComponents[i];
      String componentName = component.getName();
      Class<?> componentType = component.getType();

      Object value = parametersMap.get(componentName);
      Object convertedValue = ConversionUtils.convertStringOrArrToType(value, componentType);

      if (componentType.isPrimitive() && convertedValue == null) {
        // Skip setting primitive fields to null to avoid exceptions
        System.out.println(
            "[Fruits] : Skipping setting primitive record component to null: " + componentName + " on type: "
                + targetType.getName());
        continue;
      }

      constructorArgs[i] = convertedValue;
    }

    try {
      Constructor<T> constructor = targetType.getDeclaredConstructor(
          java.util.Arrays.stream(recordComponents)
              .map(rc -> rc.getType())
              .toArray(Class[]::new));
      constructor.setAccessible(true);
      return constructor.newInstance(constructorArgs);
    } catch (NoSuchMethodException e) {
      throw new ConversionObjectException("No matching constructor found for record type: " + targetType.getName(), e);
    } catch (Exception e) {
      throw new ConversionObjectException("Failed to instantiate record of type: " + targetType.getName(), e);
    }
  }

  private static <T> T instanciate(Class<T> targetType, Object outerInstance) {
    try {
      Constructor<T> constructor;

      if (outerInstance != null) {
        constructor = targetType.getDeclaredConstructor(outerInstance.getClass());
        constructor.setAccessible(true);
        return constructor.newInstance(outerInstance);
      }

      constructor = targetType.getDeclaredConstructor();
      constructor.setAccessible(true);
      return targetType.getDeclaredConstructor().newInstance();
    } catch (NoSuchMethodException e) {
      throw new ConversionObjectException("No default constructor found for type: " + targetType.getName(), e);
    } catch (Exception e) {
      throw new ConversionObjectException("Failed to instantiate object of type: " + targetType.getName(), e);
    }
  }
}
