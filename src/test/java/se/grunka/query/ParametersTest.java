package se.grunka.query;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ParametersTest {
    private PreparedStatement statement;

    @Before
    public void before() throws Exception {
        statement = mock(PreparedStatement.class);
    }

    @Test
    public void shouldNotDoAnyThing() throws Exception {
        Parameters.none().set(statement);

        verifyZeroInteractions(statement);
    }

    @Test
    public void shouldSetAllKindsOfParametersCorrectly() throws Exception {
        Parameters.values(
                "Hello",
                1,
                2f,
                3d,
                (short) 4,
                5l,
                (byte) 6,
                true,
                new URL("http://example.com"),
                new byte[]{1, 2, 3},
                new java.sql.Date(42),
                new Date(123),
                new UnknownObject(567),
                BigDecimal.valueOf(99)).set(statement);

        int i = 1;
        verify(statement).setString(i++, "Hello");
        verify(statement).setInt(i++, 1);
        verify(statement).setFloat(i++, 2);
        verify(statement).setDouble(i++, 3);
        verify(statement).setShort(i++, (short) 4);
        verify(statement).setLong(i++, 5l);
        verify(statement).setByte(i++, (byte) 6);
        verify(statement).setBoolean(i++, true);
        verify(statement).setURL(i++, new URL("http://example.com"));
        verify(statement).setBytes(i++, new byte[]{1, 2, 3});
        verify(statement).setDate(i++, new java.sql.Date(42));
        verify(statement).setTimestamp(i++, new Timestamp(123));
        verify(statement).setObject(i++, new UnknownObject(567));
        verify(statement).setBigDecimal(i, BigDecimal.valueOf(99));

        verifyNoMoreInteractions(statement);
    }

    public static class UnknownObject {
        private final int value;

        public UnknownObject(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return value == ((UnknownObject) o).value;

        }

        @Override
        public int hashCode() {
            return value;
        }
    }
}
