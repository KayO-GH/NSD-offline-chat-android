package com.finalyear.networkservicediscovery.activities;


//ListView holding list of users will show here

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.finalyear.networkservicediscovery.R;
import com.finalyear.networkservicediscovery.adapters.ChatArrayAdapter;
import com.finalyear.networkservicediscovery.adapters.DiscoveryListAdapter;
import com.finalyear.networkservicediscovery.pojos.ChatMessage;
import com.finalyear.networkservicediscovery.utils.database.DiscoveryManager;
import com.finalyear.networkservicediscovery.utils.nsd_classes.ChatConnection;
import com.finalyear.networkservicediscovery.utils.nsd_classes.NsdHelper;

public class UserDiscoveryActivity extends AppCompatActivity {

    private NsdHelper nsdHelper;
    private ListView lvDiscoveryList;
    private DiscoveryListAdapter discoveryListAdapter;
    private DiscoveryManager discoveryManager;

    private Handler updateHandler;

    public static final String TAG = "NsdChat";
    private String userName = null;
    private Context ctx = this;

    private ChatConnection chatConnection;
    //private ChatArrayAdapter chatArrayAdapter;
    private int currentPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        getSupportActionBar().setTitle("Discovery Activity");

        init();
        lvDiscoveryList.setAdapter(discoveryListAdapter);
        //chatArrayAdapter = new ChatArrayAdapter(UserDiscoveryActivity.this,R.layout.right);

        Bundle receivedIdentity = getIntent().getBundleExtra("identity_bundle");
        userName = receivedIdentity.getString("identity");
        nsdHelper = new NsdHelper(ctx);
        nsdHelper.mServiceName = userName;
        nsdHelper.initializeNsd();

        //might have to override some methods for this handler object (check the class that uses mUpdateHandler)
        updateHandler = new Handler();
        /*{
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                if(chatLine.startsWith("me")){
                    addChatLine(chatLine,false);
                }else{
                    addChatLine(chatLine,true);
                }
            }
        };*/
        chatConnection = new ChatConnection(updateHandler);


        //advertise yourself
        new RegisterSequence().execute();

        //scan the network for app instances
        nsdHelper.discoverServices();

        //Todo: ALGORITHM FOR THIS ACTIVITY IS IN THE EXERCISE BOOK I USED FOR NETWORKING REVISION

    }

    /*public void addChatLine(String line,boolean received) {
        //mStatusView.append("\n" + line);
        chatArrayAdapter.add(new ChatMessage(received, line));
    }*/

    private void init() {
        lvDiscoveryList = (ListView) findViewById(R.id.lvDiscoveryList);
        discoveryManager = new DiscoveryManager(getApplicationContext());
        discoveryListAdapter = new DiscoveryListAdapter(getApplicationContext(), discoveryManager.getAllContacts());//finish this up with the SavedListActivity forat in KayO
    }

    @Override
    protected void onPause() {
        if (nsdHelper != null) {
          nsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nsdHelper != null) {
            nsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        nsdHelper.tearDown();
        chatConnection.tearDown();
        super.onDestroy();
    }

    //registration is done using an async task and while loop to make sure that registration is seen
    private class RegisterSequence extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                if (chatConnection.getLocalPort() > -1) {
                    //port number set
                    currentPort = chatConnection.getLocalPort();
                    break;
                }
            }

            //Todo: now on UI thread, run discovery
            nsdHelper.discoverServices();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (currentPort > -1) {
                nsdHelper.registerService(currentPort);
                Toast.makeText(UserDiscoveryActivity.this, "Port: " + currentPort, Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "ServerSocket isn't bound.LocalPort returned is: " + currentPort);
                Toast.makeText(UserDiscoveryActivity.this, "ServerSocket isn't bound.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
