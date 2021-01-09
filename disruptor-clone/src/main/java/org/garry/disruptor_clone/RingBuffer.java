package org.garry.disruptor_clone;

import java.util.concurrent.TimeUnit;

import static org.garry.disruptor_clone.Util.ceilingNextPowerOfTwo;
import static org.garry.disruptor_clone.Util.getMinimumSequence;

/**
 * Ring based store of reusable entries containing the data representing an {@link Entry} being exchanged between producers and consumers
 *
 * @param <T> Entry implementation storing the data for sharing during exchange or parallel coordination of an event
 */
public final class RingBuffer<T extends Entry> {

    /**
     * Set to -1 as sequence starting point
     */
    public static final long INITIAL_CURSOR_VALUE = -1L;

    private long p1,p2,p3,p4,p5,p6,p7; //cache line padding why?
    private volatile long cursor = INITIAL_CURSOR_VALUE;
    private long p8,p9,p10,p11,p12,p13,p14; //cache line padding why?


    private final Object[] entries;
    private final int ringModMask;

    private final ClaimStrategy claimStrategy;
    private final WaitStrategy waitStrategy;

    /**
     * Construct a RingBuffer with the full option set
     * @param factory to create {@link Entry}s for filling the RingBuffer
     * @param size of the RingBuffer that will be rounded up to the next power of 2
     * @param claimStrategyOption threading strategy for producers claiming {@link Entry}s in the ring
     * @param waitStrategyOption waiting strategy employed by consumers waiting on {@link Entry}s becoming available
     */
    public RingBuffer(final Factory<T> factory, final int size,
                      final ClaimStrategy.Option claimStrategyOption,
                      final WaitStrategy.Option waitStrategyOption) {
        int sizeAsPowerOfTwo = ceilingNextPowerOfTwo(size);
        ringModMask = sizeAsPowerOfTwo - 1;
        entries = new Object[sizeAsPowerOfTwo];

        claimStrategy = claimStrategyOption.newInstance();
        waitStrategy = waitStrategyOption.newInstance();

        fill(factory);
    }

    public RingBuffer(final Factory<T> factory, final int size) {
        this(factory,size,
                ClaimStrategy.Option.SINGLE_THREADED,
                WaitStrategy.Option.BLOCKING);
    }

    private void fill(Factory<T> factory) {
        for (int i = 0; i < entries.length; i++) {
            entries[i] = factory.create();
        }
    }

    /**
     * The capacity of the RingBuffer to hold entries
     * @return the size of RingBuffer
     */
    public int getCapacity(){
        return entries.length;
    }

    /**
     * Get the current sequence that producers have committed to the RingBuffer
     * @return the current committed sequence
     */
    public long getCursor(){
        return cursor;
    }

    /**
     * ConsumerBarrier handed out for gating consumers of the RingBuffer and dependent {@link EventConsumer}s
     * @param <T>
     */
    final class ConsumerTrackingThresholdBarrier<T extends Entry> implements ThresholdBarrier<T>
    {
        private final EventConsumer[] eventConsumers;
        private volatile boolean alerted = false;

        public ConsumerTrackingThresholdBarrier(final EventConsumer... eventConsumers) {
            this.eventConsumers = eventConsumers;
        }

        @Override
        public T getEntry(long sequence) {
            return (T) entries[(int) (sequence & ringModMask)];
        }

        @Override
        public long waitFor(long sequence) throws InterruptedException {
            return waitStrategy.waitFor(eventConsumers, RingBuffer.this, this, sequence);
        }

        @Override
        public long waitFor(long sequence, long timeout, TimeUnit units) throws InterruptedException {
            return waitStrategy.waitFor(eventConsumers,RingBuffer.this,this,sequence,timeout,units);
        }

        @Override
        public long getCursor() {
            return cursor;
        }

        @Override
        public boolean isAlerted() {
            return alerted;
        }

        @Override
        public void alert() {
            alerted = true;
            waitStrategy.signalAll();
        }

        @Override
        public void cleanAlert() {
            alerted = false;
        }
    }



    /**
     * {@link ProducerBarrier} that tracks multiple {@link EventConsumer}s when trying to claim
     * a {@link Entry} in the {@link RingBuffer}.
     */

    final class ConsumerTrackingProducerBarrier implements ProducerBarrier<T> {

        private final EventConsumer[] eventConsumers;

        public ConsumerTrackingProducerBarrier(final EventConsumer... eventConsumers)
        {
            if (0 == eventConsumers.length)
            {
                throw new IllegalArgumentException("There must be at least one Consumer to track for preventing ring wrap");
            }

            this.eventConsumers = eventConsumers;
        }

        @Override
        public T nextEntry() {
            long sequence = claimStrategy.getAndIncrement();
            ensureConsumersAreInRange(sequence);

            T entry = (T) entries[(int) (sequence & ringModMask)];
            entry.setSequence(sequence);
            return entry;
        }

        @Override
        public void commit(final T entry) {
            long sequence = entry.getSequence();
            claimStrategy.waitForCursor(sequence -1L, RingBuffer.this);
            cursor = sequence;
            // signal all
            waitStrategy.signalAll();

        }

        @Override
        public long getCursor() {
            return cursor;
        }

        private void ensureConsumersAreInRange(final long sequence)
        {
            while ((sequence - getMinimumSequence(eventConsumers)) >= entries.length)
            {
                Thread.yield();
            }
        }
    }


}
