package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT = 5;

    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     *
     * The search is performed concurrently using the specified number of
     * threads. As soon as, collectively, the threads have detected
     * BLACK_LIST_ALARM_COUNT occurrences, the remaining threads stop
     * searching (early stop), which makes the search more efficient without
     * introducing race conditions, since all threads share a single
     * thread-safe {@link SharedOccurrencesCounter} instance.
     *
     * @param ipAddress suspicious host's IP address.
     * @param nThreads number of threads used to perform the search.
     * @return Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipAddress, int nThreads) {

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();

        int totalServers = skds.getRegisteredServersCount();

        SharedOccurrencesCounter sharedCounter =
                new SharedOccurrencesCounter(BLACK_LIST_ALARM_COUNT);

        BlackListSearchThread[] threads =
                createThreads(ipAddress, nThreads, totalServers, sharedCounter);

        for (BlackListSearchThread thread : threads) {
            thread.start();
        }

        for (BlackListSearchThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        LinkedList<Integer> blackListOccurrences = new LinkedList<>();

        int checkedListsCount = 0;

        for (BlackListSearchThread thread : threads) {

            checkedListsCount += thread.getCheckedLists();

            blackListOccurrences.addAll(thread.getBlackListOccurrences());

        }

        if (sharedCounter.alarmReached()) {
            skds.reportAsNotTrustworthy(ipAddress);
        } else {
            skds.reportAsTrustworthy(ipAddress);
        }

        LOG.log(Level.INFO,
                "Checked Black Lists:{0} of {1}",
                new Object[]{checkedListsCount, totalServers});

        return blackListOccurrences;
    }

    /**
     * Creates the worker threads and distributes the blacklist servers
     * as evenly as possible among them. All threads share the same
     * {@link SharedOccurrencesCounter} instance so they can coordinate the
     * early stop of the search.
     *
     * @param ipAddress host IP to validate.
     * @param nThreads number of threads.
     * @param totalServers total blacklist servers available.
     * @param sharedCounter counter shared by all worker threads.
     * @return array of configured worker threads.
     */
    private BlackListSearchThread[] createThreads(String ipAddress,
                                                  int nThreads,
                                                  int totalServers,
                                                  SharedOccurrencesCounter sharedCounter) {

        BlackListSearchThread[] threads = new BlackListSearchThread[nThreads];

        int blockSize = totalServers / nThreads;
        int remainder = totalServers % nThreads;

        int start = 0;

        for (int i = 0; i < nThreads; i++) {

            int end = start + blockSize;

            if (remainder > 0) {
                end++;
                remainder--;
            }

            threads[i] = new BlackListSearchThread(start, end, ipAddress, sharedCounter);

            start = end;
        }

        return threads;
    }

    private static final Logger LOG =
            Logger.getLogger(HostBlackListsValidator.class.getName());

}