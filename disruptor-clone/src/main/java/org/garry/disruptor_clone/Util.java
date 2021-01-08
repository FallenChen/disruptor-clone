package org.garry.disruptor_clone;

/**
 * Set of common functions used by The Disruptor
 */
public class Util {


    /**
     * Calculate the next power of 2, greater than or equal to x
     *
     * From Hacker's Delight, Chapter 3
     * @param x
     * @return
     */
    public static int ceilingNextPowerOfTwo(final int x){

        return 1 << (32 - Integer.numberOfLeadingZeros(x - 1));
    }

    /**
     * Get the minimum sequence from an array of {@link EventConsumer}s
     * @param eventConsumers to compare
     * @return the minimum sequence found or Long.MAX_VALUE if the array is empty
     */
    public static long getMinimumSequence(final EventConsumer[] eventConsumers)
    {
        long minimum = Long.MAX_VALUE;
        for (EventConsumer eventConsumer : eventConsumers) {
            long sequence = eventConsumer.getSequence();
            minimum = minimum < sequence ? minimum : sequence;
        }
        return minimum;
    }

    // test
    public static void main(String[] args) {
        System.out.println(ceilingNextPowerOfTwo(3));
        System.out.println(8&2);
    }
}
