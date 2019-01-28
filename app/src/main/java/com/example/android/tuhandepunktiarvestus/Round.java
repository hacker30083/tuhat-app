package com.example.android.tuhandepunktiarvestus;

class Round {
    private int[] scores = new int[3];

    Round(int scorePlayerOne, int scorePlayerTwo, int scorePlayerThree) {
        this.scores[0] = scorePlayerOne;
        this.scores[1] = scorePlayerTwo;
        this.scores[2] = scorePlayerThree;
    }

    Round() {
        this.scores[0] = 0;
        this.scores[1] = 0;
        this.scores[2] = 0;
    }

    void setScore(int player, int score) {
        scores[player] = score;
    }

    String getScore(int i) {
        return getScoreString(scores[i]);
    }

    private String getScoreString(int score) {
        return score != 0 ? Integer.toString(score) : "-";
    }
}
