package org.garry.disruptor_clone;

import java.util.concurrent.TimeUnit;

/**
 * Coordination barrier for tracking the cursor for producers and sequence of
 * dependent {@link EventConsumer}s for a {@link RingBuffer}
 *
 * @param <T> {@link Entry} implementation stored in the {@link RingBuffer}
 */
public interface ThresholdBarrier<T extends Entry> {

    /**
     * Get the {@link RingBuffer} underlying this barrier
     * @return
     */
    RingBuffer<? extends  T> getRingBuffer();

    /**
     * Get the sequence number that the {@link RingBuffer} and dependent {@link EventConsumer}s have progressed to
     *
     * This is the RingBuffer cursor and minimum sequence number of the dependent EventProcessors
     * @return the sequence that is now valid for consuming
     */
    long getProcessedEventSequence();

    /**
     * Wait for the given sequence to be available for consumption
     * @param sequence to wait for
     * @return the sequence up to which is available
     */
    long waitFor(long sequence) throws InterruptedException, AlertException;

    /**
     * Wait for the given sequence to be available for consumption with a time out
     * @param sequence to wait for
     * @param timeout value
     * @param units for the timeout value
     * @return
     */
    long waitFor(long sequence, long timeout, TimeUnit units) throws InterruptedException;

    /**
     * Check for a status change in the Disruptor being alerted to be used by the consumers
     */
    void checkForAlert() throws AlertException;


    /**
     * Alert the consumers of a status change and stay in this status until cleared
     */
    void alert();


}
