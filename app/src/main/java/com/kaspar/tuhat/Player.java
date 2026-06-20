package com.kaspar.tuhat;

import java.util.ArrayList;

public class Player {
    private String name;
    private int totalScore;
    private ArrayList<Integer> playerRoundScores = new ArrayList<Integer>();
    private int zeroPointRounds = 0;
    private boolean finished = false;

    // No-args constructor for GSON
    public Player() {
    }

    Player(String name) {
        this.name = name;
        this.totalScore = 0;
    }

    void reset() {
        this.totalScore = 0;
        this.zeroPointRounds = 0;
        this.playerRoundScores.clear();
        this.finished = false;
    }

    int updateScore(int roundScore) {
        if (finished) return 0;
        int finalRoundScore = roundScore;
        
        if (roundScore == 0) {
            zeroPointRounds++;
            if (zeroPointRounds >= 3) {
                finalRoundScore = -100;
                zeroPointRounds = 0; // Reset after penalty
            }
        } else {
            zeroPointRounds = 0;
        }

        int score = totalScore + finalRoundScore;

        if (score > 880 && totalScore < 880) {
            score = 880;
        } else if (totalScore == 880) {
            if (score > 880 && score != 1000) {
                score = 880;
            } else if (score == 1000) {
                finished = true;
            }
        }

        this.totalScore = score;
        playerRoundScores.add(finalRoundScore);
        return finalRoundScore;
    }

    void updateScoreFromHistory(int scoreInRound) {
        if (finished) {
            this.playerRoundScores.add(0);
            return;
        }

        int oldScore = this.totalScore;
        int newScore = oldScore + scoreInRound;

        if (newScore > 880 && oldScore < 880) {
            newScore = 880;
        } else if (oldScore == 880) {
            if (newScore > 880 && newScore != 1000) {
                newScore = 880;
            } else if (newScore == 1000) {
                finished = true;
            }
        }

        this.totalScore = newScore;
        this.playerRoundScores.add(scoreInRound);
        // Reconstruct zero point rounds counter
        // Note: scoreInRound will be -100 if it was a penalty round
        if (scoreInRound == 0) {
            zeroPointRounds++;
        } else {
            zeroPointRounds = 0;
        }
        if (totalScore >= 1000) {
            finished = true;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    String getScoreAsString() {
        return Integer.toString(totalScore);
    }

    int getZeroPointRounds() {
        return zeroPointRounds;
    }

    void setName(String name) {
        this.name = name;
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
