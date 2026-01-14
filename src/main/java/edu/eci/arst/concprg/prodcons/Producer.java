/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class Producer extends Thread {

    private BlockingQueue<Integer> queue = null;

    private int dataSeed = 0;
    private Random rand = null;
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
                // put() bloquea automáticamente si la cola está llena
                queue.put(dataSeed);
                System.out.println("Producer added " + dataSeed + " (Queue size: " + queue.size() + ")");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            try {
                Thread.sleep(100); // Producción rápida: 100ms
            } catch (InterruptedException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
