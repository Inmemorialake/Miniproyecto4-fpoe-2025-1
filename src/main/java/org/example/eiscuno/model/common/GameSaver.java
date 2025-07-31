package org.example.eiscuno.model.common;

import java.io.*;

public class GameSaver {

    private static final File SAVE_FILE = new File(PlayerStatsManager.getAppDataFolder(), "savegame.dat");

    public static void save(GameHandler handler) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameHandler load() {
        if (!SAVE_FILE.exists()) return null;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            return (GameHandler) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteSaveFile() {
        if (SAVE_FILE.exists()) {
            SAVE_FILE.delete();
        }
    }

    public static boolean saveExists() {
        return SAVE_FILE.exists();
    }
}
