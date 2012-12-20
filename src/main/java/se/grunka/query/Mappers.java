package se.grunka.query;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
                        field.set(instance, getValue(resultSet, entry.getKey(), field.getType()));
                    }
                    return instance;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new Error("Could not instantiate object", e);
                }
            }
        };
    }

    private static Object getValue(ResultSet resultSet, String columnName, Class<?> targetType) throws SQLException {
        //TODO one big unit test :)
        //TODO order in most used order? Does it matter?
        if (targetType == String.class) {
            return resultSet.getString(columnName);
        }
        if (targetType == Integer.class || targetType == int.class) {
            return resultSet.getInt(columnName);
        }
        if (targetType == Double.class || targetType == double.class) {
            return resultSet.getDouble(columnName);
        }
        if (targetType == Long.class || targetType == long.class) {
            return resultSet.getLong(columnName);
        }
        if (targetType == Short.class || targetType == short.class) {
            return resultSet.getShort(columnName);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return resultSet.getBoolean(columnName);
        }
        if (targetType == Float.class || targetType == float.class) {
            return resultSet.getFloat(columnName);
        }
        if (targetType == BigDecimal.class) {
            return resultSet.getBigDecimal(columnName);
        }
        if (targetType == URL.class) {
            return resultSet.getURL(columnName);
        }
        if (targetType == Byte.class || targetType == byte.class) {
            return resultSet.getByte(columnName);
        }
        if (targetType == byte[].class) {
            return resultSet.getBytes(columnName);
        }
        if (targetType == java.sql.Date.class) {
            return resultSet.getDate(columnName);
        }
        if (targetType == Date.class) {
            //TODO check that this is correct...
            return new Date(resultSet.getTimestamp(columnName).getTime());
        }
        return resultSet.getObject(columnName);
    }
}
