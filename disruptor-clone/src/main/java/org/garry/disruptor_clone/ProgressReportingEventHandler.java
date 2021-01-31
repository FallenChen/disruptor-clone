package org.garry.disruptor_clone;

public interface ProgressReportingEventHandler<T extends Entry> extends EventHandler<T> {

    void setProgressTracker(final BatchEventConsumer.ProgressTrackerCallback progressTrackerCallback);
}
