package org.garry.disruptor_clone;

/**
 * EntryConsumers waitFor {@link Entry}s to become available for consumption from the {@link RingBuffer}
 *
 */
public interface EventConsumer extends Runnable{

    /**
     * Get the sequence up to which this Consumer has consumed {@link Entry}s
     * @return the sequence of the last consumed {@link Entry}
     */
    long getSequence();

    /**
     * Signal that this Consumer should stop when it has finished consuming at the next clean break.
     * It will call {@link ThresholdBarrier#alert()} to notify the thread to check status.
     */
    void halt();

    ThresholdBarrier getBarrier();
}
