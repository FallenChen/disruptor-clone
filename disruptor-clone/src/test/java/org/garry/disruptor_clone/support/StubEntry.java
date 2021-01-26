package org.garry.disruptor_clone.support;

import org.garry.disruptor_clone.AbstractEntry;
import org.garry.disruptor_clone.EntryFactory;

public final class StubEntry extends AbstractEntry {

    private int value;

    public StubEntry(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void copy(StubEntry entry)
    {
        value = entry.value;
    }

    public final static EntryFactory<StubEntry> FACTORY = new EntryFactory<StubEntry>() {
        @Override
        public StubEntry create() {
           return new StubEntry(-1);
        }
    };

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        StubEntry other = (StubEntry)obj;

        return value == other.value;
    }

}
