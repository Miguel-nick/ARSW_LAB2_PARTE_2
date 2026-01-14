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
    private boolean paused = false;
    private boolean stopped = false;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }

    public void run() {
        while (!stopped) {
            synchronized(this) {
                while (paused && !stopped) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            if (stopped) break;

            // Verificar si este inmortal sigue vivo
            if (this.getHealth() <= 0) {
                break; // Salir del loop si está muerto
            }

            Immortal im = null;

            // Buscar un oponente vivo
            synchronized(immortalsPopulation) {
                if (immortalsPopulation.size() <= 1) {
                    break; // Solo queda uno o ninguno
                }

                int myIndex = immortalsPopulation.indexOf(this);
                if (myIndex == -1) {
                    break; // Ya no estoy en la lista
                }

                int attempts = 0;
                while (im == null && attempts < immortalsPopulation.size()) {
                    int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                    if (nextFighterIndex != myIndex) {
                        Immortal candidate = immortalsPopulation.get(nextFighterIndex);
                        if (candidate.getHealth() > 0) {
                            im = candidate;
                        }
                    }
                    attempts++;
                }
            }

            if (im != null) {
                this.fight(im);
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void fight(Immortal i2) {
        Immortal firstLock = this.hashCode() < i2.hashCode() ? this : i2;
        Immortal secondLock = this.hashCode() < i2.hashCode() ? i2 : this;

        synchronized(firstLock) {
            synchronized(secondLock) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");

                    // Eliminar inmortales muertos de la población
                    if (i2.getHealth() <= 0) {
                        immortalsPopulation.remove(i2);
                        updateCallback.processReport(i2 + " has died and been removed from simulation!\n");
                    }
                } else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                }
            }
        }
    }

    public synchronized void changeHealth(int v) {
        health = v;
    }

    public synchronized int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public void pauseImmortal() {
        paused = true;
    }

    public void resumeImmortal() {
        synchronized(this) {
            paused = false;
            notify();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void stopImmortal() {
        synchronized(this) {
            stopped = true;
            paused = false;
            notify();
        }
    }

    public boolean isStopped() {
        return stopped;
    }
}
