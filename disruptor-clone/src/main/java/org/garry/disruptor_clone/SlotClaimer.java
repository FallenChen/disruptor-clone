package org.garry.disruptor_clone;

/**
 * Abstraction for claiming slots in a {@link RingBuffer} while tracking dependent {@link EventConsumer}s
 * @param <T> {@link Entry} implementation stored in the {@link RingBuffer}
 */
public interface SlotClaimer<T extends Entry> {

    /**
     * Claim the next slot in sequence for a producer on the {@link RingBuffer}
     * @return
     */
    T claimNext();

    /**
     * Claim a specific sequence in the {@link RingBuffer} when only one producer is involved
     * @param sequence to be claimed
     * @return the claimed {@link Entry}
     */
    T claimSequence(long sequence);

    /**
     * Get the sequence that {@link EventConsumer}s have consumed from the {@link RingBuffer}
     * @return
     */
    long getConsumedEventSequence();

    /**
     * Get the underlying {@link RingBuffer} in which slots are being claimed
     * @return the {@link RingBuffer}
     */
    RingBuffer<? extends Entry> getRingBuffer();
}
