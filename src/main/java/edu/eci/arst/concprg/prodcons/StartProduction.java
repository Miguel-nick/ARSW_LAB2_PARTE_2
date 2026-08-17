/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Miguel Sandoval
 */
public class StartProduction {

    public static void main(String[] args) {

        // Stock máximo de 2 elementos para prueba
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(2);

        new Producer(queue, 5).start();

        new Consumer(queue).start();
    }
}