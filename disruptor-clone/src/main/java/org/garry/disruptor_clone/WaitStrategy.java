package org.garry.disruptor_clone;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Strategy employed for making {@link Consumer}s wait on a {@link RingBuffer}
 */
public interface WaitStrategy {

    /**
     * Wait for the given sequence to be available for consumption in a {@link RingBuffer}
     * @param consumers future
     * @param ringBuffer
     * @param barrier
     * @param sequence
     * @return
     */
    long waitFor(Consumer[] consumers, RingBuffer ringBuffer,ConsumerBarrier barrier, long sequence);


    long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence, long timeout, TimeUnit units);


    void signalAll();


    /**
     * Blocking strategy that uses a lock and condition variable for {@link Consumer}s waiting on a barrier
     *
     * This strategy should be used when performances and low-latency are not as important as CPU resource
     */
    static final class BlockingStrategy implements WaitStrategy
    {
        private final Lock lock = new ReentrantLock();
        private final Condition consumerNotifyCondition = lock.newCondition();

        @Override
        public long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence) {
            return 0;
        }

        @Override
        public long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence, long timeout, TimeUnit units) {
            return 0;
        }

        @Override
        public void signalAll() {

        }
    }


}
