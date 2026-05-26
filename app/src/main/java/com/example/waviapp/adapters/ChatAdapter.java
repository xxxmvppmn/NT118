package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.ChatMessage;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_BOT_LOADING = 3;

    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (message.getNguoiGui().equals("user")) {
            return VIEW_TYPE_USER;
        } else if (message.getNguoiGui().equals("bot_loading")) {
            return VIEW_TYPE_BOT_LOADING;
        } else {
            return VIEW_TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        int viewType = getItemViewType(position);

        if (holder instanceof UserViewHolder) {
            String timeStr = formatTimestamp(message.getThoiGian());
            ((UserViewHolder) holder).txtMessageUser.setText(message.getNoiDung());
            ((UserViewHolder) holder).txtTimeUser.setText(timeStr);
        } else if (holder instanceof BotViewHolder) {
            if (viewType == VIEW_TYPE_BOT_LOADING) {
                ((BotViewHolder) holder).txtMessageBot.setText(message.getNoiDung());
                ((BotViewHolder) holder).txtMessageBot.setTypeface(null, android.graphics.Typeface.ITALIC);
                ((BotViewHolder) holder).txtTimeBot.setText("...");
            } else {
                String timeStr = formatTimestamp(message.getThoiGian());
                ((BotViewHolder) holder).txtMessageBot.setText(message.getNoiDung());
                ((BotViewHolder) holder).txtMessageBot.setTypeface(null, android.graphics.Typeface.NORMAL);
                ((BotViewHolder) holder).txtTimeBot.setText(timeStr);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "...";
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessageUser, txtTimeUser;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageUser = itemView.findViewById(R.id.txtMessageUser);
            txtTimeUser = itemView.findViewById(R.id.txtTimeUser);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessageBot, txtTimeBot;

        BotViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageBot = itemView.findViewById(R.id.txtMessageBot);
            txtTimeBot = itemView.findViewById(R.id.txtTimeBot);
        }
    }
}
