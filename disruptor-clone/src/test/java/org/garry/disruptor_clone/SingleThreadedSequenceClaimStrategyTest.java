package org.garry.disruptor_clone;

import org.junit.Assert;
import org.junit.Test;

public final class SingleThreadedSequenceClaimStrategyTest {

    @Test
    public void shouldSetThenIncrement()
    {
        SingleThreadedSequenceClaimStrategy sequenceClaimStrategy = new SingleThreadedSequenceClaimStrategy();

        sequenceClaimStrategy.setSequence(7L);

        Assert.assertEquals(7L,sequenceClaimStrategy.getAndIncrement());
        Assert.assertEquals(8L,sequenceClaimStrategy.getAndIncrement());

    }
}
