package org.garry.disruptor_clone;

/**
 * Strategy employed for making {@link Consumer}s wait on a {@link RingBuffer}
 */
public interface WaitStrategy {

    /**
     * Wait for the given sequence to be available for consumption in a {@link RingBuffer}
     * @param consumers
     * @param ringBuffer
     * @param barrier
     * @param sequence
     * @return
     */
    long waitFor(Consumer[] consumers, RingBuffer ringBuffer,ConsumerBarrier barrier, long sequence);
}
