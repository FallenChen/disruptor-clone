package org.garry.disruptor_clone;

public interface SequenceClaimStrategy {

    long getAndIncrement();

    void setSequence(long sequence);
}
