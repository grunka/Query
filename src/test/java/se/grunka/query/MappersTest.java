package se.grunka.query;

import java.sql.ResultSet;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MappersTest {
    private ResultSet resultSet;

    @Before
    public void before() throws Exception {
        resultSet = mock(ResultSet.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForMissingConstructor() throws Exception {
        Mappers.object(MissingConstructor.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForMissingColumnAnnotations() throws Exception {
        Mappers.object(MissingAnnotations.class);
    }

    @Test
    public void shouldMapValuesToFields() throws Exception {
        //TODO moar!
        when(resultSet.getString("text")).thenReturn("hello");
        when(resultSet.getLong("basic_long")).thenReturn(1L);
        when(resultSet.getLong("object_long")).thenReturn(2L);

        Target result = Mappers.object(Target.class).map(resultSet);

        assertEquals("hello", result.text);
        assertEquals(1, result.basicLong);
        assertEquals(Long.valueOf(2), result.objectLong);
    }

    public static class Target {
        @Column("text")
        public final String text = null;
        @Column("basic_long")
        public long basicLong = -1;
        @Column("object_long")
        public final Long objectLong = null;
    }

    public static class MissingConstructor {
        @SuppressWarnings("UnusedParameters")
        public MissingConstructor(String someValue) {
        }
    }

    public static class MissingAnnotations {
    }
}
