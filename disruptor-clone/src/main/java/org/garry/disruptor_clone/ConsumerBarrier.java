package org.garry.disruptor_clone;

import java.util.concurrent.TimeUnit;

/**
 * Coordination barrier for tracking the cursor for producers and sequence of
 * dependent {@link Consumer}s for a {@link RingBuffer}
 *
 * @param <T> {@link Entry} implementation stored in the {@link RingBuffer}
 */
public interface ConsumerBarrier<T extends Entry> {

    T getEntry(long sequence);

    long waitFor(long sequence);

    long waitFor(long sequence, long timeout, TimeUnit units);

    long getCursor();

}
