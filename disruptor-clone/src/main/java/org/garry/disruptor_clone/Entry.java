package org.garry.disruptor_clone;

/**
 * Entries are the items exchange via a RingBuffer
 */
public interface Entry {

    /**
     * Get the sequence number assigned to this item in the series
     * @return
     */
    long getSequence();

    /**
     * Explicitly set the sequence number for this Entry and a CommitCallback for
     * indicating when the producer is finished with assigning data for exchange
     * @param sequence to be assigned to this Entry
     */
    void setSequence(long sequence, CommitCallback commitCallback);

    /**
     * Indicate that this entry has been updated and is now available to the consumers of a {@link RingBuffer}
     */
    void commit();
}
