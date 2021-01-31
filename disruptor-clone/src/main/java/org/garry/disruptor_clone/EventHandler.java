package org.garry.disruptor_clone;

public interface EventHandler<T extends Entry> {

    void onEvent(T entry);

    void onEndOfBatch();

    void onCompletion();
}
