package org.garry.disruptor_clone;

/**
 * Strategies employed for claiming the sequence of {@link Entry}s in the {@link RingBuffer} by producers.
 * The {@link Entry} index is a sequence value mod the {@link RingBuffer} capacity
 */
public interface ClaimStrategy {

    /**
     * Claim the next sequence index in the {@link RingBuffer} and increment
     *
     * @return the {@link Entry} index to be used for the producer
     */
    long getAndIncrement();

    /**
     * Set the current sequence value for claiming {@link Entry} in the {@link RingBuffer}
     * @param sequence to be set as the current value.
     */
    void setSequence(long sequence);

    /**
     * Wait for the current commit to reach a given sequence
     * @param sequence to wait for
     * @param ringBuffer on which to wait forCursor
     */
    void waitForCursor(long sequence, RingBuffer ringBuffer);
}
