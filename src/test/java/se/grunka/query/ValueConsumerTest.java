package se.grunka.query;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValueConsumerTest {

    private ValueConsumer<String> target;
    private BlockingQueue<String> queue;
    private String endOfResultSetMarker;

    @SuppressWarnings("unchecked")
    @Before
    public void before() throws Exception {
        queue = mock(BlockingQueue.class);
        endOfResultSetMarker = "THE END";
        target = new ValueConsumer<>(queue, endOfResultSetMarker);
    }

    @Test
    public void shouldSeeNextValue() throws Exception {
        when(queue.take())
                .thenReturn("a")
                .thenReturn("b")
                .thenReturn(endOfResultSetMarker);
        assertTrue(target.hasNext());
        assertEquals("a", target.next());
        assertTrue(target.hasNext());
        assertEquals("b", target.next());
        assertFalse(target.hasNext());
    }

    @Test
    public void shouldGetValueWithoutHasNext() throws Exception {
        when(queue.take())
                .thenReturn("a")
                .thenReturn("b")
                .thenReturn(endOfResultSetMarker);
        assertEquals("a", target.next());
        assertEquals("b", target.next());
        assertFalse(target.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowExceptionWhenCallingNextOnEmptyQueue() throws Exception {
        when(queue.take()).thenReturn(endOfResultSetMarker);
        target.next();
    }

    @Test
    public void shouldHandleHasNextOnEmptyQueue() throws Exception {
        when(queue.take()).thenReturn(endOfResultSetMarker);
        assertFalse(target.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotSupportRemove() throws Exception {
        when(queue.take())
                .thenReturn("a")
                .thenReturn("b")
                .thenReturn(endOfResultSetMarker);
        target.remove();
    }

    @Test(expected = Error.class)
    public void shouldThrowErrorIfInterruptedSinceThereIsNoWayToRecover() throws Exception {
        when(queue.take()).thenThrow(new InterruptedException());
        target.hasNext();
    }
}
