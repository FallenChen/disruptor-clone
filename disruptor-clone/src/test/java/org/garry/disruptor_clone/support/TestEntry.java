package org.garry.disruptor_clone.support;

import org.garry.disruptor_clone.AbstractEntry;
import org.garry.disruptor_clone.EntryFactory;
import org.junit.Test;

public final class TestEntry extends AbstractEntry {

    @Override
    public String toString() {
       return "Test Entry";
    }

    public final static EntryFactory<TestEntry> FACTORY = new EntryFactory<TestEntry>() {
        @Override
        public TestEntry create() {
           return new TestEntry();
        }
    };
}
