package org.garry.disruptor_clone;

/**
 * Used to alert consumers waiting at a {@link ThresholdBarrier} of status changes
 *
 * It does not fill in a stack trace for performance reasons
 */
public class AlertException extends Exception{

    /**
     * Overridden so the stack trace is not filled in
     * @return
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
