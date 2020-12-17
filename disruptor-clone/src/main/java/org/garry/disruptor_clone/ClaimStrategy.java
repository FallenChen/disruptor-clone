package org.garry.disruptor_clone;

/**
 * Strategies employed for claiming the sequence of {@link Entry}s in the {@link RingBuffer} by producers.
 * The {@link Entry} index is a sequence value mod the {@link RingBuffer} capacity
 */
public interface ClaimStrategy {

    /**
     * Claim the next sequence index in the {@link RingBuffer} and increment
     *
     * @return the {@link Entry} index to be used for the producer
     */
    long getAndIncrement();

    /**
     * Set the current sequence value for claiming {@link Entry} in the {@link RingBuffer}
     * @param sequence to be set as the current value.
     */
    void setSequence(long sequence);

    /**
     * Wait for the current commit to reach a given sequence
     * @param sequence to wait for
     * @param ringBuffer on which to wait forCursor
     */
    void waitForCursor(long sequence, RingBuffer ringBuffer);

    /**
     * Indicates the threading policy to be applied for claiming {@link Entry}s by producers to the {@link RingBuffer}
     */
    enum Option
    {
        SINGLE_THREADED
                {
                    @Override
                    ClaimStrategy newInstance() {
                       return  new SingleThreadedStrategy();
                    }
                };

        /**
         * Used by the {@link RingBuffer} as a polymorphic constructor.
         * @return a new instance of the ClaimStrategy
         */
        abstract ClaimStrategy newInstance();
    }

    /**
     * Optimised strategy can be used when there is a single producer thread claiming {@link Entry}
     */
    static final class SingleThreadedStrategy implements ClaimStrategy
    {
        private long sequence;

        @Override
        public long getAndIncrement() {
            return sequence++;
        }

        @Override
        public void setSequence(long sequence) {
            this.sequence = sequence;
        }

        @Override
        public void waitForCursor(long sequence, RingBuffer ringBuffer) {
            // no op for this class
        }
    }
}
