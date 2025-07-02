// app/src/main/java/com/example/newtrade/adapters/MessageAdapter.java
package com.example.newtrade.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "MessageAdapter";
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messages;
    private Long currentUserId;

    public MessageAdapter(Context context, List<Message> messages, Long currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder for sent messages
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);

            Log.d("MessageAdapter", "SentMessageViewHolder - tvMessage: " + tvMessage + ", tvTime: " + tvTime);
        }

        void bind(Message message) {
            if (tvMessage != null) {
                tvMessage.setText(message.getContent() != null ? message.getContent() : "");
            }
            if (tvTime != null) {
                tvTime.setText(message.getCreatedAt() != null ? message.getCreatedAt() : "");
            }
        }
    }

    // ViewHolder for received messages
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);

            Log.d("MessageAdapter", "ReceivedMessageViewHolder - tvMessage: " + tvMessage + ", tvTime: " + tvTime);
        }

        void bind(Message message) {
            if (tvMessage != null) {
                tvMessage.setText(message.getContent() != null ? message.getContent() : "");
            } else {
                Log.e("MessageAdapter", "❌ tvMessage is null in ReceivedMessageViewHolder");
            }

            if (tvTime != null) {
                tvTime.setText(message.getCreatedAt() != null ? message.getCreatedAt() : "");
            } else {
                Log.e("MessageAdapter", "❌ tvTime is null in ReceivedMessageViewHolder");
            }
        }
    }
}