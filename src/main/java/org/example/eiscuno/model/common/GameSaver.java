package org.example.eiscuno.model.common;

// Imports
import java.io.*;

/**
 * GameSaver is a utility class that handles saving and loading the game state.
 * It uses serialization to write the GameHandler object to a file and read it back.
 */
public class GameSaver {

    // The file where the game state will be saved.
    private static final File SAVE_FILE = new File(PlayerStatsManager.getAppDataFolder(), "savegame.dat");

    /**
     * Saves the current game state to a file.
     *
     * @param handler the GameHandler object containing the game state
     */
    public static void save(GameHandler handler) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the game state from a file.
     *
     * @return the GameHandler object containing the loaded game state, or null if loading fails
     */
    public static GameHandler load() {
        if (!SAVE_FILE.exists()) return null;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            return (GameHandler) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes the save file if it exists.
     */
    public static void deleteSaveFile() {
        if (SAVE_FILE.exists()) {
            SAVE_FILE.delete();
        }
    }

    /**
     * Checks if a save file exists.
     *
     * @return true if the save file exists, false otherwise
     */
    public static boolean saveExists() {
        return SAVE_FILE.exists();
    }
}
