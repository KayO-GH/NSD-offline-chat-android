package com.finalyear.networkservicediscovery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.finalyear.networkservicediscovery.pojos.ChatMessage;
import com.finalyear.networkservicediscovery.R;

import java.util.ArrayList;

/**
 * Created by KayO on 23/11/2016.
 */
public class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {
    private TextView tvChatText;
    private ArrayList<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
    private Context context;
    //make provision to show files
    private LinearLayout llFileView;
    private TextView tvFileName;
    private ImageView ivFilePic;

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
        if (chatMessage.isReceived()) {
            //message appears on the left
            chatRow = inflater.inflate(R.layout.left, parent, false);
        } else {
            chatRow = inflater.inflate(R.layout.right, parent, false);
        }
        tvChatText = (TextView) chatRow.findViewById(R.id.tvMessage);
        llFileView = (LinearLayout) chatRow.findViewById(R.id.llFileView);
        tvFileName = (TextView) chatRow.findViewById(R.id.tvFileName);
        ivFilePic = (ImageView) chatRow.findViewById(R.id.ivFilePic);
        // TODO: 18/04/2017 Decide which incoming messages should be seen and which should not 
        if (chatMessage.getMessageContent().contains("##")) {
            //incoming instruction... do not show on screen
            tvChatText.setVisibility(View.GONE);

            if (chatMessage.getMessageContent().contains("##port")) {
                //incoming file
                llFileView.setVisibility(View.VISIBLE);
                if ((isAudio(chatMessage.getMessageContent().substring(chatMessage.getMessageContent().lastIndexOf('/') + 1)))) {
                    ivFilePic.setImageResource(R.drawable.wi_files_music);
                } else if (isVideo(chatMessage.getMessageContent().substring(chatMessage.getMessageContent().lastIndexOf('/') + 1))) {
                    ivFilePic.setImageResource(R.drawable.wi_files_video);
                } else if (isImage(chatMessage.getMessageContent().substring(chatMessage.getMessageContent().lastIndexOf('/') + 1))) {
                    ivFilePic.setImageResource(R.drawable.wi_files_image);
                } else
                    ivFilePic.setImageResource(R.drawable.wi_files_logo);
                tvFileName.setText(chatMessage.getMessageContent().substring(chatMessage.getMessageContent().lastIndexOf('/') + 1));
            }
        }

        tvChatText.setText(chatMessage.getMessageContent());
        return chatRow;
    }

    private boolean isAudio(String fileName) {
        return fileName.endsWith(".mp3") || fileName.endsWith(".aac") || fileName.endsWith(".m4a") || fileName.endsWith(".amr");
    }

    private boolean isVideo(String fileName) {
        return fileName.endsWith(".mp4") || fileName.endsWith(".3gp") || fileName.endsWith(".mkv") || fileName.endsWith(".webm") || fileName.endsWith(".avi");
    }

    private boolean isImage(String fileName) {
        return fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif") || fileName.endsWith(".JPG") || fileName.endsWith(".PNG") || fileName.endsWith(".GIF") || fileName.endsWith(".jpeg");
    }
}
