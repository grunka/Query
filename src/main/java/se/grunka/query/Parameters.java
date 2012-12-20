package se.grunka.query;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class Parameters {
    private static final Parameter NONE = new Parameter() {
        @Override
        public void set(PreparedStatement statement) throws SQLException {
        }
    };

    public static Parameter values(final Object... objects) {
        return new Parameter() {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                for (int i = 0; i < objects.length; i++) {
                    //TODO test
                    Object value = objects[i];
                    if (value instanceof String) {
                        statement.setString(i + 1, (String) value);
                    } else if (value instanceof Integer) {
                        statement.setInt(i + 1, (Integer) value);
                    } else if (value instanceof Double) {
                        statement.setDouble(i + 1, (Double) value);
                    } else if (value instanceof Long) {
                        statement.setLong(i + 1, (Long) value);
                    } else if (value instanceof Short) {
                        statement.setShort(i + 1, (Short) value);
                    } else if (value instanceof Boolean) {
                        statement.setBoolean(i + 1, (Boolean) value);
                    } else if (value instanceof Float) {
                        statement.setFloat(i + 1, (Float) value);
                    } else if (value instanceof BigDecimal) {
                        statement.setBigDecimal(i + 1, (BigDecimal) value);
                    } else if (value instanceof URL) {
                        statement.setURL(i + 1, (URL) value);
                    } else if (value instanceof Byte) {
                        statement.setByte(i + 1, (Byte) value);
                    } else if (value instanceof byte[]) {
                        statement.setBytes(i + 1, (byte[]) value);
                    } else if (value instanceof java.sql.Date) {
                        statement.setDate(i + 1, (java.sql.Date) value);
                    } else if (value instanceof Date) {
                        //TODO check that this is correct...
                        statement.setTimestamp(i + 1, new Timestamp(((Date) value).getTime()));
                    } else {
                        statement.setObject(i + 1, objects[i]);
                    }
                }
            }
        };
    }

    public static Parameter none() {
        return NONE;
    }
}
