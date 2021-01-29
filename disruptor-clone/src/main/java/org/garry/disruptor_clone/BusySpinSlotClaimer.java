package org.garry.disruptor_clone;

/**
 * SlotClaimer that uses a busy spin strategy when trying to claim a slot in the {@link RingBuffer}
 * @param <T> {@link Entry} implementation stored in the {@link RingBuffer}
 */
public final class BusySpinSlotClaimer<T extends Entry> extends AbstractSlotClaimer<T> {

    public BusySpinSlotClaimer(final int bufferReverseThreshold,
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
            // busy spin
        }

        return ringBuffer.claimNext();
    }

    @Override
    public T claimSequence(long sequence) {
        final RingBuffer<? extends T> ringBuffer = getRingBuffer();

        final int threshold = ringBuffer.getCapacity() - getBufferReverseThreshold();

        while (sequence - getConsumedEventSequence() >= threshold)
        {
            // busy spin
        }
        return ringBuffer.claimSequence(sequence);
    }
}
