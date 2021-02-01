package org.garry.disruptor_clone;

import org.garry.disruptor_clone.support.TestEntry;
import org.junit.Test;

public final class BatchEventConsumerTest {

    @Test
    public void shouldDoStuff()
    {
        RingBuffer<TestEntry> ringBuffer = new RingBuffer<>(TestEntry.FACTORY, 100);
        ThresholdBarrier<TestEntry> barrier = ringBuffer.createBarrier();
        EventHandler<TestEntry> eventHandler = new EventHandler<TestEntry>() {
            @Override
            public void onEvent(TestEntry entry) {

            }

            @Override
            public void onEndOfBatch() {

            }

            @Override
            public void onCompletion() {

            }
        };

        BatchEventConsumer<TestEntry> batchEventConsumer = new BatchEventConsumer<>(barrier, eventHandler);

        batchEventConsumer.halt();
        batchEventConsumer.run();
        batchEventConsumer.getBarrier();
    }
}
