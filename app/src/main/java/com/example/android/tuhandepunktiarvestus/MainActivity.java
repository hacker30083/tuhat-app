package com.example.android.tuhandepunktiarvestus;

import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {

    final Context context = this;
    private TextView playerOneScore;
    private TextView playerTwoScore;
    private TextView playerThreeScore;


    private Player playerOne = new Player("Margit");
    private Player playerTwo = new Player("Andres");
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

        // Set player names on main view
        TextView playerOneName = findViewById(R.id.player_1_name);
        playerOneName.setText(playerOne.getName());
        TextView playerTwoName = findViewById(R.id.player_2_name);
        playerTwoName.setText(playerTwo.getName());
        TextView playerThreeName = findViewById(R.id.player_3_name);
        playerThreeName.setText(playerThree.getName());

        playerOneScore = findViewById(R.id.playerOneScore);
        playerTwoScore = findViewById(R.id.playerTwoScore);
        playerThreeScore = findViewById(R.id.playerThreeScore);

        final TableLayout historyTable = findViewById(R.id.historyTable);

        // add button listener
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                final View promptsView = li.inflate(R.layout.prompts, null);

                // Set player names on prompt view
                TextView playerOneNamePrompt = promptsView.findViewById(R.id.labelPlayerOne);
                playerOneNamePrompt.setText(playerOne.getName());
                TextView playerTwoNamePrompt = promptsView.findViewById(R.id.labelPlayerTwo);
                playerTwoNamePrompt.setText(playerTwo.getName());
                TextView playerThreeNamePrompt = promptsView.findViewById(R.id.labelPlayerThree);
                playerThreeNamePrompt.setText(playerThree.getName());

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
                                    updateWarnings();

                                    alertDialog.dismiss();
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
                nameCell.setBackgroundColor(Color.YELLOW);
            } else {
                nameCell.setBackgroundColor(Color.WHITE);
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
                userInput.setBackgroundColor(Color.YELLOW);
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
        playerOneScore.setText(playerOne.getScoreAsString());
        playerTwoScore.setText(playerTwo.getScoreAsString());
        playerThreeScore.setText(playerThree.getScoreAsString());
        return true;
    }

    private int getRoundScore(EditText userInput) {
        String userInputString = userInput.getText().toString();
        return userInputString.isEmpty() || "-".equals(userInputString) ? 0 : Integer.parseInt(userInputString);
    }
}
