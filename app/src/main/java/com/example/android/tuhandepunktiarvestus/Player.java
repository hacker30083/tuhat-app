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
        this.totalScore = totalScore + roundScore;
        playerRoundScores.add(roundScore);
        if(roundScore == 0) {
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
}
