/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.concurrent.BlockingQueue;
import java.util.Random;

/**
 *
 * @author Miguel Sandoval
 */
public class Producer extends Thread {

    private BlockingQueue<Integer> queue;

    private int dataSeed = 0;
    private Random rand;
    private final long stockLimit;

    public Producer(BlockingQueue<Integer> queue, long stockLimit) {
        this.queue = queue;
        rand = new Random(System.currentTimeMillis());
        this.stockLimit = stockLimit;
    }

    @Override
    public void run() {
        while (true) {

            dataSeed = dataSeed + rand.nextInt(100);

            try {
                queue.put(dataSeed);
                System.out.println("Producer added " + dataSeed);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}