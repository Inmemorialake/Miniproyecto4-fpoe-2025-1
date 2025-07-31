package org.example.eiscuno.model.common;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class GamePauseManager {
    private static final GamePauseManager INSTANCE = new GamePauseManager();

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition unpaused = lock.newCondition();
    private volatile boolean paused = false;

    private GamePauseManager() {}

    public static GamePauseManager getInstance() {
        return INSTANCE;
    }

    public void pauseGame() {
        lock.lock();
        try {
            paused = true;
        } finally {
            lock.unlock();
        }
    }

    public void resumeGame() {
        lock.lock();
        try {
            paused = false;
            unpaused.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void waitIfPaused() {
        lock.lock();
        try {
            while (paused) {
                unpaused.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }
}
