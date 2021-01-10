package org.garry.disruptor_clone;

public enum SequenceClaimThreadingStrategy {

    MULTI_THREADED
            {
                @Override
                public SequenceClaimStrategy newInstance() {
                    return new MultiThreadedSequenceClaimStrategy();
                }
            },
    SINGLE_THREAD
            {
                @Override
                public SequenceClaimStrategy newInstance() {
                    return new SingleThreadedSequenceClaimStrategy();
                }
            };

    public abstract SequenceClaimStrategy newInstance();
}
