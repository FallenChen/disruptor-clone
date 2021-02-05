package org.garry.disruptor_clone.support;

import org.garry.disruptor_clone.ConsumerBarrier;
import org.garry.disruptor_clone.RingBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

public final class ReadingCallable implements Callable<List<StubEntry>> {

    private final RingBuffer<StubEntry> ringBuffer;
    private final long toWaitFor;
    private final long initial;
    private final CyclicBarrier cb;
    private final ConsumerBarrier consumerBarrier;

    public ReadingCallable(final RingBuffer<StubEntry> ringBuffer,
                           final long toWaitFor,
                           final long initial,
                           final CyclicBarrier cb) {
        this.ringBuffer = ringBuffer;
        this.toWaitFor = toWaitFor;
        this.initial = initial;
        this.cb = cb;
        consumerBarrier = ringBuffer.createConsumerBarrier();
    }


    @Override
    public List<StubEntry> call() throws Exception {
        cb.await();
        final ArrayList<StubEntry> messages = new ArrayList<>();
        consumerBarrier.waitFor(toWaitFor);
        for (long l = initial; l <=toWaitFor; l++)
        {
            messages.add(ringBuffer.getEntry(l));
        }
        return messages;
    }
}
