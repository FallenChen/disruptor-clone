package org.garry.disruptor_clone.code_example;

import org.garry.disruptor_clone.Entry;

public class EntryValue implements Entry {

    @Override
    public long getSequence() {
        return 0;
    }

    @Override
    public void setSequence(long sequence) {

    }
}
