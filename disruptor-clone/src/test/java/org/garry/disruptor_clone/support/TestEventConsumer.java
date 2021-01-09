package org.garry.disruptor_clone.support;

import org.garry.disruptor_clone.EventConsumer;
import org.garry.disruptor_clone.ThresholdBarrier;

public final class TestEventConsumer implements EventConsumer {

    private volatile long sequence = -7;

    public TestEventConsumer(final long sequence) {
        this.sequence = sequence;
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public ThresholdBarrier getBarrier() {
        return null;
    }

    @Override
    public void halt() {

    }

    @Override
    public void run() {

    }
}
