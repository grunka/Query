package se.grunka.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Parameter {
    void set(PreparedStatement statement) throws SQLException;
}
