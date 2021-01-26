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
    public void shouldGetWithTimeout() throws InterruptedException, AlertException {

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

    @Test
    public void shouldClaimAndGetMultipleMessages() throws AlertException, InterruptedException {
        int numMessages = ringBuffer.getCapacity();
        for(int i=0; i < numMessages; i++)
        {
            StubEntry entry = ringBuffer.claimNext();
            entry.setValue(i);
            entry.commit();
        }

        int expectedSequence = numMessages - 1;
        long available = barrier.waitFor(expectedSequence);
        assertEquals(expectedSequence,available);

        for(int i=0; i< numMessages; i++)
        {
            assertEquals(i,ringBuffer.get(i).getValue());
        }
    }

    @Test
    public void shouldWrap() throws AlertException, InterruptedException {
        int numMessages = ringBuffer.getCapacity();
        int offset = 1000;
        for(int i = 0; i < numMessages + offset; i++)
        {
            StubEntry entry = ringBuffer.claimNext();
            entry.setValue(i);
            entry.commit();
        }

        int expectedSequence = numMessages + offset - 1;
        long available = barrier.waitFor(expectedSequence);
        assertEquals(expectedSequence, available);

        for(int i=offset; i<numMessages + offset; i++)
        {
            assertEquals(i, ringBuffer.get(i).getValue());
        }
    }

    @Test
    public void shouldSetAtSpecificSequence() throws AlertException, InterruptedException {
        long expectedSequence = 5;
        StubEntry expectedEntry = ringBuffer.claimSequence(expectedSequence);
        expectedEntry.setValue((int) expectedSequence);
        expectedEntry.commit();

        long sequence = barrier.waitFor(expectedSequence);
        assertEquals(expectedSequence, sequence);

        StubEntry entry = ringBuffer.get(sequence);
        assertEquals(expectedEntry, entry);

        assertEquals(expectedSequence, ringBuffer.getCursor());
    }


    private Future<List<StubEntry>> getMessages(final int initial, int toWaitFor)
            throws BrokenBarrierException, InterruptedException {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        final Future<List<StubEntry>> f = EXECUTOR.submit(new ReadingCallable(barrier, ringBuffer, initial, toWaitFor));
        barrier.await();
        return f;
    }
}
