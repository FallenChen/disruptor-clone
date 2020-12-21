package org.garry.disruptor_clone;

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
    public static final long INITIAL_CURSOR_VALUE = -1L;

    private volatile long cursor = INITIAL_CURSOR_VALUE;


    private final Object[] entries;

    private final ClaimStrategy claimStrategy;

    public RingBuffer(final EntryFactory<T> entryFactory,final int size,
                      ClaimStrategy.Option claimStrategyOption) {
        int sizeAsPowerOfTwp = ceilingNextPowerOfTwo(size);
        entries = new Object[sizeAsPowerOfTwp];
        claimStrategy = claimStrategyOption.newInstance();
        fill(entryFactory);
    }

    public RingBuffer(final EntryFactory<T> entryFactory, final int size) {
        this(entryFactory,size,
                ClaimStrategy.Option.SINGLE_THREADED);
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
            return null;
        }

        @Override
        public void commit(T entry) {
            long sequence = entry.getSequence();
            claimStrategy.waitForCursor(sequence -1L, RingBuffer.this);
            cursor = sequence;
            // signal all

        }

        @Override
        public long getCursor() {
            return cursor;
        }
    }


}
