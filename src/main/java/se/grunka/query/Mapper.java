package se.grunka.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Mapper<T> {
    T map(ResultSet resultSet) throws SQLException;
}
