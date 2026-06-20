package com.kaspar.tuhat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameData {
    private String id;
    private String customTitle;
    private long lastModified;
    private List<Player> players;
    private List<Round> rounds;
    private int dealerIndex;

    public GameData(List<Player> players, List<Round> rounds, int dealerIndex) {
        this.id = UUID.randomUUID().toString();
        this.lastModified = System.currentTimeMillis();
        this.players = players;
        this.rounds = rounds;
        this.dealerIndex = dealerIndex;
    }

    public String getId() { return id; }
    public String getCustomTitle() { return customTitle; }
    public void setCustomTitle(String customTitle) { this.customTitle = customTitle; }
    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    public List<Player> getPlayers() { return players; }
    public List<Round> getRounds() { return rounds; }
    public int getDealerIndex() { return dealerIndex; }
    public void setDealerIndex(int dealerIndex) { this.dealerIndex = dealerIndex; }

    public String getTitle() {
        if (customTitle != null && !customTitle.isEmpty()) {
            return customTitle;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            sb.append(players.get(i).getName());
            if (i < players.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
