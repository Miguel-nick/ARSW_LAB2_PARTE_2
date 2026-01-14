package edu.eci.arsw.partII.spamkeywordsdatasource;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe facade providing access to blacklist server data sources.
 *
 * Implements the Singleton pattern to ensure consistent access across multiple threads.
 * Simulates blacklist server queries with predetermined data for testing purposes.
 *
 * NOTE: DON'T MODIFY THIS CLASS!
 *
 * @author hcadavid
 * @version 1.0
 */
public class HostBlacklistsDataSourceFacade {

    static ConcurrentHashMap<Tuple<Integer, String>, Object> blistocurrences = new ConcurrentHashMap<>();

    static {
        Object anyObject = new Object();
        // to be found by a single thread
        blistocurrences.put(new Tuple<>(23, "200.24.34.55"), anyObject);
        blistocurrences.put(new Tuple<>(50, "200.24.34.55"), anyObject);
        blistocurrences.put(new Tuple<>(200, "200.24.34.55"), anyObject);
        blistocurrences.put(new Tuple<>(1000, "200.24.34.55"), anyObject);
        blistocurrences.put(new Tuple<>(500, "200.24.34.55"), anyObject);

        // to be found through all threads
        blistocurrences.put(new Tuple<>(29, "202.24.34.55"), anyObject);
        blistocurrences.put(new Tuple<>(10034, "202.24.34.55"), anyObject);
        blistocurrences.put(new Tuple<>(20200, "202.24.34.55"), anyObject);
        blistocurrences.put(new Tuple<>(31000, "202.24.34.55"), anyObject);
        blistocurrences.put(new Tuple<>(70500, "202.24.34.55"), anyObject);

        // to be found through all threads
        blistocurrences.put(new Tuple<>(39, "202.24.34.54"), anyObject);
        blistocurrences.put(new Tuple<>(10134, "202.24.34.54"), anyObject);
        blistocurrences.put(new Tuple<>(20300, "202.24.34.54"), anyObject);
        blistocurrences.put(new Tuple<>(70210, "202.24.34.54"), anyObject);
    }

    private static HostBlacklistsDataSourceFacade instance = new HostBlacklistsDataSourceFacade();

    private Map<String, Integer> threadHits = new ConcurrentHashMap<>();

    private String lastConfig = null;

    private int lastIndex = 0;

    private HostBlacklistsDataSourceFacade() {

    }

    public static HostBlacklistsDataSourceFacade getInstance() {
        return instance;
    }

    public int getRegisteredServersCount() {
        return 80000;
    }

    public boolean isInBlackListServer(int serverNumber, String ip) {

        threadHits.computeIfPresent(Thread.currentThread().getName(), (k, v) -> v + 1);
        threadHits.putIfAbsent(Thread.currentThread().getName(), 1);

        if (System.getProperty("threadsinfo") != null && System.getProperty("threadsinfo").compareToIgnoreCase("true") == 0) {
            lastConfig = threadHits.toString();
            lastIndex = serverNumber;
        }
        try {
            Thread.sleep(0, 1);
        } catch (InterruptedException ex) {
            Logger.getLogger(HostBlacklistsDataSourceFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return blistocurrences.containsKey(new Tuple<>(serverNumber, ip));

    }

    private static final Logger LOG = Logger.getLogger(HostBlacklistsDataSourceFacade.class.getName());

    public void reportAsNotTrustworthy(String host) {
        LOG.info("HOST " + host + " Reported as NOT trustworthy");
        if (System.getProperty("threadsinfo") != null && System.getProperty("threadsinfo").compareToIgnoreCase("true") == 0) {
            System.out.println("Total threads:" + threadHits.keySet().size());
            System.out.println(lastConfig);
            System.out.println(lastIndex);
        }
    }

    public void reportAsTrustworthy(String host) {
        LOG.info("HOST " + host + " Reported as trustworthy");
    }

}

class Tuple<T1, T2> {

    T1 firstElement;
    T2 secondElement;

    public Tuple(T1 firstElement, T2 secondElement) {
        this.firstElement = firstElement;
        this.secondElement = secondElement;
    }

    public T1 getFirstElement() {
        return firstElement;
    }

    public void setFirstElement(T1 firstElement) {
        this.firstElement = firstElement;
    }

    public T2 getSecondElement() {
        return secondElement;
    }

    public void setSecondElement(T2 secondElement) {
        this.secondElement = secondElement;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.firstElement);
        hash = 79 * hash + Objects.hashCode(this.secondElement);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tuple<?, ?> other = (Tuple<?, ?>) obj;
        if (!Objects.equals(this.firstElement, other.firstElement)) {
            return false;
        }
        if (!Objects.equals(this.secondElement, other.secondElement)) {
            return false;
        }
        return true;
    }

}
