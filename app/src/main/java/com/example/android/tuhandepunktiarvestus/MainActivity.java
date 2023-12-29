package com.example.android.tuhandepunktiarvestus;

import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static int SHUFFLE_ID = 1;
    final Context context = this;
    private TextView playerOneScore;
    private TextView playerTwoScore;
    private TextView playerThreeScore;


    private Player playerOne;
    private Player playerTwo;
    private Player playerThree;

    private ArrayList<Round> rounds = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ArrayList<String> playerNames = new ArrayList<>(Arrays.asList("Margit", "Martin", "Andres"));

        Collections.shuffle(playerNames);
        // add players
        for (String playerName: playerNames) {
            players.add(new Player(playerName));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // components from main.xml
        Button button = findViewById(R.id.buttonPrompt);

        // Set player names on main view
        TextView playerOneName = findViewById(R.id.player_1_name);
        playerOneName.setText(players.get(0).getName());
        playerOneName.setTypeface(playerOneName.getTypeface(), Typeface.BOLD);

        TextView playerTwoName = findViewById(R.id.player_2_name);
        playerTwoName.setText(players.get(1).getName());
        TextView playerThreeName = findViewById(R.id.player_3_name);
        playerThreeName.setText(players.get(2).getName());

        playerOneScore = findViewById(R.id.player_1_score);
        playerTwoScore = findViewById(R.id.player_2_score);
        playerThreeScore = findViewById(R.id.player_3_score);

        final TableLayout historyTable = findViewById(R.id.historyTable);

        // add button listener
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                final View promptsView = li.inflate(R.layout.prompts, null);

                // Set player names on prompt view
                TextView playerOneNamePrompt = promptsView.findViewById(R.id.player_1);
                playerOneNamePrompt.setText(players.get(0).getName());
                TextView playerTwoNamePrompt = promptsView.findViewById(R.id.player_2);
                playerTwoNamePrompt.setText(players.get(1).getName());
                TextView playerThreeNamePrompt = promptsView.findViewById(R.id.player_3);
                playerThreeNamePrompt.setText(players.get(2).getName());

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context)
                        .setView(promptsView)
                        .setCancelable(false)
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                final AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button button = (alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                if (updateScores(promptsView)) {
                                    drawTable(historyTable);
                                    updateShuffleId();
                                    updateWarnings();

                                    alertDialog.dismiss();
                                }
                            }

                            private void updateShuffleId() {
                                if (SHUFFLE_ID == 3) {
                                    SHUFFLE_ID = 1;
                                } else {
                                    SHUFFLE_ID ++;
                                }
                            }
                        });
                    }
                });

                alertDialog.show();

            }
        });
    }

    private void drawTable(TableLayout historyTable) {
        TableRow tableRow = new TableRow(MainActivity.this);

        tableRow.setLayoutParams(
                new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT)
        );

        tableRow.setWeightSum(3);

        for (Player player : players) {
            LinearLayout playerHistoryContent = new LinearLayout(tableRow.getContext());

            playerHistoryContent.setOrientation(LinearLayout.HORIZONTAL);
            playerHistoryContent.setWeightSum(2);
            playerHistoryContent.setLayoutParams(
                    new TableRow.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    )
            );


            TextView scoreAfterRound = new TextView(MainActivity.this);
            scoreAfterRound.setTextSize(20);
            scoreAfterRound.setGravity(Gravity.END);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            //params.setMargins(20, 10, 10, 10);
            scoreAfterRound.setLayoutParams(params);

            TextView roundScore = new TextView(MainActivity.this);
            roundScore.setLayoutParams(params);
            roundScore.setTextSize(12);
            roundScore.setGravity(Gravity.CENTER_HORIZONTAL);

            scoreAfterRound.setText(player.getScoreAsString());

            if (player.getLastRoundScore() < 0) {
                roundScore.setText(player.getLastRoundScoreAsString());
                roundScore.setTextColor(Color.RED);
            } else if (player.getLastRoundScore() > 0) {
                roundScore.setText(String.format("+%s", player.getLastRoundScoreAsString()));
                roundScore.setTextColor(0xFF00A800);
            } else {
                roundScore.setText(player.getLastRoundScoreAsString());
            }

            playerHistoryContent.addView(scoreAfterRound);
            playerHistoryContent.addView(roundScore);
            tableRow.addView(playerHistoryContent);
        }
        historyTable.addView(tableRow);
        ((ScrollView) findViewById(R.id.historyTableScrollable)).fullScroll(View.FOCUS_DOWN);
    }

    private void updateWarnings() {
        int i = 1;
        for (Player player : players) {
            String identifierString = "player_" + i + "_name";
            int identifier = getResources().getIdentifier(identifierString, "id", getPackageName());
            TextView nameCell = findViewById(identifier);
            if (player.getZeroPointRounds() == 2) {
                nameCell.setBackgroundColor(getResources().getColor(R.color.text_warning_background));
            } else {
                nameCell.setBackgroundColor(getResources().getColor(R.color.text_background));
            }

            if(i == SHUFFLE_ID) {
                nameCell.setTypeface(nameCell.getTypeface(), Typeface.BOLD);
            } else {
                nameCell.setTypeface(null, Typeface.NORMAL);
            }
            i++;
        }

    }


    private boolean validateInput(View promptsView, int[] playerScores) {
        for (int i = 0; i < playerScores.length; i++) {
            EditText userInput = promptsView
                    .findViewById(playerScores[i]);
            int playerScore = getRoundScore(userInput);
            if (playerScore % 5 != 0) {
                userInput.setBackgroundColor(getResources().getColor(R.color.text_warning_background));
                return false;
            }
        }
        return true;
    }

    private boolean updateScores(View promptsView) {
        int[] playerScores = new int[]{R.id.editTextDialogUserInputPlayerOne, R.id.editTextDialogUserInputPlayerTwo, R.id.editTextDialogUserInputPlayerThree};

        if(!validateInput(promptsView, playerScores)){
            return false;
        }

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
        playerOneScore.setText(players.get(0).getScoreAsString());
        playerTwoScore.setText(players.get(1).getScoreAsString());
        playerThreeScore.setText(players.get(2).getScoreAsString());
        return true;
    }

    private int getRoundScore(EditText userInput) {
        String userInputString = userInput.getText().toString();
        return userInputString.isEmpty() || "-".equals(userInputString) ? 0 : Integer.parseInt(userInputString);
    }
}
