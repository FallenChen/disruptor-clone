package org.garry.disruptor_clone;

import static org.garry.disruptor_clone.Util.ceilingNextPowerOfTwo;

/**
 * Ring based store of reusable entries containing the data representing an {@link Entry} being exchanged between producers and consumers
 *
 * @param <T> Entry implementation storing the data for sharing during exchange or parallel coordination of an event
 */
public final class RingBuffer<T extends Entry> {

    /**
     * Set to -1 as sequence starting point
     */
    public static final long INITIAL_CURSOR_VALUE = -1L;

    private volatile long cursor = INITIAL_CURSOR_VALUE;


    private final Object[] entries;

    public RingBuffer(final int size) {
        int sizeAsPowerOfTwp = ceilingNextPowerOfTwo(size);
        entries = new Object[sizeAsPowerOfTwp];
    }


}
