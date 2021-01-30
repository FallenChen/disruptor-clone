package org.garry.disruptor_clone;

import org.garry.disruptor_clone.support.TestEntry;
import org.garry.disruptor_clone.support.TestEventConsumer;
import org.junit.Assert;
import org.junit.Test;

public final class BusySpinSlotClaimerTest {

    @Test
    public void shouldClaimFirstSlot()
    {
        RingBuffer<TestEntry> ringBuffer = new RingBuffer<>(TestEntry.FACTORY, 100);
        TestEventConsumer eventProcessor = new TestEventConsumer(0);

        BusySpinSlotClaimer<TestEntry> slotClaimer = new BusySpinSlotClaimer<>(0, ringBuffer, eventProcessor);

        TestEntry entry = slotClaimer.claimNext();

        Assert.assertEquals(0L,entry.getSequence());
    }

    @Test
    public void shouldClaimSequence()
    {
        int sequence = 15;

        RingBuffer<TestEntry> ringBuffer = new RingBuffer<>(TestEntry.FACTORY, 100);
        TestEventConsumer eventProcessor = new TestEventConsumer(0);

        BusySpinSlotClaimer<TestEntry> slotClaimer = new BusySpinSlotClaimer<>(20, ringBuffer, eventProcessor);

        TestEntry entry = slotClaimer.claimSequence(sequence);

        Assert.assertEquals(sequence, entry.getSequence());
    }
}
