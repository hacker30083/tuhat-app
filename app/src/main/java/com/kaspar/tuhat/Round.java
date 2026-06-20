package com.kaspar.tuhat;

import java.util.ArrayList;
import java.util.List;

public class Round {
    private List<Integer> playerRoundScores;
    private List<Integer> playerTotalScoresAfterRound;

    // No-args constructor for GSON
    public Round() {
    }

    Round(int playerCount) {
        this.playerRoundScores = new ArrayList<>(playerCount);
        this.playerTotalScoresAfterRound = new ArrayList<>(playerCount);
        for (int i = 0; i < playerCount; i++) {
            playerRoundScores.add(0);
            playerTotalScoresAfterRound.add(0);
        }
    }

    void setRoundScore(int playerIndex, int score) {
        if (playerRoundScores == null) {
            playerRoundScores = new ArrayList<>();
        }
        while (playerRoundScores.size() <= playerIndex) {
            playerRoundScores.add(0);
        }
        playerRoundScores.set(playerIndex, score);
    }

    int getRoundScore(int i) {
        if (playerRoundScores == null || i >= playerRoundScores.size()) {
            return 0;
        }
        return playerRoundScores.get(i);
    }

    void setTotalScoreAfterRound(int playerIndex, int score) {
        if (playerTotalScoresAfterRound == null) {
            playerTotalScoresAfterRound = new ArrayList<>();
        }
        while (playerTotalScoresAfterRound.size() <= playerIndex) {
            playerTotalScoresAfterRound.add(0);
        }
        playerTotalScoresAfterRound.set(playerIndex, score);
    }

    private String getScoreString(int score) {
        return Integer.toString(score);
    }

    String getTotalScoreAfterRound(int i) {
        if (playerTotalScoresAfterRound == null || i >= playerTotalScoresAfterRound.size()) {
            return "0";
        }
        return getScoreString(playerTotalScoresAfterRound.get(i));
    }
}
