package org.garry.disruptor_clone;

import org.garry.disruptor_clone.support.DaemonThreadFactory;
import org.garry.disruptor_clone.support.ReadingCallable;
import org.garry.disruptor_clone.support.StubEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class RingBufferTest {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
    private RingBuffer<StubEntry> ringBuffer;
    private ThresholdBarrier barrier;

    @Before
    public void setUp()
    {
        ringBuffer = new RingBuffer<>(StubEntry.FACTORY,20);
        barrier = ringBuffer.createBarrier();
    }

    @Test
    public void shouldClaimAndGet() throws AlertException, InterruptedException {
        assertEquals(RingBuffer.INITIAL_CURSOR_VALUE, ringBuffer.getCursor());

        StubEntry expectedEntry = new StubEntry(2701);
        StubEntry oldEntry = ringBuffer.claimNext();
        oldEntry.copy(expectedEntry);
        oldEntry.commit();

        long sequence = barrier.waitFor(0);
        assertEquals(0,sequence);

        StubEntry entry = ringBuffer.get(sequence);
        assertEquals(expectedEntry,entry);

        assertEquals(0L, ringBuffer.getCursor());
    }

    @Test
    public void shouldClaimAndGetWithTimeout() throws Exception{
        assertEquals(RingBuffer.INITIAL_CURSOR_VALUE, ringBuffer.getCursor());

        StubEntry exceptedEntry = new StubEntry(2701);

        StubEntry oldEntry = ringBuffer.claimNext();
        oldEntry.copy(exceptedEntry);
        oldEntry.commit();

        long sequence = barrier.waitFor(0, 5, TimeUnit.MILLISECONDS);
        assertEquals(0,sequence);

        StubEntry entry = ringBuffer.get(sequence);
        assertEquals(exceptedEntry,entry);

        assertEquals(0L,ringBuffer.getCursor());
    }

    @Test
    public void shouldGetWIthTimeout() throws InterruptedException {
        long sequence = barrier.waitFor(0, 5, TimeUnit.MILLISECONDS);
        assertEquals(RingBuffer.INITIAL_CURSOR_VALUE,sequence);
    }

    @Test
    public void shouldClaimAndGetInSeparateThread() throws Exception{
        Future<List<StubEntry>> messages = getMessages(0,0);
        StubEntry expectedEntry = new StubEntry(2701);

        StubEntry oldEntry = ringBuffer.claimNext();
        oldEntry.copy(expectedEntry);
        oldEntry.commit();

        assertEquals(expectedEntry,messages.get().get(0));
    }

    private Future<List<StubEntry>> getMessages(final int initial, int toWaitFor) throws BrokenBarrierException, InterruptedException {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        final Future<List<StubEntry>> f = EXECUTOR.submit(new ReadingCallable(barrier, ringBuffer, initial, toWaitFor));
        barrier.await();
        return f;
    }
}
