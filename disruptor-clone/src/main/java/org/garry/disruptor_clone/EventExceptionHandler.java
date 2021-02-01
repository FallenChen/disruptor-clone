package org.garry.disruptor_clone;

public interface EventExceptionHandler {

    void handle(Exception ex, Entry currentEntry);
}
