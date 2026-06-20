package com.kaspar.tuhat;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Player> players = new ArrayList<>();
    private List<Round> rounds = new ArrayList<>();
    
    private LinearLayout namesContainer;
    private LinearLayout scoresContainer;
    private TableLayout historyTable;
    private NestedScrollView historyScrollView;
    
    private final List<TextView> playerScoresUI = new ArrayList<>();
    private final List<TextView> playerNamesUI = new ArrayList<>();

    private int dealerIndex = 0;
    private StorageManager storageManager;
    private GameData currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageManager = new StorageManager(this);

        namesContainer = findViewById(R.id.names_container);
        scoresContainer = findViewById(R.id.scores_container);
        historyTable = findViewById(R.id.historyTable);
        historyScrollView = findViewById(R.id.historyTableScrollable);
        View btnAddRound = findViewById(R.id.buttonPrompt);

        btnAddRound.setOnClickListener(v -> showAddRoundDialog());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentGame != null) {
                    showLoadGameDialog();
                    currentGame = null;
                    // Clear UI
                    namesContainer.removeAllViews();
                    scoresContainer.removeAllViews();
                    historyTable.removeAllViews();
                } else {
                    finish();
                }
            }
        });

        showLoadGameDialog();
    }

    private void showLoadGameDialog() {
        Dialog loadDialog = new Dialog(this, R.style.FullScreenDialog);
        View loadView = LayoutInflater.from(this).inflate(R.layout.dialog_load_game, null);
        
        // Ensure the root view has MATCH_PARENT layout params so it fills the dialog window
        loadView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.MATCH_PARENT));
        
        loadDialog.setContentView(loadView);
        loadDialog.setCancelable(true);
        loadDialog.setOnCancelListener(dialog -> {
            if (currentGame == null) finish();
        });

        RecyclerView recyclerView = loadView.findViewById(R.id.recycler_games);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        List<GameData> games = storageManager.loadGames();
        GamesAdapter adapter = new GamesAdapter(games, new GamesAdapter.OnGameClickListener() {
            @Override
            public void onGameClick(GameData game) {
                loadGame(game);
                loadDialog.dismiss();
            }

            @Override
            public void onRenameClick(GameData game) {
                TextInputLayout til = new TextInputLayout(MainActivity.this);
                til.setPadding(32, 16, 32, 0);
                TextInputEditText et = new TextInputEditText(MainActivity.this);
                et.setText(game.getTitle());
                et.setSingleLine(true);
                til.addView(et);

                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Rename Game")
                        .setView(til)
                        .setPositiveButton("Rename", (dialog, which) -> {
                            if (et.getText() != null) {
                                String newName = et.getText().toString().trim();
                                if (!newName.isEmpty()) {
                                    game.setCustomTitle(newName);
                                    storageManager.saveGame(game);
                                    recyclerView.getAdapter().notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onDeleteClick(GameData game) {
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Delete Game")
                        .setMessage("Are you sure you want to delete this game?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            storageManager.deleteGame(game.getId());
                            games.remove(game);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        loadView.findViewById(R.id.btn_new_game).setOnClickListener(v -> {
            loadDialog.dismiss();
            showSetupDialog();
        });

        loadView.findViewById(R.id.buttonInfo).setOnClickListener(v -> showInfoDialog());

        loadDialog.show();
    }

    private void loadGame(GameData game) {
        currentGame = game;
        players = game.getPlayers();
        rounds = game.getRounds();
        dealerIndex = game.getDealerIndex();

        // Reconstruct player state from history
        for (Player p : players) {
            p.reset();
        }
        for (Round r : rounds) {
            for (int i = 0; i < players.size(); i++) {
                int scoreInRound = r.getRoundScore(i);
                // We use a simplified update here because penalties (-100) 
                // are already stored AS the round score.
                // If we used p.updateScore(), it would apply the penalty twice.
                // Instead, we just need to reconstruct the strike counter.
                players.get(i).updateScoreFromHistory(scoreInRound);
            }
        }

        initGameUI();
        // Update the big score numbers
        for (int i = 0; i < players.size(); i++) {
            playerScoresUI.get(i).setText(players.get(i).getScoreAsString());
        }

        // Redraw full history
        historyTable.removeAllViews();
        for (int i = 0; i < rounds.size(); i++) {
            drawTableForRound(i);
        }
        updateWarnings();
    }

    private void drawTableForRound(int roundIndex) {
        if (roundIndex > 0 && historyTable.getChildCount() > 0) {
            View divider = new View(this);
            divider.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.outline));
            divider.setAlpha(0.2f);
            historyTable.addView(divider);
        }

        Round round = rounds.get(roundIndex);
        TableRow row = new TableRow(this);
        row.setPadding(0, 8, 0, 8);

        for (int i = 0; i < players.size(); i++) {
            RelativeLayout cell = new RelativeLayout(this);
            cell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));

            TextView totalTv = new TextView(this);
            totalTv.setId(View.generateViewId());
            totalTv.setText(round.getTotalScoreAfterRound(i));
            totalTv.setTextSize(18);
            totalTv.setTypeface(null, Typeface.BOLD);
            totalTv.setGravity(Gravity.CENTER);

            RelativeLayout.LayoutParams totalParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            totalParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            totalTv.setLayoutParams(totalParams);

            TextView roundTv = new TextView(this);
            int score = round.getRoundScore(i);
            String scoreStr = (score == 0) ? "-" : (score > 0 ? "+" + score : String.valueOf(score));
            roundTv.setText(scoreStr);
            roundTv.setTextSize(10);
            if (score > 0) roundTv.setTextColor(ContextCompat.getColor(this, R.color.success));
            else if (score < 0) roundTv.setTextColor(ContextCompat.getColor(this, R.color.error));

            RelativeLayout.LayoutParams roundParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            roundParams.addRule(RelativeLayout.ALIGN_TOP, totalTv.getId());
            roundParams.addRule(RelativeLayout.END_OF, totalTv.getId());
            roundParams.setMarginStart(4);
            roundTv.setLayoutParams(roundParams);

            cell.addView(totalTv);
            cell.addView(roundTv);
            row.addView(cell);
        }
        historyTable.addView(row);
        historyScrollView.post(() -> historyScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void showSetupDialog() {
        Dialog setupDialog = new Dialog(this, R.style.FullScreenDialog);
        View setupView = LayoutInflater.from(this).inflate(R.layout.dialog_setup, null);
        
        // Ensure the root view has MATCH_PARENT layout params so it fills the dialog window
        setupView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.MATCH_PARENT));
        
        setupDialog.setContentView(setupView);
        setupDialog.setCancelable(true);
        setupDialog.setOnCancelListener(dialog -> showLoadGameDialog());

        MaterialButtonToggleGroup toggleGroup = setupView.findViewById(R.id.player_count_toggle_group);
        LinearLayout namesInputContainer = setupView.findViewById(R.id.names_input_container);
        Button btnStart = setupView.findViewById(R.id.btn_start_game);
        View btnInfo = setupView.findViewById(R.id.buttonInfo);
        View btnBack = setupView.findViewById(R.id.btn_back);

        btnInfo.setOnClickListener(v -> showInfoDialog());
        btnBack.setOnClickListener(v -> {
            setupDialog.dismiss();
            showLoadGameDialog();
        });

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int count = 3;
                if (checkedId == R.id.btn_2_players) count = 2;
                else if (checkedId == R.id.btn_4_players) count = 4;
                updateSetupNamesInput(namesInputContainer, count);
            }
        });

        updateSetupNamesInput(namesInputContainer, 3); // Default 3

            btnStart.setOnClickListener(v -> {
                players.clear();
                for (int i = 0; i < namesInputContainer.getChildCount(); i++) {
                    View child = namesInputContainer.getChildAt(i);
                    EditText et = child instanceof TextInputLayout ? ((TextInputLayout) child).getEditText() : (EditText) child;
                    String name = et != null ? et.getText().toString().trim() : "";
                    if (name.isEmpty()) name = "Player " + (i + 1);
                    players.add(new Player(name));
                }
                rounds.clear();
                dealerIndex = 0;
                currentGame = new GameData(players, rounds, dealerIndex);
                storageManager.saveGame(currentGame);
                
                setupDialog.dismiss();
                initGameUI();
                historyTable.removeAllViews();
            });

        setupDialog.show();
    }

    private void updateSetupNamesInput(LinearLayout container, int count) {
        container.removeAllViews();
        for (int i = 0; i < count; i++) {
            TextInputLayout til = new TextInputLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            til.setLayoutParams(lp);
            til.setHint("Player " + (i + 1) + " Name");

            TextInputEditText et = new TextInputEditText(this);
            et.setSingleLine(true);
            et.setImeOptions(i == count - 1 ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
            til.addView(et);
            container.addView(til);
        }
    }

    private void initGameUI() {
        namesContainer.removeAllViews();
        scoresContainer.removeAllViews();
        playerNamesUI.clear();
        playerScoresUI.clear();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int index = i;

            TextView tvName = new TextView(this);
            tvName.setLayoutParams(params);
            tvName.setGravity(Gravity.CENTER);
            tvName.setText(player.getName());
            tvName.setTextSize(18);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setPadding(8, 24, 8, 8);
            tvName.setOnClickListener(v -> showEditNameDialog(index));
            namesContainer.addView(tvName);
            playerNamesUI.add(tvName);

            TextView tvScore = new TextView(this);
            tvScore.setLayoutParams(params);
            tvScore.setGravity(Gravity.CENTER);
            tvScore.setText("0");
            tvScore.setTextSize(36);
            tvScore.setTextColor(ContextCompat.getColor(this, R.color.primary));
            scoresContainer.addView(tvScore);
            playerScoresUI.add(tvScore);
        }
        updateDealerHighlight();
    }

    private void showEditNameDialog(int index) {
        TextInputLayout til = new TextInputLayout(this);
        til.setPadding(32, 16, 32, 0);
        TextInputEditText et = new TextInputEditText(this);
        et.setText(players.get(index).getName());
        et.setSingleLine(true);
        et.setImeOptions(EditorInfo.IME_ACTION_DONE);
        til.addView(et);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Name")
                .setView(til)
                .setPositiveButton("Save", (dialog, which) -> {
                    if (et.getText() != null) {
                        String newName = et.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            players.get(index).setName(newName);
                            playerNamesUI.get(index).setText(newName);
                            if (currentGame != null) {
                                storageManager.saveGame(currentGame);
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddRoundDialog() {
        View promptsView = LayoutInflater.from(this).inflate(R.layout.prompts, null);
        LinearLayout container = promptsView.findViewById(R.id.prompts_container);
        List<EditText> inputs = new ArrayList<>();
        List<Integer> activePlayerIndices = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.isFinished()) continue;

            activePlayerIndices.add(i);
            TextInputLayout til = new TextInputLayout(this);
            
            String hint = p.getName();
            if (p.getTotalScore() == 880) {
                hint += " (120 to win)";
                til.setHelperText("Must be 120 or 0");
                til.setHelperTextColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))); // Material Red
            } else if (p.getZeroPointRounds() == 2) {
                hint += " (-100?)";
                til.setHelperText("Penalty if zero!");
                til.setHelperTextColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#FBC02D"))); // Material Yellow
            }
            til.setHint(hint);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            til.setLayoutParams(lp);

            TextInputEditText et = new TextInputEditText(this);
            et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
            et.setSingleLine(true);
            
            if (p.getZeroPointRounds() == 2) {
                et.setTextColor(Color.parseColor("#FBC02D"));
            }

            til.addView(et);
            container.addView(til);
            inputs.add(et);
        }

        // Set IME options for the last active input
        if (!inputs.isEmpty()) {
            for (int i = 0; i < inputs.size(); i++) {
                inputs.get(i).setImeOptions(i == inputs.size() - 1 ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
            }
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Add Round Scores")
                .setView(promptsView)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(v -> {
                if (validateAndProcessRound(inputs, activePlayerIndices)) {
                    dialog.dismiss();
                    drawTable();
                    updateDealerIndex();
                    updateDealerHighlight();
                    updateWarnings();
                }
            });
        });
        dialog.show();
    }

    private boolean validateAndProcessRound(List<EditText> inputs, List<Integer> activePlayerIndices) {
        int[] roundScores = new int[players.size()];
        for (int i = 0; i < inputs.size(); i++) {
            int playerIndex = activePlayerIndices.get(i);
            String val = inputs.get(i).getText().toString().trim();
            if (val.isEmpty() || "-".equals(val) || "+".equals(val)) {
                roundScores[playerIndex] = 0;
            } else {
                try {
                    int rawScore = Integer.parseInt(val);
                    int rounded = (int) (Math.round(rawScore / 5.0) * 5);
                    
                    if (players.get(playerIndex).getTotalScore() == 880) {
                        if (rounded != 120 && rounded != 0) {
                            View current = inputs.get(i);
                            while (current.getParent() instanceof View) {
                                if (current.getParent() instanceof TextInputLayout) {
                                    ((TextInputLayout) current.getParent()).setError("Must be 120 or 0");
                                    break;
                                }
                                current = (View) current.getParent();
                            }
                            return false;
                        }
                    }
                    
                    roundScores[playerIndex] = rounded;
                } catch (NumberFormatException e) {
                    View current = inputs.get(i);
                    while (current.getParent() instanceof View) {
                        if (current.getParent() instanceof TextInputLayout) {
                            ((TextInputLayout) current.getParent()).setError("Invalid number");
                            break;
                        }
                        current = (View) current.getParent();
                    }
                    return false;
                }
            }
        }

        Round round = new Round(players.size());
        List<String> winners = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.isFinished()) {
                round.setRoundScore(i, 0);
                round.setTotalScoreAfterRound(i, p.getTotalScore());
                continue;
            }

            int actualScoreChange = p.updateScore(roundScores[i]);
            round.setRoundScore(i, actualScoreChange);
            round.setTotalScoreAfterRound(i, p.getTotalScore());
            playerScoresUI.get(i).setText(p.getScoreAsString());

            if (p.isFinished()) {
                winners.add(p.getName());
            }
        }
        rounds.add(round);
        
        if (currentGame != null) {
            currentGame.setDealerIndex((dealerIndex + 1) % players.size());
            storageManager.saveGame(currentGame);
        }

        if (!winners.isEmpty()) {
            showWinDialog(winners);
        }

        return true;
    }

    private void showWinDialog(List<String> winners) {
        String message = winners.size() == 1 ? winners.get(0) + " reached 1000!" : String.join(", ", winners) + " reached 1000 points!";
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Winner!")
                .setMessage(message + "\n\nDo you want to continue playing with the rest of the players?")
                .setPositiveButton("Continue", (dialog, which) -> {
                    // Check if anyone is left to play
                    boolean anyoneLeft = false;
                    for (Player p : players) {
                        if (!p.isFinished()) {
                            anyoneLeft = true;
                            break;
                        }
                    }
                    if (!anyoneLeft) {
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("Game Over")
                                .setMessage("All players have finished!")
                                .setPositiveButton("OK", (d, w) -> finish())
                                .show();
                    }
                })
                .setNegativeButton("End Game", (dialog, which) -> {
                    if (currentGame != null) {
                        currentGame = null;
                        namesContainer.removeAllViews();
                        scoresContainer.removeAllViews();
                        historyTable.removeAllViews();
                        showLoadGameDialog();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void updateDealerIndex() {
        int originalIndex = dealerIndex;
        do {
            dealerIndex = (dealerIndex + 1) % players.size();
        } while (players.get(dealerIndex).isFinished() && dealerIndex != originalIndex);
    }

    private void updateWarnings() {
        for (int i = 0; i < players.size(); i++) {
            TextView tv = playerNamesUI.get(i);
            TextView scoreTv = playerScoresUI.get(i);
            // Reset backgrounds first
            tv.setBackgroundColor(Color.TRANSPARENT);
            
            if (players.get(i).isFinished()) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.onSurfaceVariant));
                tv.setAlpha(0.3f);
                scoreTv.setAlpha(0.3f);
                tv.setPaintFlags(tv.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            } else if (players.get(i).getZeroPointRounds() == 2) {
                tv.setTextColor(Color.parseColor("#FBC02D")); // Yellow for warning
                tv.setAlpha(1.0f);
                scoreTv.setAlpha(1.0f);
                tv.setPaintFlags(tv.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            } else if (i == dealerIndex) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.primary));
                tv.setAlpha(1.0f);
                scoreTv.setAlpha(1.0f);
                tv.setPaintFlags(tv.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            } else {
                tv.setTextColor(ContextCompat.getColor(this, R.color.onSurfaceVariant));
                tv.setAlpha(0.6f);
                scoreTv.setAlpha(0.6f);
                tv.setPaintFlags(tv.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    private void updateDealerHighlight() {
        updateWarnings();
        for (int i = 0; i < playerNamesUI.size(); i++) {
            TextView tv = playerNamesUI.get(i);
            if (i == dealerIndex) {
                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, android.R.drawable.button_onoff_indicator_on);
            } else {
                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
    }

    private void drawTable() {
        if (!rounds.isEmpty()) {
            drawTableForRound(rounds.size() - 1);
        }
    }

    private void showInfoDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Tonn v.3")
                .setMessage("\"Tuhande\" punktiarvestus.\n\nVersion: 3.0\nDeveloped by Kaspar\n")
                .setPositiveButton("OK", null)
                .show();
    }
}
