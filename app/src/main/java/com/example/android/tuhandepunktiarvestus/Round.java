package com.example.android.tuhandepunktiarvestus;

class Round {
    private int[] playerRoundScore = new int[3];
    private int[] playerTotalScoreAfterRound = new int[3];

    Round() {
        this.playerRoundScore[0] = 0;
        this.playerRoundScore[1] = 0;
        this.playerRoundScore[2] = 0;

        this.playerTotalScoreAfterRound[0] = 0;
        this.playerTotalScoreAfterRound[1] = 0;
        this.playerTotalScoreAfterRound[2] = 0;
    }

    void setRoundScore(int player, int score) {
        playerRoundScore[player] = score;
    }

    int getRoundScore(int i) {
        return playerRoundScore[i];
    }

    String getRoundScoreAsString(int i) {
        return getScoreString(playerRoundScore[i]);
    }

    private String getScoreString(int score) {
        return score != 0 ? Integer.toString(score) : "-";
    }

    void setTotalScoreAfterRound(int player, int score) {
        playerTotalScoreAfterRound[player] = score;
    }

    String getTotalScoreAfterRound(int i) {
        return getScoreString(playerTotalScoreAfterRound[i]);
    }
}
