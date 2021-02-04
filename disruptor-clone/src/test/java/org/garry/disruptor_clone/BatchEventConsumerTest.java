package org.garry.disruptor_clone;

import org.garry.disruptor_clone.support.TestEntry;
import org.junit.Test;

public final class BatchEventConsumerTest {

    @Test
    public void shouldDoStuff()
    {
        RingBuffer<TestEntry> ringBuffer = new RingBuffer<>(TestEntry.FACTORY, 100);
        ConsumerBarrier<TestEntry> barrier = ringBuffer.createConsumerBarrier();
        BatchHandler<TestEntry> eventHandler = new BatchHandler<TestEntry>() {

            @Override
            public void onAvailable(TestEntry entry) throws Exception {

            }

            @Override
            public void onEndOfBatch() throws Exception {

            }

            @Override
            public void onCompletion() {

            }
        };

        BatchConsumer<TestEntry> batchEventConsumer = new BatchConsumer<>(barrier, eventHandler);

        batchEventConsumer.halt();
        batchEventConsumer.run();
        batchEventConsumer.getConsumerBarrier();
    }
}
