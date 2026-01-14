package edu.eci.arsw.partII.blacklistvalidator;

import edu.eci.arsw.partII.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Optimized worker thread for searching IP addresses within a specific segment of blacklist servers.
 * Features early termination when the global occurrence count reaches the alarm threshold.
 *
 * @author David Velásquez, Jesús Pinzón
 * @version 2.0
 * @since 2025-09-03
 */
public class BlackListSearchThread extends Thread {

    private int startIndex;
    private int endIndex;
    private String ipAddress;
    private int occurrencesFound;
    private List<Integer> blackListOccurrences;
    private HostBlacklistsDataSourceFacade dataSource;
    private AtomicInteger globalOccurrenceCount;
    private final int alarmThreshold;
    private volatile boolean shouldStop = false;

    /**
     * Constructs a search thread for a specific server segment with shared occurrence tracking.
     *
     * @param startIndex the starting server index (inclusive)
     * @param endIndex the ending server index (inclusive)
     * @param ipAddress the IP address to search for
     * @param globalOccurrenceCount shared atomic counter for total occurrences across all threads
     * @param alarmThreshold the maximum number of occurrences before stopping the search
     */
    public BlackListSearchThread(int startIndex, int endIndex, String ipAddress, 
                                AtomicInteger globalOccurrenceCount, int alarmThreshold) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.ipAddress = ipAddress;
        this.occurrencesFound = 0;
        this.blackListOccurrences = new LinkedList<>();
        this.dataSource = HostBlacklistsDataSourceFacade.getInstance();
        this.globalOccurrenceCount = globalOccurrenceCount;
        this.alarmThreshold = alarmThreshold;
    }

    /**
     * Executes the optimized blacklist search within the assigned server segment.
     *
     * Features:
     * - Early termination when global threshold is reached
     * - Thread-safe shared occurrence counting
     * - Detailed progress tracking and logging
     * - Race condition prevention through atomic operations
     */
    @Override
    public void run() {
        System.out.println("Thread " + Thread.currentThread().getName() + 
            " Started - Checking servers [" + startIndex + " - " + endIndex + "]");

        for (int i = startIndex; i <= endIndex && !shouldStop; i++) {
            
            // Check if we should stop due to global threshold being reached
            if (globalOccurrenceCount.get() >= alarmThreshold) {
                System.out.println("Thread " + Thread.currentThread().getName() + 
                    " EARLY TERMINATION - Global threshold reached at server " + i);
                shouldStop = true;
                break;
            }

            if (dataSource.isInBlackListServer(i, ipAddress)) {
                blackListOccurrences.add(i);
                occurrencesFound++;
                
                // Atomically increment global counter and get new value
                int newGlobalCount = globalOccurrenceCount.incrementAndGet();
                
                System.out.println("Thread " + Thread.currentThread().getName() + 
                    " Found IP on blacklist #" + i + " (Global count: " + newGlobalCount + ")");
                
                // Check if we've reached the threshold after this discovery
                if (newGlobalCount >= alarmThreshold) {
                    System.out.println("Thread " + Thread.currentThread().getName() + 
                        " STOPPING - Alarm threshold (" + alarmThreshold + ") reached!");
                    shouldStop = true;
                    break;
                }
            }
        }

        System.out.println("Thread " + Thread.currentThread().getName() + 
            " Finished - Found " + occurrencesFound + " occurrences" + 
            (shouldStop ? " (STOPPED EARLY)" : " (COMPLETED RANGE)"));
    }

    /**
     * Signals this thread to stop its search operation.
     */
    public void requestStop() {
        this.shouldStop = true;
    }

    /**
     * Returns the number of blacklist occurrences found by this thread.
     * @return count of occurrences found in the assigned segment
     */
    public int getOccurrencesFound() {
        return occurrencesFound;
    }

    /**
     * Returns the list of blacklist indices where the IP was found.
     * @return list of server indices containing the target IP
     */
    public List<Integer> getBlackListOccurrences() {
        return blackListOccurrences;
    }

    /**
     * Returns a string representation of the assigned segment range.
     * @return formatted string showing the segment boundaries
     */
    public String getSegmentInfo() {
        return "[" + startIndex + " - " + endIndex + "]";
    }

    /**
     * Returns whether this thread stopped early due to threshold being reached.
     * @return true if thread terminated early, false if it completed its full range
     */
    public boolean wasStoppedEarly() {
        return shouldStop;
    }
}
