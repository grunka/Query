package se.grunka.query;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ValueProducerTest {

    private ValueProducer<String> target;
    private Parameter parameter;
    private Mapper<String> mapper;
    private BlockingQueue<String> valueQueue;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private String endOfResultSetMarker;

    @SuppressWarnings("unchecked")
    @Before
    public void before() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement("SELECT something FROM SomeTable")).thenReturn(statement);
        when(dataSource.getConnection()).thenReturn(connection);
        parameter = mock(Parameter.class);
        mapper = mock(Mapper.class);
        valueQueue = mock(BlockingQueue.class);
        endOfResultSetMarker = "THE END";
        target = new ValueProducer<>(dataSource, "SELECT something FROM SomeTable", parameter, mapper, valueQueue, endOfResultSetMarker);
    }

    @After
    public void after() throws Exception {
        verify(parameter).set(statement);
    }

    @Test
    public void shouldPassMappedResultSetValuesToQueue() throws Exception {
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(mapper.map(resultSet))
                .thenReturn("a")
                .thenReturn("b");

        target.run();

        verify(valueQueue).put("a");
        verify(valueQueue).put("b");
        verify(valueQueue).put(endOfResultSetMarker);
        verifyNoMoreInteractions(valueQueue);
    }

    @Test(expected = Error.class)
    public void shouldThrowErrorIfInterruptedSinceThereIsntAnythingElseToDo() throws Exception {
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(false);
        when(mapper.map(resultSet))
                .thenReturn("a");
        doThrow(new InterruptedException()).when(valueQueue).put("a");

        target.run();
    }

    @Test
    public void shouldAddExceptionAndMarkerToQueueOnFailure() throws Exception {
        when(resultSet.next()).thenThrow(new SQLException("oh hai"));
        doThrow(new InterruptedException()).when(valueQueue).put("a");

        target.run();

        verify(valueQueue).put(Matchers.argThat(new BaseMatcher<String>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof ExceptionWrapper) {
                    if (((ExceptionWrapper) o).exception instanceof SQLException) {
                        return "oh hai".equals(((ExceptionWrapper) o).exception.getMessage());
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
            }
        }));
        verify(valueQueue).put(endOfResultSetMarker);
        verifyNoMoreInteractions(valueQueue);
    }
}
