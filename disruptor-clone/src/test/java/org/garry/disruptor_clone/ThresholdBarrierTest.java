package org.garry.disruptor_clone;

import org.garry.disruptor_clone.support.StubEntry;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JMock.class)
public class ThresholdBarrierTest {

    private Mockery mockery;
    private RingBuffer<StubEntry> ringBuffer;
    private EventConsumer eventProcessor1;
    private EventConsumer eventProcessor2;
    private EventConsumer eventProcessor3;
    private ThresholdBarrier<StubEntry> thresholdBarrier;

    @Before
    public void setUp()
    {
        mockery = new Mockery();
        ringBuffer = new RingBuffer<>(StubEntry.FACTORY,20);
        eventProcessor1 = mockery.mock(EventConsumer.class,"eventConsumer1");
        eventProcessor2 = mockery.mock(EventConsumer.class,"eventConsumer2");
        eventProcessor3 = mockery.mock(EventConsumer.class,"eventConsumer3");
        thresholdBarrier = ringBuffer.createBarrier(eventProcessor1,eventProcessor2,eventProcessor3);
    }

    @Test
    public void shouldGetMinOffWorkers()
    {
        final long expectedMinimum = 3;
        mockery.checking(new Expectations()
        {
            {
                one(eventProcessor1).getSequence();
                will(returnValue(expectedMinimum));

                one(eventProcessor2).getSequence();
                will(returnValue(86L));

                one(eventProcessor3).getSequence();
                will(returnValue(2384378L));
            }
        });
        ringBuffer.claimSequence(2384378L).commit();
        assertEquals(expectedMinimum,thresholdBarrier.getProcessedEventSequence());
    }

    @Test
    public void shouldWaitForWorkCompleteWhereCompleteWorkThresholdIsBehind() throws AlertException, InterruptedException {
        long exceptedNumberMessages = 10;
        fillRingBuffer(exceptedNumberMessages);

        final StubEventConsumer[] eventProcessors = new StubEventConsumer[3];
        for(int i=0; i < eventProcessors.length;i++)
        {
            eventProcessors[i] = new StubEventConsumer();
            eventProcessors[i].setSequence(exceptedNumberMessages-2);
        }

        ThresholdBarrier<StubEntry> barrier = ringBuffer.createBarrier(eventProcessors);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (StubEventConsumer stubWorker : eventProcessors) {
                    stubWorker.setSequence(stubWorker.getSequence() + 1);
                }
            }
        };
        new Thread(runnable).start();;
        long exceptedWorkSequence = exceptedNumberMessages - 1;
        long completedWorkSequence = barrier.waitFor(exceptedWorkSequence);
        assertTrue(completedWorkSequence >= exceptedWorkSequence);

    }

    private void fillRingBuffer(long expectedNumberMessages)
    {
        for(long i = 0; i <expectedNumberMessages; i++)
        {
            StubEntry entry = ringBuffer.claimNext();
            entry.setValue((int) i);
            entry.commit();
        }
    }

    private static class StubEventConsumer implements EventConsumer
    {
        private volatile long sequence;

        public void setSequence(long sequence) {
            this.sequence = sequence;
        }

        @Override
        public long getSequence() {
            return sequence;
        }

        @Override
        public void halt() {

        }

        @Override
        public ThresholdBarrier getBarrier() {
            return null;
        }

        @Override
        public void run() {

        }
    }
}
