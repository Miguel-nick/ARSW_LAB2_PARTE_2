package edu.eci.arsw.blacklistvalidator;

/**
 * Thread-safe shared counter used by the {@link BlackListSearchThread}
 * workers to coordinate an early stop of the distributed search.
 *
 * All worker threads share a single instance of this class. Every time a
 * thread finds the IP address in a blacklist server, it reports the finding
 * through {@link #reportOccurrence()}. Once the accumulated number of
 * occurrences (across ALL threads) reaches the configured alarm threshold,
 * {@link #alarmReached()} starts returning {@code true}, and every worker
 * thread checks this flag on each iteration to stop searching the rest of
 * its assigned range.
 *
 * Both the update ({@link #reportOccurrence()}) and the read
 * ({@link #alarmReached()}) operations are synchronized on the same
 * monitor (this object), so there is no race condition between threads
 * incrementing the counter and threads checking whether the threshold was
 * reached.
 */
public class SharedOccurrencesCounter {

    private final int alarmThreshold;
    private int occurrencesCount;

    /**
     * Creates a shared counter configured with the alarm threshold.
     *
     * @param alarmThreshold number of occurrences required to consider the
     *                       host as NOT trustworthy.
     */
    public SharedOccurrencesCounter(int alarmThreshold) {
        this.alarmThreshold = alarmThreshold;
        this.occurrencesCount = 0;
    }

    /**
     * Reports a new occurrence found by a worker thread. This method is
     * synchronized to guarantee that concurrent increments from different
     * threads are never lost (no race condition).
     */
    public synchronized void reportOccurrence() {
        occurrencesCount++;
    }

    /**
     * Indicates whether the collective number of occurrences reported so
     * far already reached the alarm threshold. Worker threads use this to
     * decide whether they should keep searching or stop early.
     *
     * @return true if the alarm threshold has been reached.
     */
    public synchronized boolean alarmReached() {
        return occurrencesCount >= alarmThreshold;
    }

    /**
     * Returns the current occurrences count (mainly for logging/reporting
     * purposes once all threads have finished).
     *
     * @return the current occurrences count.
     */
    public synchronized int getOccurrencesCount() {
        return occurrencesCount;
    }

}