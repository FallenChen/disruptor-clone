package org.garry.disruptor_clone;

import org.garry.disruptor_clone.support.StubEntry;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.action.CustomAction;
import org.jmock.lib.action.DoAllAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    public void shouldWaitForWorkCompleteWhereCompleteWorkThresholdIsAhead() throws AlertException, InterruptedException {
        final long expectedNumberMessages =  10;
        final long expectedWorkSequence = 9;
        fillRingBuffer(expectedNumberMessages);

        mockery.checking(new Expectations()
        {
            {
                one(eventProcessor1).getSequence();
                will(returnValue(expectedWorkSequence));

                one(eventProcessor2).getSequence();
                will(returnValue(expectedWorkSequence));

                one(eventProcessor3).getSequence();
                will(returnValue(expectedWorkSequence));
            }
        });

        long completedWorkSequence = thresholdBarrier.waitFor(expectedWorkSequence);
        assertTrue(completedWorkSequence >= expectedWorkSequence);
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

    @Test
    public void shouldWaitForWorkCompleteWhereAllWorkersAreBlockedOnRingBuffer() throws AlertException, InterruptedException {
        long expectedNumberMessages = 10;
        fillRingBuffer(expectedNumberMessages);

        final StubEventConsumer[] workers = new StubEventConsumer[3];
        for(int i=0, size = workers.length; i< size; i++) {
            workers[i] = new StubEventConsumer();
            workers[i].setSequence(expectedNumberMessages -1 );
        }

        final ThresholdBarrier<StubEntry> barrier = ringBuffer.createBarrier(workers);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                StubEntry entry = ringBuffer.claimNext();
                entry.setValue((int) entry.getSequence());
                entry.commit();
                for (StubEventConsumer stubWorker : workers) {
                    stubWorker.setSequence(entry.getSequence());
                }
            }
        };

        new Thread(runnable).start();

        long expectedWorkSequence = expectedNumberMessages;
        long completedWorkSequence = barrier.waitFor(expectedWorkSequence);
        assertTrue(completedWorkSequence >= expectedWorkSequence);
    }

    @Test
    public void shouldInterruptDuringBusySpin() throws Exception {
        final long expectedNumberMessages = 10;
        fillRingBuffer(expectedNumberMessages);
        final CountDownLatch latch = new CountDownLatch(9);


        mockery.checking(new Expectations(){
            {
                allowing(eventProcessor1).getSequence();
                will(new DoAllAction(countDown(latch),returnValue(8L)));

                allowing(eventProcessor2).getSequence();
                will(new DoAllAction(countDown(latch),returnValue(8L)));

                allowing(eventProcessor3).getSequence();
                will(new DoAllAction(countDown(latch),returnValue(8L)));
            }
        });

        final boolean[] alerted = {false};
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    thresholdBarrier.waitFor(expectedNumberMessages - 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (AlertException e) {
                    alerted[0] = true;
                }
            }
        });
        t.start();
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        thresholdBarrier.alert();
        t.join();
        assertTrue("Thread was not interupted",alerted[0]);
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

    protected Action countDown(final CountDownLatch latch)
    {
        return new CustomAction("Count Down Latch") {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
               latch.countDown();
               return null;
            }
        };
    }
}
