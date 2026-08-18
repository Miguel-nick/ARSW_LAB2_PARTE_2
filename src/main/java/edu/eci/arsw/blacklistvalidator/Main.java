package edu.eci.arsw.blacklistvalidator;

import java.util.List;

/**
 *
 * @author hcadavid
 */
public class Main {

    public static void main(String[] args) {

        HostBlackListsValidator hblv = new HostBlackListsValidator();

        // Number of threads with which the search will be performed
        int nThreads = 2;

        List<Integer> blackListOccurrences =
                hblv.checkHost("200.24.34.55", nThreads);

        System.out.println("The host was found in the following blacklists: "
                + blackListOccurrences);

    }

}