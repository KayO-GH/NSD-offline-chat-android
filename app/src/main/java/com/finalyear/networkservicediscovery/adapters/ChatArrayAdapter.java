package com.finalyear.networkservicediscovery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.finalyear.networkservicediscovery.pojos.ChatMessage;
import com.finalyear.networkservicediscovery.R;

import java.util.ArrayList;

/**
 * Created by KayO on 23/11/2016.
 */
public class ChatArrayAdapter extends ArrayAdapter<ChatMessage>{
    private TextView tvChatText;
    private ArrayList<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
    private Context context;

    public ChatArrayAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
    }

    @Override
    public void add(ChatMessage object) {
        super.add(object);
        chatMessages.add(object);
    }

    @Override
    public int getCount() {
        return this.chatMessages.size();
    }

    @Override
    public ChatMessage getItem(int position) {
        return this.chatMessages.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessage = getItem(position);
        View chatRow = convertView;
        LayoutInflater inflater =
                (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(chatMessage.isReceived()){
            //message appears on the left
            chatRow = inflater.inflate(R.layout.left, parent, false);
        }else{
            chatRow = inflater.inflate(R.layout.right,parent,false);
        }
        tvChatText = (TextView) chatRow.findViewById(R.id.tvMessage);
        tvChatText.setText(chatMessage.getMessageContent());
        return chatRow;
    }
}
