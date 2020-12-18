package org.garry.disruptor_clone;

/**
 * Strategy employed for making {@link Consumer}s wait on a {@link RingBuffer}
 */
public interface WaitStrategy {

    long waitFor(Consumer[] consumers, RingBuffer ringBuffer);
}
