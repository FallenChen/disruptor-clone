package org.garry.disruptor_clone;

import java.util.concurrent.TimeUnit;

/**
 * Coordination barrier for tracking the cursor for producers and sequence of
 * dependent {@link Consumer}s for a {@link RingBuffer}
 *
 * @param <T> {@link Entry} implementation stored in the {@link RingBuffer}
 */
public interface ConsumerBarrier<T extends Entry> {

    /**
     * Get the {@link Entry} for a given sequence from the underlying {@link RingBuffer}
     * @param sequence of the {@link Entry} to get
     * @return the {@link Entry} for the sequence
     */
    T getEntry(long sequence);

    /**
     * Wait for the given sequence to be available for consumption
     * @param sequence to wait for
     * @return the sequence up to which is available
     */
    long waitFor(long sequence);

    /**
     * Wait for the given sequence to be available for consumption with a time out
     * @param sequence to wait for
     * @param timeout value
     * @param units for the timeout value
     * @return
     */
    long waitFor(long sequence, long timeout, TimeUnit units);

    /**
     * Delegate a call to the {@link RingBuffer#getCursor()} ???todo
     * @return value of the cursor for entries that have been published
     */
    long getCursor();



}
