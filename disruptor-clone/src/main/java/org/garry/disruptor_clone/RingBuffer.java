package org.garry.disruptor_clone;

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

    private volatile long cursor = INITIAL_CURSOR_VALUE;


    private final Object[] entries;
    private final int ringModMask;

    private final ClaimStrategy claimStrategy;
    private final WaitStrategy waitStrategy;

    public RingBuffer(final EntryFactory<T> entryFactory,final int size,
                      final ClaimStrategy.Option claimStrategyOption,
                      final WaitStrategy.Option waitStrategyOption) {
        int sizeAsPowerOfTwo = ceilingNextPowerOfTwo(size);
        ringModMask = sizeAsPowerOfTwo - 1;
        entries = new Object[sizeAsPowerOfTwo];
        claimStrategy = claimStrategyOption.newInstance();
        waitStrategy = waitStrategyOption.newInstance();
        fill(entryFactory);
    }

    public RingBuffer(final EntryFactory<T> entryFactory, final int size) {
        this(entryFactory,size,
                ClaimStrategy.Option.SINGLE_THREADED,
                WaitStrategy.Option.BLOCKING);
    }

    private void fill(EntryFactory<T> entryFactory) {
        for (int i = 0; i < entries.length; i++) {
            entries[i] = entryFactory.create();
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
     * {@link ProducerBarrier} that tracks multiple {@link Consumer}s when trying to claim
     * a {@link Entry} in the {@link RingBuffer}.
     */

    final class ConsumerTrackingProducerBarrier implements ProducerBarrier<T> {

        private final Consumer[] consumers;

        public ConsumerTrackingProducerBarrier(final Consumer... consumers)
        {
            if (0 == consumers.length)
            {
                throw new IllegalArgumentException("There must be at least one Consumer to track for preventing ring wrap");
            }

            this.consumers = consumers;
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
        public void commit(T entry) {
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
            while ((sequence - getMinimumSequence(consumers)) >= entries.length)
            {
                Thread.yield();
            }
        }
    }


}
