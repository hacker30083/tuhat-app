package com.kaspar.tuhat;

import org.junit.Assert;
import org.junit.Test;

public class PlayerTest {

    @Test
    public void updateScore() {
        Player player = new Player("Martin");
        int scoreOne = 125;
        player.updateScore(scoreOne);
        int scoreTwo = 25;
        player.updateScore(scoreTwo);
        Assert.assertEquals(150, player.getTotalScore());
    }

    @Test
    public void updateScoreShouldLimitTo880WhenInitialUnder880() {
        Player player = new Player("Martin");
        int scoreOne = 879;
        player.updateScore(scoreOne);
        int scoreTwo = 10;
        player.updateScore(scoreTwo);
        Assert.assertEquals(880, player.getTotalScore());
    }

    @Test
    public void updateScoreShouldLimitTo880WhenInitialExactly880AndSumLessThan1000() {
        Player player = new Player("Martin");
        int scoreOne = 880;
        player.updateScore(scoreOne);
        int scoreTwo = 110;
        player.updateScore(scoreTwo);
        Assert.assertEquals(880, player.getTotalScore());
    }

    @Test
    public void updateScoreShouldLimitTo1000WhenInitialExactly880AndSum1000OrMore() {
        Player player = new Player("Martin");
        int scoreOne = 880;
        player.updateScore(scoreOne);
        int scoreTwo = 125;
        player.updateScore(scoreTwo);
        Assert.assertEquals(1000, player.getTotalScore());
    }

    @Test
    public void updateScoreShouldLimitTo1000WhenInitialExactly880AndSumLess() {
        Player player = new Player("Martin");
        int scoreOne = 880;
        player.updateScore(scoreOne);
        int scoreTwo = -120;
        player.updateScore(scoreTwo);
        Assert.assertEquals(760, player.getTotalScore());
    }

    @Test
    public void getScoreAsString() {
        Player player = new Player("Martin");
        player.updateScore(125);
        Assert.assertEquals("125", player.getScoreAsString());
    }
}
