package org.garry.disruptor_clone;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.garry.disruptor_clone.Util.ceilingNextPowerOfTwo;

/**
 * Ring based store of reusable entries containing the data representing an {@link Entry} being exchanged between producers and consumers
 *
 * @param <T> Entry implementation storing the data for sharing during exchange or parallel coordination of an event
 */
public final class RingBuffer<T extends Entry> {

    /**
     * Set to -1 as sequence starting point
     */
    public static final long INITIAL_CURSOR_VALUE = -1;

    private final Object[] entries;
    private final int ringModMask;

    private final CommitCallback appendCallback = new AppendCommitCallback();

    private final SequenceClaimStrategy sequenceClaimStrategy;
    private final Lock lock = new ReentrantLock();
    private final Condition consumerNotifyCondition = lock.newCondition();

    private volatile long cursor = INITIAL_CURSOR_VALUE;

    public RingBuffer(final Factory<T> entryFactory, final int size,
                      final SequenceClaimThreadingStrategy sequenceClaimThreadingStrategy)
    {
        int sizeAsPowerOfTwo = ceilingNextPowerOfTwo(size);
        ringModMask = sizeAsPowerOfTwo - 1;
        entries = new Object[sizeAsPowerOfTwo];
        fill(entryFactory);
        sequenceClaimStrategy = sequenceClaimThreadingStrategy.newInstance();
    }

    public RingBuffer(final Factory<T> entryFactory, final int size)
    {
        this(entryFactory,size,SequenceClaimThreadingStrategy.MULTI_THREADED);
    }

    /**
     * Claim the next entry in sequence for use by a producer
     * @return the next entry in the sequence
     */
    public T claimNext()
    {
        long sequence = sequenceClaimStrategy.getAndIncrement();
        T next = (T)entries[(int) (sequence & ringModMask)];
        next.setSequence(sequence,appendCallback);
        return next;
    }

    private void fill(Factory<T> entryFactory) {
        for (int i = 0; i < entries.length; i++) {
            entries[i] = entryFactory.create();
        }
    }

    /**
     * Callback to be used when claiming slots in sequence and cursor is catching up with claim
     * for notifying the consumers of progress.This will busy spin on the commit until previous
     * producers have committed lower sequence Entries.
     */
    final class AppendCommitCallback implements CommitCallback
    {
        @Override
        public void commit(long sequence) {
            long slotMinusOne = sequence - 1;
            while (cursor != slotMinusOne)
            {
                // busy spin
            }
            cursor = sequence;
            notifyConsumer();
        }
    }

    private void notifyConsumer() {
        lock.lock();
        consumerNotifyCondition.signalAll();
        lock.unlock();
    }

    /**
     * Barrier handed out for gating consumers of the RingBuffer and dependent {@link EventConsumer}(s)
     */
    final class RingBufferThresholdBarrier implements ThresholdBarrier
    {
       private final EventConsumer[] eventConsumers;
       private final boolean hasGatingEventProcessors;

       private volatile boolean alerted = false;

       public RingBufferThresholdBarrier(EventConsumer... eventConsumers)
       {
           this.eventConsumers = eventConsumers;
           hasGatingEventProcessors = eventConsumers.length !=0;
       }


        @Override
        public long waitFor(long sequence) throws InterruptedException {
            return 0;
        }

        @Override
        public long waitFor(long sequence, long timeout, TimeUnit units) throws InterruptedException {
            return 0;
        }

        @Override
        public void checkForAlert() {
        }


        @Override
        public void alert() {

        }


        @Override
        public RingBuffer getRingBuffer() {
            return null;
        }

        @Override
        public long getProcessedEventSequence() {
            return 0;
        }
    }

}

