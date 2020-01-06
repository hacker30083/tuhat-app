package com.example.android.tuhandepunktiarvestus;

import java.util.ArrayList;

public class Player {
    private String name;
    private int totalScore;
    private ArrayList<Integer> playerRoundScores = new ArrayList<Integer>();
    private int zeroPointRounds = 0;

    Player(String name) {
        this.name = name;
        this.totalScore = 0;
    }

    void updateScore(int roundScore) {

        int score = totalScore + roundScore;

        if (score > 880 && totalScore < 880) {
            score = 880;
        } else if (totalScore == 880) {
            if (score < 1000 && score > 880) {
                score = 880;
            } else if (score >= 1000) {
                score = 1000;
            }
        }

        this.totalScore = score;
        playerRoundScores.add(roundScore);

        if (roundScore == 0) {
            zeroPointRounds++;
        } else {
            zeroPointRounds = 0;
        }
    }

    String getScoreAsString() {
        return Integer.toString(totalScore);
    }

    int getZeroPointRounds() {
        return zeroPointRounds;
    }

    String getName() {
        return name;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getLastRoundScore() {
        return playerRoundScores.get(playerRoundScores.size() - 1);
    }

    public String getLastRoundScoreAsString() {
        if(getLastRoundScore() == 0) {
            return "-";
        } else {
            return Integer.toString(playerRoundScores.get(playerRoundScores.size() - 1));
        }
    }
}
