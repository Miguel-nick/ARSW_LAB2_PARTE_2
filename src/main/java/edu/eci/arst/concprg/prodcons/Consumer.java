/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author hcadavid
 */
public class Consumer extends Thread {

    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // take() bloquea automáticamente si la cola está vacía
                int elem = queue.take();
                System.out.println("Consumer consumes " + elem + " (Queue size: " + queue.size() + ")");

                // Consumo lento: 2 segundos
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
