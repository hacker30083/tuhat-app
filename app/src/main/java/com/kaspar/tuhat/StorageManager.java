package com.kaspar.tuhat;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorageManager {
    private static final String FILE_NAME = "games.json";
    private final Gson gson = new Gson();
    private final Context context;

    public StorageManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<GameData> loadGames() {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) return new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<GameData>>() {}.getType();
            List<GameData> games = gson.fromJson(reader, listType);
            if (games == null) return new ArrayList<>();
            
            // Filter out any null entries that might have occurred due to corrupted JSON
            List<GameData> validGames = new ArrayList<>();
            for (GameData g : games) {
                if (g != null && g.getId() != null) {
                    validGames.add(g);
                }
            }
            
            Collections.sort(validGames, (g1, g2) -> Long.compare(g2.getLastModified(), g1.getLastModified()));
            return validGames;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveGame(GameData game) {
        List<GameData> games = loadGames();
        boolean found = false;
        game.setLastModified(System.currentTimeMillis());

        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getId().equals(game.getId())) {
                games.set(i, game);
                found = true;
                break;
            }
        }

        if (!found) {
            games.add(0, game);
        }

        saveAllGames(games);
    }

    public void deleteGame(String id) {
        List<GameData> games = loadGames();
        games.removeIf(g -> g.getId().equals(id));
        saveAllGames(games);
    }

    private void saveAllGames(List<GameData> games) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(games, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
