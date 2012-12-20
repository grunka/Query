package se.grunka.query;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

class ValueProducer<T> implements Runnable {
    private final DataSource dataSource;
    private final String sql;
    private final Parameter parameter;
    private final Mapper<T> mapper;
    private final BlockingQueue valueQueue;
    private final Object endOfResultSetMarker;

    public ValueProducer(DataSource dataSource, String sql, Parameter parameter, Mapper<T> mapper, BlockingQueue<T> valueQueue, Object endOfResultSetMarker) {
        this.dataSource = dataSource;
        this.sql = sql;
        this.parameter = parameter;
        this.mapper = mapper;
        this.valueQueue = valueQueue;
        this.endOfResultSetMarker = endOfResultSetMarker;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    parameter.set(statement);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            T value = mapper.map(resultSet);
                            valueQueue.put(value);
                        }
                    }
                }
            } catch (SQLException e) {
                valueQueue.put(new ExceptionWrapper(e));
            } finally {
                valueQueue.put(endOfResultSetMarker);
            }
        } catch (InterruptedException e) {
            throw new Error("Interrupted while adding to queue", e);
        }
    }
}
