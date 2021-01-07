package org.garry.disruptor_clone;

/**
 * Abstraction for claiming slots in a {@link RingBuffer} while tracking dependent {@link EventConusme}s
 * @param <T> {@link Entry} implementation stored in the {@link RingBuffer}
 */
public interface SlotClaimer<T extends Entry> {

    T claimNext();

    T claimSequence(long sequence);

    long getConsumedEventSequence();

    RingBuffer<? extends Entry> getRingBuffer();
}
