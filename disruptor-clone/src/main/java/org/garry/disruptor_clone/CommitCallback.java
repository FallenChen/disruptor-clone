package org.garry.disruptor_clone;

public interface CommitCallback {

    void commit(long sequence);
}
