package se.grunka.query;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.concurrent.SynchronousQueue;

class QueryIterable<T> implements Iterable<T> {
    private final DataSource dataSource;
    private final String sql;
    private final Parameter parameter;
    private final Mapper<T> mapper;

    public QueryIterable(DataSource dataSource, String sql, Parameter parameter, Mapper<T> mapper) {
        this.dataSource = dataSource;
        this.sql = sql;
        this.parameter = parameter;
        this.mapper = mapper;
    }

    @Override
    public Iterator<T> iterator() {
        SynchronousQueue<T> valueQueue = new SynchronousQueue<>();
        Object endOfResultSetMarker = new Object();
        new Thread(new ValueProducer<>(dataSource, sql, parameter, mapper, valueQueue, endOfResultSetMarker)).start();
        return new ValueConsumer<>(valueQueue, endOfResultSetMarker);
    }
}
