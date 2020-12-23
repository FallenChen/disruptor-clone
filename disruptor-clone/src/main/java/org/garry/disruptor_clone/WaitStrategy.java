package org.garry.disruptor_clone;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.garry.disruptor_clone.Util.getMinimumSequence;

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
    long waitFor(Consumer[] consumers, RingBuffer ringBuffer,ConsumerBarrier barrier, long sequence) throws InterruptedException;


    long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence, long timeout, TimeUnit units) throws InterruptedException;


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
        public long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence) throws InterruptedException {
            long availableSequence;
            if ((availableSequence = ringBuffer.getCursor()) < sequence)
            {
                lock.lock();
                try {

                    while ((availableSequence = ringBuffer.getCursor()) < sequence)
                    {
                        consumerNotifyCondition.await();
                    }
                }
                finally {
                    lock.unlock();
                }
            }

            if (0 != consumers.length)
            {
                while ((availableSequence = getMinimumSequence(consumers)) < sequence)
                {

                }
            }

            return availableSequence;
        }

        @Override
        public long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence, long timeout, TimeUnit units) throws InterruptedException {
            long availableSequence;
            if ((availableSequence = ringBuffer.getCursor()) < sequence)
            {
                lock.lock();
                try {

                    while ((availableSequence = ringBuffer.getCursor()) < sequence)
                    {
                        if(!consumerNotifyCondition.await(timeout, units))
                        {
                            break;
                        }
                    }
                }
                finally {
                    lock.unlock();
                }
            }

            if (0 != consumers.length)
            {
                while ((availableSequence = getMinimumSequence(consumers)) < sequence)
                {

                }
            }

            return availableSequence;
        }

        @Override
        public void signalAll() {
            lock.lock();
            try {
                consumerNotifyCondition.signalAll();
            }
            finally {
                lock.unlock();
            }
        }
    }


}