package com.finalyear.networkservicediscovery.adapters;

import android.content.Context;
import android.graphics.Color;
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
import java.util.Random;

/**
 * Created by KayO on 28/12/2016.
 */
public class DiscoveryListAdapter extends BaseAdapter {
    private Context ctx;
    private ArrayList<Contact> allContacts;
    private TextView tvContactName, tvLastMessage, tvOnlineStatus, tvFirstLetter;
    private ImageView ivContactImage;

    public DiscoveryListAdapter(Context ctx, ArrayList<Contact> allContacts) {
        this.ctx = ctx;
        this.allContacts = allContacts;

        //remove redundant contacts shown here
        for (int i = 0; i < this.allContacts.size() - 1; i++) {//start i from first element
            for (int j = 1; j < this.allContacts.size(); j++) {//start j from second element
                if (this.allContacts.get(i).getName().equals(this.allContacts.get(j).getName())) {
                    //duplicate encountered, remove duplicate
                    this.allContacts.remove(j);
                    //reduce j by one so that after j++ the new object in j's position can be tested
                    --j;
                }
            }
        }
    }

    public void setAllContacts(ArrayList<Contact> allContacts) {
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
        tvFirstLetter = (TextView) view.findViewById(R.id.tvFirstLetter);


        //set name
        if (contact.getName() != null)//contact has a name
            tvContactName.setText(contact.getName());
        else//contact does not have a name set, so use phone number
            tvContactName.setText(contact.getPhoneNumber());

        //set image
        if (contact.getImage() != null)
            ivContactImage.setImageBitmap(ImageConversionUtil.convertByteArrayToPhoto(contact.getImage()));
        else{
            int[] shapeArray = {R.drawable.color_blue,R.drawable.color_gold,R.drawable.color_green,R.drawable.color_orange,R.drawable.color_red,R.drawable.color_violet};
            ivContactImage.setImageResource(shapeArray[new Random().nextInt(shapeArray.length)]);
            if (contact.getName() != null)//contact has a name
                tvFirstLetter.setText(contact.getName().charAt(0)+"");
            else//contact does not have a name set, so use phone number
                tvFirstLetter.setText(contact.getPhoneNumber().charAt(0)+"");

        }

        //set last message
        tvLastMessage.setText(contact.getLastMessage());

        //set online status display
        if (contact.isOnline()) {
            tvOnlineStatus.setText("Online");
        } else {
            tvOnlineStatus.setText("Offline");
            tvOnlineStatus.setTextColor(Color.RED);
        }


        return view;
    }
}
