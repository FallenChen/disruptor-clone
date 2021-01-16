package org.garry.disruptor_clone.support;

import org.garry.disruptor_clone.AbstractEntry;
import org.garry.disruptor_clone.Factory;

import java.util.Objects;

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

    public final static Factory<StubEntry> FACTORY =
            new Factory<StubEntry>() {
                @Override
                public StubEntry create() {
                    return new StubEntry(-1);
                }
            };

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;
        StubEntry other = (StubEntry) o;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }
}
