package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;

/**
 * Thread responsible for searching a specific range of blacklist servers
 * for a given IP address.
 *
 * Each instance processes an independent segment of the available blacklist
 * servers, keeping track of:
 * <ul>
 *     <li>The number of blacklist servers checked.</li>
 *     <li>The blacklist servers where the IP address was found.</li>
 * </ul>
 *
 * Part II (efficiency improvement): this thread now stops searching its
 * assigned range as soon as the {@link SharedOccurrencesCounter} shared by
 * all worker threads indicates that the alarm threshold has already been
 * reached collectively, avoiding unnecessary work. The shared counter is
 * thread-safe, so no race condition is introduced by this early-stop check.
 *
 * @author Miguel Angel Sandoval
 */
public class BlackListSearchThread extends Thread {

    private final int start;
    private final int end;
    private final String ipAddress;

    private final HostBlacklistsDataSourceFacade facade;

    private final SharedOccurrencesCounter sharedCounter;

    private final List<Integer> blackListOccurrences;

    private int checkedLists;

    /**
     * Creates a thread that searches a range of blacklist servers.
     *
     * @param start the initial blacklist server index (inclusive).
     * @param end the final blacklist server index (exclusive).
     * @param ipAddress the IP address to search for.
     * @param sharedCounter counter shared by all worker threads, used to
     *                      detect that the alarm threshold has already been
     *                      reached and stop the search early.
     */
    public BlackListSearchThread(int start, int end, String ipAddress,
                                 SharedOccurrencesCounter sharedCounter) {
        this.start = start;
        this.end = end;
        this.ipAddress = ipAddress;

        this.facade = HostBlacklistsDataSourceFacade.getInstance();

        this.sharedCounter = sharedCounter;

        this.blackListOccurrences = new LinkedList<>();

        this.checkedLists = 0;
    }

    /**
     * Executes the search over the assigned range of blacklist servers.
     * For each blacklist checked, the thread records whether the IP address
     * is present. The loop stops as soon as the shared counter reports that
     * the alarm threshold was already reached by the whole set of threads,
     * even if this thread has not finished its assigned range.
     */
    @Override
    public void run() {

        for (int i = start; i < end && !sharedCounter.alarmReached(); i++) {

            checkedLists++;

            if (facade.isInBlackListServer(i, ipAddress)) {

                blackListOccurrences.add(i);

                sharedCounter.reportOccurrence();
            }
        }
    }

    /**
     * Returns the number of blacklist servers checked by this thread.
     *
     * @return the number of blacklist servers checked.
     */
    public int getCheckedLists() {
        return checkedLists;
    }

    /**
     * Returns the identifiers of the blacklist servers where the IP address
     * was found.
     *
     * @return a list containing the blacklist server identifiers.
     */
    public List<Integer> getBlackListOccurrences() {
        return blackListOccurrences;
    }

}