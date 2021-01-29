package org.garry.disruptor_clone;

public final class YieldingSlotClaimer <T extends Entry> extends AbstractSlotClaimer<T>{

    public YieldingSlotClaimer(final int bufferReverseThreshold,
                               final RingBuffer<? extends T> ringBuffer,
                               final EventConsumer... gatingEventConsumers) {
        super(bufferReverseThreshold, ringBuffer, gatingEventConsumers);
    }

    @Override
    public T claimNext() {
        final RingBuffer<? extends T> ringBuffer = getRingBuffer();

        final long threshold = ringBuffer.getCapacity() - getBufferReverseThreshold();
        while (ringBuffer.getCursor() - getConsumedEventSequence() >= threshold)
        {
            Thread.yield();
        }
        return ringBuffer.claimNext();
    }

    @Override
    public T claimSequence(long sequence) {
        final RingBuffer<? extends T> ringBuffer = getRingBuffer();

        final long threshold = ringBuffer.getCapacity() - getBufferReverseThreshold();
        while (sequence - getConsumedEventSequence() >= threshold)
        {
            Thread.yield();
        }
        return ringBuffer.claimSequence(sequence);
    }
}
