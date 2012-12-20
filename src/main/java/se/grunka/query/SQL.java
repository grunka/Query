package se.grunka.query;

import javax.sql.DataSource;

public class SQL {
    public static <T> Iterable<T> query(DataSource dataSource, String sql, Parameter parameter, Mapper<T> mapper) {
        return new QueryIterable<>(dataSource, sql, parameter, mapper);
    }

}

