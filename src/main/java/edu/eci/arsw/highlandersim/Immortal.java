package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;

    private int health;

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    public boolean pause = false;

    private final Object lock = new Object();

    private boolean alive = true;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }

    public void run() {
        while (health > 0) {
            checkPause();
            Immortal im;
            int myIndex = immortalsPopulation.indexOf(this);
            int nextFighterIndex = r.nextInt(immortalsPopulation.size());
            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            if (nextFighterIndex < immortalsPopulation.size()) {
                im = immortalsPopulation.get(nextFighterIndex);

                if (im.getHealth() > 0) {
                    this.fight(im);
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // No eliminar directamente, solo marcar como muerto
                if (health <= 0) {
                    this.markAsDead();
                }
            }

        }

    }

    public void fight(Immortal i2) {
        Immortal first = this;
        Immortal second = i2;

        // Asegurar un orden consistente en la adquisición de bloqueos
        if (System.identityHashCode(first) > System.identityHashCode(second)) {
            first = i2;
            second = this;
        }

        synchronized (first) { // Bloquea el primer objeto
            synchronized (second) { // Luego bloquea el segundo
                if (second.getHealth() > 0) {
                    second.changeHealth(second.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else {
                    updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
                }
            }
        }
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public void pause() {
        synchronized (lock) {
            pause = true;
        }
    }

    private void markAsDead() {
        this.health = 0;
    }

    private void checkPause() {
        synchronized (lock) {
            while (pause) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void resumeThread() {
        synchronized (lock) {
            pause = false;
            lock.notifyAll();
        }
    }
}
