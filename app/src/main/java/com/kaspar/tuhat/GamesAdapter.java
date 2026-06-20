package com.kaspar.tuhat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.ViewHolder> {

    public interface OnGameClickListener {
        void onGameClick(GameData game);
        void onRenameClick(GameData game);
        void onDeleteClick(GameData game);
    }

    private final List<GameData> games;
    private final OnGameClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public GamesAdapter(List<GameData> games, OnGameClickListener listener) {
        this.games = games;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameData game = games.get(position);
        holder.textTitle.setText(game.getTitle());
        holder.textDate.setText(dateFormat.format(new Date(game.getLastModified())));

        holder.itemView.setOnClickListener(v -> listener.onGameClick(game));
        
        holder.buttonMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Rename");
            popup.getMenu().add("Delete");
            
            popup.setOnMenuItemClickListener(item -> {
                if ("Rename".equals(item.getTitle())) {
                    listener.onRenameClick(game);
                    return true;
                } else if ("Delete".equals(item.getTitle())) {
                    listener.onDeleteClick(game);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView textDate;
        ImageButton buttonMenu;

        ViewHolder(View view) {
            super(view);
            textTitle = view.findViewById(R.id.text_game_title);
            textDate = view.findViewById(R.id.text_game_date);
            buttonMenu = view.findViewById(R.id.button_game_menu);
        }
    }
}
