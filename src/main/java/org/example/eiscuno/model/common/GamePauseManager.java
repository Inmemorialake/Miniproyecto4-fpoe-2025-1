package org.example.eiscuno.model.common;

// Imports
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * GamePauseManager is a singleton class that manages the pause state of the game.
 * It allows the game to be paused and resumed, and provides a mechanism for threads
 * to wait until the game is unpaused.
 */
public class GamePauseManager {
    private static final GamePauseManager INSTANCE = new GamePauseManager();

    // Lock to manage access to the pause state
    // and to coordinate between threads waiting for the game to be unpaused.
    // Using ReentrantLock for better control over locking and unlocking.
    // Condition to signal when the game is unpaused.
    // Using volatile for the paused state to ensure visibility across threads.
    // This ensures that changes to the paused state are visible to all threads.
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition unpaused = lock.newCondition();
    private volatile boolean paused = false;

    // Private constructor to prevent instantiation
    private GamePauseManager() {}

    /**
     * Returns the singleton instance of GamePauseManager.
     *
     * @return the singleton instance
     */
    public static GamePauseManager getInstance() {
        return INSTANCE;
    }

    /**
     * Pauses the game.
     * This method sets the paused state to true and locks the game.
     */
    public void pauseGame() {
        lock.lock();
        try {
            paused = true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Resumes the game.
     * This method sets the paused state to false and signals all waiting threads.
     */
    public void resumeGame() {
        lock.lock();
        try {
            paused = false;
            unpaused.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /*
    * Waits until the game is unpaused.
    * This method will block the current thread until the game is resumed.
    * It uses a condition variable to wait for the unpaused signal.
    * If the game is currently paused, the thread will wait until it is resumed.
    * If the thread is interrupted while waiting, it will restore the interrupt status.
    * This is important to ensure that the thread can handle interruptions properly.
    */
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
