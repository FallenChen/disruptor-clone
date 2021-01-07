package org.garry.disruptor_clone;

/**
 * Base implementation provided for ease of use
 */
public abstract class AbstractEntry implements Entry{

    private long sequence;
    private CommitCallback commitCallback;

    @Override
    public void commit() {
        commitCallback.commit(sequence);
    }


    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(long sequence, CommitCallback commitCallback) {
        this.sequence = sequence;
        this.commitCallback = commitCallback;
    }
}
