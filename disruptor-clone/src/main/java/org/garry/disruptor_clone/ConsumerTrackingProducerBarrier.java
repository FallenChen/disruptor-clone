package org.garry.disruptor_clone;

/**
 * {@link ProducerBarrier} that tracks multiple {@link Consumer}s when trying to claim
 * a {@link Entry} in the {@link RingBuffer}.
 */

public class ConsumerTrackingProducerBarrier<T extends Entry> implements ProducerBarrier {

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
    public Entry nextEntry() {
        return null;
    }

    @Override
    public void commit(Entry entry) {

    }

    @Override
    public long getCursor() {
        return 0;
    }
}
