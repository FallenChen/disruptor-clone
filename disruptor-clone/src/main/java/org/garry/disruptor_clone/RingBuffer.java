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
    private final CommitCallback setCallback = new SetCommitCallback();

    /**
     * Pre-allocated exception to avoid garbage generation
     */
    public static final AlertException ALERT_EXCEPTION = new AlertException();

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

    public T claimSequence(long sequence)
    {
        T entry = (T) entries[(int) (sequence & ringModMask)];
        entry.setSequence(sequence,setCallback);
        return entry;
    }

    /**
     * Create a barrier that gates on the RingBuffer and a list of {@link EventConsumer}s
     * @param eventConsumers this barrier will track
     * @return the barrier gated as required
     */
    public ThresholdBarrier<T> createBarrier(EventConsumer... eventConsumers)
    {
        return new RingBufferThresholdBarrier(eventConsumers);
    }

    /**
     * Get the entry for a given sequence from the RingBuffer
     * @param sequence for the entry
     * @return entry matching the sequence
     */
    public T get(long sequence)
    {
        return (T) entries[(int) (sequence&ringModMask)];
    }

    /**
     * The capacity of the RingBuffer to hold entries
     * @return the size of the RingBuffer
     */
    public int getCapacity()
    {
        return entries.length;
    }

    /**
     * Get the current sequence that producers have committed to the RingBuffer
     * @return the current committed sequence
     */
    public long getCursor() {
        return cursor;
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
        public long waitFor(long sequence) throws InterruptedException, AlertException {
           if (hasGatingEventProcessors)
           {
               long completedProcessedEventSequence = getProcessedEventSequence();
               if (completedProcessedEventSequence >= sequence)
               {
                   return completedProcessedEventSequence;
               }
               waitForRingBuffer(sequence);

               while ((completedProcessedEventSequence = getProcessedEventSequence()) < sequence)
               {
                   checkForAlert();
               }
               return completedProcessedEventSequence;
           }
            return 0;
        }

        private long waitForRingBuffer(long sequence) throws InterruptedException, AlertException {
            if (cursor < sequence)
            {
                lock.lock();
                try {
                    while (cursor < sequence)
                    {
                        checkForAlert();
                        consumerNotifyCondition.await();
                    }
                }
                finally {
                    lock.unlock();
                }
            }
            return cursor;
        }

        @Override
        public long waitFor(long sequence, long timeout, TimeUnit units) throws InterruptedException {
            return 0;
        }

        @Override
        public void checkForAlert() throws AlertException {
           if (alerted)
           {
               alerted = true;
               throw ALERT_EXCEPTION;
           }
        }


        @Override
        public void alert() {

        }


        @Override
        public RingBuffer getRingBuffer() {
           return RingBuffer.this;
        }

        @Override
        public long getProcessedEventSequence() {
           long minimum = cursor;
           for(EventConsumer eventConsumer: eventConsumers)
           {
               long sequence = eventConsumer.getSequence();
               minimum = minimum < sequence ? minimum : sequence;
           }
           return minimum;
        }
    }

    /**
     * Callback to be used when claiming slots and the cursor is explicitly set by the producer when
     * you are sure only one producer exists
     */
    final class SetCommitCallback implements CommitCallback
    {
        @Override
        public void commit(long sequence) {
            sequenceClaimStrategy.setSequence(sequence + 1);
            cursor = sequence;
            notifyConsumer();
        }
    }
}

