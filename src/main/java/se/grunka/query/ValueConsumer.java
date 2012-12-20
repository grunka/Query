package se.grunka.query;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;

class ValueConsumer<T> implements Iterator<T> {

    private final BlockingQueue<T> valueQueue;
    private final Object endOfResultSetMarker;
    private boolean ended = false;
    private boolean hasValue = false;
    private T value = null;

    public ValueConsumer(BlockingQueue<T> valueQueue, Object endOfResultSetMarker) {
        this.valueQueue = valueQueue;
        this.endOfResultSetMarker = endOfResultSetMarker;
    }

    private void ensureValue() {
        if (!hasValue && !ended) {
            try {
                value = valueQueue.take();
            } catch (InterruptedException e) {
                throw new Error("Interrupted while taking from queue", e);
            }
            if (value == endOfResultSetMarker) {
                ended = true;
                value = null;
            } else {
                hasValue = true;
            }
        }
    }

    @Override
    public boolean hasNext() {
        ensureValue();
        return !ended && hasValue;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        hasValue = false;
        return value;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
