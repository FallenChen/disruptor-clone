package org.garry.disruptor_clone;


import org.junit.Assert;
import org.junit.Test;

public final class UntilTest {

    @Test
    public void shouldReturnNextPowerOfTwo()
    {
        int powerOfTwo = Util.ceilingNextPowerOfTwo(1000);
        Assert.assertEquals(1024,powerOfTwo);
    }

    @Test
    public void shouldReturnExactPowerOfTwo()
    {
        int powerOfTwo = Util.ceilingNextPowerOfTwo(1024);
        Assert.assertEquals(1024,powerOfTwo);
    }
}
