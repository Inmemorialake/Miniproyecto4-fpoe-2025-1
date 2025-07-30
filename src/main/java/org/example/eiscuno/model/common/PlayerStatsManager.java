package org.example.eiscuno.model.common;

import java.io.*;

public class PlayerStatsManager {
    private static final String APP_FOLDER_NAME = "EISCUno";
    private static final String FILE_NAME = "player_stats.csv";

    public static void updateStats(boolean won, int cardsToAument, boolean justUpdatePutCards) {
        File dataFolder = getAppDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs(); // crea carpeta si no existe
        }

        File file = new File(dataFolder, FILE_NAME);

        int partidasJugadas = 0;
        int partidasGanadas = 0;
        int cartasColocadas = 0;

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.readLine(); // encabezado
                String data = reader.readLine();
                if (data != null) {
                    String[] parts = data.split(",");
                    partidasJugadas = Integer.parseInt(parts[0]);
                    partidasGanadas = Integer.parseInt(parts[1]);
                    cartasColocadas = Integer.parseInt(parts[2]);
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if(!justUpdatePutCards){
            partidasJugadas++;
            if (won) partidasGanadas++;
        }

        cartasColocadas += cardsToAument;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("partidas_jugadas,partidas_ganadas,cartas_colocadas");
            writer.printf("%d,%d,%d\n", partidasJugadas, partidasGanadas, cartasColocadas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
