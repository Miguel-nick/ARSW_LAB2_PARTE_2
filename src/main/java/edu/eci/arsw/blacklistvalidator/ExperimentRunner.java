package edu.eci.arsw.blacklistvalidator;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Experiment runner for Parte III: runs checkHost with different thread counts,
 * measures elapsed time and writes results to a CSV file.
 */
public class ExperimentRunner {

    public static void main(String[] args) throws Exception {

        String ip = args.length >= 1 ? args[0] : "202.24.34.55"; // dispersed IP by default
        int iterations = args.length >= 2 ? Integer.parseInt(args[1]) : 5;
        HostBlackListsValidator validator = new HostBlackListsValidator();

        int cores = Runtime.getRuntime().availableProcessors();
        List<Integer> threadCounts = new ArrayList<>();
        // default set: 1, cores, 2*cores, 50, 100
        threadCounts.add(1);
        threadCounts.add(cores);
        threadCounts.add(Math.max(1, cores * 2));
        threadCounts.add(50);
        threadCounts.add(100);

        // Allow the user to append additional thread counts as further arguments
        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                try {
                    int v = Integer.parseInt(args[i]);
                    if (!threadCounts.contains(v)) threadCounts.add(v);
                } catch (NumberFormatException ex) {
                    // ignore non-integers
                }
            }
        }

        String outFile = "experiments_parte3.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(outFile, false))) {
            pw.println("threads,run,elapsedMs,ip");

            System.out.println("Thread counts to test: " + threadCounts);

            for (int t : threadCounts) {
                int threadsToUse = Math.max(1, t);
                // Warm-up (JIT)
                System.out.println("Warmup for threads=" + threadsToUse);
                try {
                    validator.checkHost(ip, threadsToUse);
                } catch (Exception ex) {
                    // continue
                }
                Thread.sleep(200);

                for (int r = 1; r <= iterations; r++) {
                    System.out.printf("Running threads=%d run=%d...%n", threadsToUse, r);
                    long start = System.nanoTime();
                    validator.checkHost(ip, threadsToUse);
                    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                    pw.printf("%d,%d,%d,%s%n", threadsToUse, r, elapsedMs, ip);
                    pw.flush();
                    Thread.sleep(300);
                }
            }
        }

        System.out.println("Experiments finished. Output: " + outFile);
    }

}