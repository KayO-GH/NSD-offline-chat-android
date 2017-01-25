package com.finalyear.networkservicediscovery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.finalyear.networkservicediscovery.R;
import com.finalyear.networkservicediscovery.pojos.Contact;
import com.finalyear.networkservicediscovery.utils.ImageConversionUtil;

import java.util.ArrayList;

/**
 * Created by KayO on 28/12/2016.
 */
public class DiscoveryListAdapter extends BaseAdapter {
    private Context ctx;
    private ArrayList<Contact> allContacts;
    private TextView tvContactName, tvLastMessage, tvOnlineStatus;
    private ImageView ivContactImage;

    public DiscoveryListAdapter(Context ctx, ArrayList<Contact> allContacts) {
        this.ctx = ctx;
        this.allContacts = allContacts;
    }

    @Override
    public int getCount() {
        return allContacts.size();
    }

    @Override
    public Object getItem(int i) {
        return allContacts.get(i);
    }

    //we'll work on using phone numbers for id's later
    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Contact contact = allContacts.get(i);

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(ctx.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.discovery_list_row, null);

        tvContactName = (TextView) view.findViewById(R.id.tvContactName);
        tvLastMessage = (TextView) view.findViewById(R.id.tvLastMessage);
        tvOnlineStatus = (TextView) view.findViewById(R.id.tvOnlineStatus);
        ivContactImage = (ImageView) view.findViewById(R.id.ivContactImage);

        //set image
        if(contact.getImage() != null)
            ivContactImage.setImageBitmap(ImageConversionUtil.convertByteArrayToPhoto(contact.getImage()));
        //set name
            tvContactName.setText(contact.getName());
        //set last message
            tvLastMessage.setText(contact.getLastMessage());
        //set online status display
        if(contact.isOnline())
            tvOnlineStatus.setText("Online");
        else
            tvOnlineStatus.setText("Offline");


        return view;
    }
}
