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

    public static void main(String[] args) {
        System.out.println(ceilingNextPowerOfTwo(3));
    }
}
