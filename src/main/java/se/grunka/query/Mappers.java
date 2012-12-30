package se.grunka.query;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Mappers {
    public static <T> Mapper<T> object(Class<T> type) {
        final Constructor<T> constructor;
        try {
            constructor = type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No zero argument constructor found for " + type.getName(), e);
        }
        final Map<String, Field> objectFields = new HashMap<>();

        for (Field field : type.getDeclaredFields()) {
            Column annotation = field.getAnnotation(Column.class);
            if (annotation != null) {
                String columnName = annotation.value();
                if (objectFields.containsKey(columnName)) {
                    throw new IllegalArgumentException("Duplicate column name found " + columnName);
                }
                int modifiers = field.getModifiers();
                if (Modifier.isFinal(modifiers) && field.getType().isPrimitive()) {
                    throw new IllegalArgumentException("Will not be able to set the field for @Column(\"" + columnName + "\") since it is a primitive type marked as final");
                }
                field.setAccessible(true);
                objectFields.put(columnName, field);
            }
        }
        if (objectFields.isEmpty()) {
            throw new IllegalArgumentException("No fields annotated with @Column found in " + type.getName());
        }

        return new Mapper<T>() {
            @Override
            public T map(ResultSet resultSet) throws SQLException {
                try {
                    T instance = constructor.newInstance();
                    for (Map.Entry<String, Field> entry : objectFields.entrySet()) {
                        Field field = entry.getValue();
                        setValue(resultSet, entry.getKey(), field, instance);
                    }
                    return instance;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new Error("Could not instantiate object", e);
                }
            }
        };
    }

    private static <T> void setValue(ResultSet resultSet, String columnName, Field field, T instance) throws SQLException, IllegalAccessException {
        Class<?> targetType = field.getType();
        if (targetType == String.class) {
            field.set(instance, resultSet.getString(columnName));
        } else if (targetType == Integer.class || targetType == int.class) {
            field.set(instance, resultSet.getInt(columnName));
        } else if (targetType == Double.class || targetType == double.class) {
            field.set(instance, resultSet.getDouble(columnName));
        } else if (targetType == Long.class || targetType == long.class) {
            field.set(instance, resultSet.getLong(columnName));
        } else if (targetType == Short.class || targetType == short.class) {
            field.set(instance, resultSet.getShort(columnName));
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            field.set(instance, resultSet.getBoolean(columnName));
        } else if (targetType == Float.class || targetType == float.class) {
            field.set(instance, resultSet.getFloat(columnName));
        } else if (targetType == BigDecimal.class) {
            field.set(instance, resultSet.getBigDecimal(columnName));
        } else if (targetType == URL.class) {
            field.set(instance, resultSet.getURL(columnName));
        } else if (targetType == Byte.class || targetType == byte.class) {
            field.set(instance, resultSet.getByte(columnName));
        } else if (targetType == byte[].class) {
            field.set(instance, resultSet.getBytes(columnName));
        } else if (targetType == java.sql.Date.class) {
            field.set(instance, resultSet.getDate(columnName));
        } else if (targetType == Date.class) {
            //TODO check that this is correct...
            field.set(instance, new Date(resultSet.getTimestamp(columnName).getTime()));
        } else {
            field.set(instance, resultSet.getObject(columnName));
        }
    }
}
