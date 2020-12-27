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
     * @param consumers further back the chain that must advance fast
     * @param ringBuffer on which to wait
     * @param barrier the consumer is waiting on
     * @param sequence to be waited on
     * @return the sequence that is available which may be greater than the requested sequence
     */
    long waitFor(Consumer[] consumers, RingBuffer ringBuffer,ConsumerBarrier barrier, long sequence) throws InterruptedException;


    long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence, long timeout, TimeUnit units) throws InterruptedException;


    /**
     * Signal those waiting that the {@link RingBuffer} cursor has advanced
     */
    void signalAll();

    /**
     * Strategy options which are available to those waiting on a {@link RingBuffer}
     */
    enum Option
    {
        /**
         * This strategy uses a condition variable inside a lock to block the consumer which saves CPU resource as
         * the expense of lock contention
         */
        BLOCKING
                {
                    @Override
                    WaitStrategy newInstance() {
                        return new BlockingStrategy();
                    }
                },

        /**
         * This strategy calls Thread.yield() in a loop as a waiting strategy which reduces contention
         * at the expense of CPU resource
         */
        YIELDING
                {
                    @Override
                    WaitStrategy newInstance() {
                        return new YieldingStrategy();
                    }
                },

        /**
         * This strategy call spins in a loop as a waiting strategy which is lowest and most consistent latency
         * but ties up a CPU
         */
        BUSY_SPIN
                {
                    @Override
                    WaitStrategy newInstance() {
                        return new BusySpinStrategy();
                    }
                };
        abstract WaitStrategy newInstance();
    }


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


    /**
     * Busy Spin strategy that uses a busy spin loop for {@link Consumer}s waiting on a barrier.
     *
     * This strategy will use CPU resource to avoid syscall which can introduce latency jitter. It is best
     * used when threads can be bounded to specific CPU cores.
     */
    static final class BusySpinStrategy implements WaitStrategy
    {
        @Override
        public long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence) throws InterruptedException {
            long availableSequence;
            if (0 == consumers.length)
            {
                while ((availableSequence = ringBuffer.getCursor()) < sequence)
                {

                }
            }
            else
            {
                while ((availableSequence = getMinimumSequence(consumers)) < sequence)
                {

                }
            }
            return availableSequence;
        }

        @Override
        public long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence, long timeout, TimeUnit units) throws InterruptedException {
            final long timeoutMS = units.convert(timeout, TimeUnit.MILLISECONDS);
            final long currentTime = System.currentTimeMillis();
            long availableSequence;

            if (0 == consumers.length)
            {
                while ((availableSequence = ringBuffer.getCursor()) < sequence)
                {
                    if (timeoutMS < (System.currentTimeMillis() - currentTime))
                    {
                        break;
                    }
                }
            }
            else
            {
                while ((availableSequence = getMinimumSequence(consumers)) < sequence)
                {
                    if (timeoutMS < (System.currentTimeMillis()) - currentTime)
                    {
                        break;
                    }
                }
            }

            return availableSequence;
        }

        @Override
        public void signalAll() {

        }
    }

    /**
     * Yielding strategy that uses a Thread.yield() for {@link Consumer}s waiting on a barrier
     * This strategy is a good compromise between performance and CPU resources.
     */
    static final class YieldingStrategy implements WaitStrategy
    {

        @Override
        public long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence) throws InterruptedException {
            long availableSequence;

            if (0 == consumers.length)
            {
                while ((availableSequence = ringBuffer.getCursor())< sequence)
                {
                    Thread.yield();
                }
            }else
            {
                while ((availableSequence = getMinimumSequence(consumers)) < sequence)
                {
                    Thread.yield();
                }
            }
            return availableSequence;
        }

        @Override
        public long waitFor(Consumer[] consumers, RingBuffer ringBuffer, ConsumerBarrier barrier, long sequence, long timeout, TimeUnit units) throws InterruptedException {
            final long timeoutMs = units.convert(timeout,TimeUnit.MILLISECONDS);
            final long currentTime = System.currentTimeMillis();
            long availableSequence;

            if (0 == consumers.length)
            {
                while ((availableSequence = ringBuffer.getCursor()) < sequence)
                {
                    Thread.yield();
                    if (timeoutMs < (System.currentTimeMillis() - currentTime))
                    {
                        break;
                    }
                }
            }
            else
            {
                while ((availableSequence = getMinimumSequence(consumers)) < sequence)
                {
                    Thread.yield();
                    if (timeoutMs < (System.currentTimeMillis() - currentTime))
                    {
                        break;
                    }
                }
            }
            return availableSequence;
        }

        @Override
        public void signalAll() {

        }
    }



}
