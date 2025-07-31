package org.example.eiscuno.model.common;

// Imports
import java.io.*;

/**
 * PlayerStatsManager is responsible for managing player statistics such as
 * the number of games played, won, and cards placed.
 * It reads from and writes to a CSV file located in the user's application data folder.
 */
public class PlayerStatsManager {
    // Constants for file management
    private static final String APP_FOLDER_NAME = "EISCUno";
    private static final String FILE_NAME = "player_stats.csv";

    /**
     * Updates the player statistics based on the game outcome and number of cards placed.
     *
     * @param won               Indicates if the player won the game.
     * @param cardsToAument     The number of cards to add to the total placed cards.
     * @param justUpdatePutCards If true, only updates the number of cards placed without changing game counts.
     */
    public static void updateStats(boolean won, int cardsToAument, boolean justUpdatePutCards) {
        File dataFolder = getAppDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs(); // crea carpeta si no existe
        }

        File file = new File(dataFolder, FILE_NAME);

        int playedGames = 0;
        int gamesWon = 0;
        int PlacedCards = 0;

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.readLine(); // encabezado
                String data = reader.readLine();
                if (data != null) {
                    String[] parts = data.split(",");
                    playedGames = Integer.parseInt(parts[0]);
                    gamesWon = Integer.parseInt(parts[1]);
                    PlacedCards = Integer.parseInt(parts[2]);
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if(!justUpdatePutCards){
            playedGames++;
            if (won) gamesWon++;
        }

        PlacedCards += cardsToAument;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("partidas_jugadas,partidas_ganadas,cartas_colocadas");
            writer.printf("%d,%d,%d\n", playedGames, gamesWon, PlacedCards);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the application data folder based on the operating system.
     *
     * @return The File object representing the application data folder.
     */
    public static File getAppDataFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            return new File(System.getenv("APPDATA") + File.separator + APP_FOLDER_NAME);
        } else if (os.contains("mac")) {
            return new File(userHome + "/Library/Application Support/" + APP_FOLDER_NAME);
        } else {
            return new File(userHome + "/.config/" + APP_FOLDER_NAME);
        }
    }
}
