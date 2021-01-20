package org.garry.disruptor_clone.support;

import org.garry.disruptor_clone.RingBuffer;
import org.garry.disruptor_clone.ThresholdBarrier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

public final class ReadingCallable implements Callable<List<StubEntry>> {

    private final RingBuffer<StubEntry> ringBuffer;
    private final long toWaitFor;
    private final  long initial;
    private final CyclicBarrier cb;
    private final ThresholdBarrier thresholdBarrier;

    public ReadingCallable(final CyclicBarrier cb,
                           final RingBuffer<StubEntry> ringBuffer,
                           final long toWaitFor,
                           final long initial) {
        this.ringBuffer = ringBuffer;
        this.toWaitFor = toWaitFor;
        this.initial = initial;
        this.cb = cb;
        thresholdBarrier = ringBuffer.createBarrier();
    }

    @Override
    public List<StubEntry> call() throws Exception {
       cb.await();
       final List<StubEntry> messages = new ArrayList<>();
       thresholdBarrier.waitFor(toWaitFor);
       for (long l = initial; l <= toWaitFor; l++)
       {
           messages.add(ringBuffer.get(l));
       }
       return messages;
    }
}
