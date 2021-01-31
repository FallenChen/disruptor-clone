package org.garry.disruptor_clone;

public final class BatchEventConsumer<T extends Entry> implements EventConsumer {

    private volatile long sequence = -1L;
    private volatile boolean running = true;

    private final ThresholdBarrier<T> barrier;
    private final EventHandler<T> handler;

    private final boolean noProgressTracker;

    public BatchEventConsumer(final ThresholdBarrier<T> barrier,
                              final EventHandler<T> handler) {
        this.barrier = barrier;
        this.handler = handler;
        this.noProgressTracker = true;
    }



    @Override
    public long getSequence() {
        return sequence;
    }



    @Override
    public void halt() {
        running = false;
    }

    @Override
    public ThresholdBarrier getBarrier() {
        return barrier;
    }

    @Override
    public void run() {

    }

    public final class ProgressTrackerCallback
    {
        public void onCompleted(final long sequence)
        {
            BatchEventConsumer.this.sequence = sequence;
        }
    }

}



