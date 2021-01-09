package org.garry.disruptor_clone.support;

import org.garry.disruptor_clone.AbstractEntry;
import org.garry.disruptor_clone.Factory;

public final class TestEntry extends AbstractEntry {

    @Override
    public String toString() {
        return "Test Entry";
    }

    public final static Factory<TestEntry> FACTORY = new Factory<TestEntry>() {
                @Override
                public TestEntry create() {
                    return new TestEntry();
                }
            };
}
