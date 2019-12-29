package com.example.android.tuhandepunktiarvestus;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final Context context = this;
    private TextView playerOneScore;
    private TextView playerTwoScore;
    private TextView playerThreeScore;


    private Player playerOne = new Player("Andres");
    private Player playerTwo = new Player("Margit");
    private Player playerThree = new Player("Martin");

    private ArrayList<Round> rounds = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // add players
        players.add(playerOne);
        players.add(playerTwo);
        players.add(playerThree);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // components from main.xml
        Button button = findViewById(R.id.buttonPrompt);

        playerOneScore = findViewById(R.id.playerOneScore);
        playerTwoScore = findViewById(R.id.playerTwoScore);
        playerThreeScore = findViewById(R.id.playerThreeScore);


        // add button listener
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                final View promptsView = li.inflate(R.layout.prompts, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alert dialog builder
                alertDialogBuilder.setView(promptsView);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // get user input and set it to result
                                        updateScores(promptsView);
                                        updatePreviousRoundTable();
                                        updateWarnings();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });
    }

    private void updateWarnings() {
        int i = 1;
        for (Player player : players) {
            String identifierString = "player_" + i + "_name";
            int identifier = getResources().getIdentifier(identifierString, "id", getPackageName());
            TextView nameCell = findViewById(identifier);
            if (player.getZeroPointRounds() == 2) {
                nameCell.setBackgroundColor(Color.YELLOW);
            } else {
                nameCell.setBackgroundColor(Color.WHITE);
            }
            i++;
        }

    }

    private void updatePreviousRoundTable() {
        int row = 1;

        int roundsSize = rounds.size();
        for (int i = roundsSize - 5; roundsSize > i; i++) {
            for (int j = 1; j <= players.size(); j++) {
                if (i >= 0) {
                    String playerRowRoundScoreString = "player_" + j + "_round_score_" + row;
                    String playerRowTotalScoreAfterRoundString = "player_" + j + "_total_after_round_" + row;

                    int playerRowRoundScore = getResources().getIdentifier(playerRowRoundScoreString, "id", getPackageName());
                    int playerRowTotalScoreAfterRound = getResources().getIdentifier(playerRowTotalScoreAfterRoundString, "id", getPackageName());

                    TextView playerRowRoundScoreTableCell = findViewById(playerRowRoundScore);
                    TextView playerRowTotalScoreAfterRoundTableCell = findViewById(playerRowTotalScoreAfterRound);

                    int roundScore = rounds.get(i).getRoundScore(j - 1);

                    String roundScoreAsString = rounds.get(i).getRoundScoreAsString(j - 1);

                    if (roundScore < 0) {
                        playerRowRoundScoreTableCell.setText(roundScoreAsString);
                        playerRowRoundScoreTableCell.setTextColor(Color.RED);
                    } else if (roundScore > 0) {
                        playerRowRoundScoreTableCell.setText(String.format("+%s", roundScoreAsString));
                        playerRowRoundScoreTableCell.setTextColor(0xFF00A800);
                    } else {
                        playerRowRoundScoreTableCell.setText(roundScoreAsString);
                    }

                    playerRowTotalScoreAfterRoundTableCell.setText(rounds.get(i).getTotalScoreAfterRound(j - 1));
                }
            }
            row++;
        }

    }

    private void updateScores(View promptsView) {
        int[] playerScores = new int[]{R.id.editTextDialogUserInputPlayerOne, R.id.editTextDialogUserInputPlayerTwo, R.id.editTextDialogUserInputPlayerThree};

        Round round = new Round();
        for (int i = 0; i < playerScores.length; i++) {
            EditText userInput = promptsView
                    .findViewById(playerScores[i]);
            int playerScore = getRoundScore(userInput);
            players.get(i).updateScore(playerScore);
            round.setRoundScore(i, playerScore);
            round.setTotalScoreAfterRound(i, players.get(i).getTotalScore());

        }
        rounds.add(round);

        // Update scores in app
        playerOneScore.setText(playerOne.getScoreAsString());
        playerTwoScore.setText(playerTwo.getScoreAsString());
        playerThreeScore.setText(playerThree.getScoreAsString());
    }

    private int getRoundScore(EditText userInput) {
        String userInputString = userInput.getText().toString();
        return userInputString.isEmpty() || "-".equals(userInputString) ? 0 : Integer.parseInt(userInputString);
    }
}
