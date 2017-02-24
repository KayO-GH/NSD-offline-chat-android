package com.finalyear.networkservicediscovery.activities;


//ListView holding list of users will show here

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.finalyear.networkservicediscovery.R;
import com.finalyear.networkservicediscovery.adapters.ChatArrayAdapter;
import com.finalyear.networkservicediscovery.adapters.DiscoveryListAdapter;
import com.finalyear.networkservicediscovery.pojos.ChatMessage;
import com.finalyear.networkservicediscovery.pojos.Contact;
import com.finalyear.networkservicediscovery.services.SocketService;
import com.finalyear.networkservicediscovery.utils.database.DiscoveryManager;
import com.finalyear.networkservicediscovery.utils.nsd_classes.ChatConnection;
import com.finalyear.networkservicediscovery.utils.nsd_classes.NsdHelper;

import java.net.Inet4Address;

public class UserDiscoveryActivity extends AppCompatActivity {

    private NsdHelper nsdHelper;
    private ListView lvDiscoveryList;
    private DiscoveryListAdapter discoveryListAdapter;
    private DiscoveryManager discoveryManager;
    private Contact selectedConact;
    private Inet4Address selectedIP;
    private int selectedPort;

    private Handler updateHandler;

    public static final String TAG = "socket_service";
    private String userName = null;
    private Activity ctx = this;

    private ChatConnection chatConnection;
    //private ChatArrayAdapter chatArrayAdapter;
    private int currentPort;
    SocketService socketService;
    boolean bound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            socketService = binder.getService();
            bound = true;
            Log.d(TAG, "about to register ");
            AsyncTask<Void,Void,Void> registerTask = new RegisterSequence();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                registerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
            else
                registerTask.execute((Void[])null);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            getApplicationContext().unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        getSupportActionBar().setTitle("Discovery Activity");

        init();

        //start the service now even before binding to ensure that port is ready when needed
        Intent serviceIntent = new Intent(getApplicationContext(), SocketService.class);
        startService(serviceIntent);
        Log.d(TAG, "onStart: Service started");

        lvDiscoveryList.setAdapter(discoveryListAdapter);
        //chatArrayAdapter = new ChatArrayAdapter(UserDiscoveryActivity.this,R.layout.right);

        Bundle receivedIdentity = getIntent().getBundleExtra("identity_bundle");
        userName = receivedIdentity.getString("identity");
        nsdHelper = new NsdHelper(ctx,discoveryManager, discoveryListAdapter);
        nsdHelper.mServiceName = userName;
        nsdHelper.initializeNsd();

        //might have to override some methods for this handler object (check the class that uses mUpdateHandler)
        /*updateHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                if (chatLine.startsWith("me")) {
                    //addChatLine(chatLine, false);
                    // TODO: 07/02/2017 log message
                } else {
                    //addChatLine(chatLine, true);
                    // TODO: 07/02/2017 log message
                }

            }
        };*/
        /*{
            @Override
            public void handleMessage(Message msg) {
                //log messages correctly while still on this page
            }
        };*/
        //chatConnection = new ChatConnection(updateHandler);


        Intent bindIntent =  new Intent(getApplicationContext(),SocketService.class);
        getApplicationContext().bindService(bindIntent, serviceConnection,Context.BIND_AUTO_CREATE);


        lvDiscoveryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //get ip address and service name of contact at that position
                selectedConact = (Contact) lvDiscoveryList.getItemAtPosition(i);
                //extract ip, port and name in chat activity for use in connection
                selectedIP = selectedConact.getIpAddress();
                /*
                selectedPort = selectedConact.getPort();*/

                final Intent chatIntent = new Intent(getApplicationContext(), ProvidedIpActivity.class);
                Bundle pushSocket = new Bundle();
                pushSocket.putSerializable("contact", selectedConact);
                pushSocket.putInt("myPort",currentPort);

                //check if a connection has already been established
                if(alreadyConnected(selectedIP)){//server
                    Log.d(TAG, "onItemClick: Already connected to this user");
                    //a connection exists, open the chat
                    pushSocket.putBoolean("isServer",true);
                    chatIntent.putExtra("socket_bundle",pushSocket);
                    startActivity(chatIntent);
                }else{//not the server
                    Log.d(TAG, "onItemClick: New conection to this user");
                    pushSocket.putBoolean("isServer",false);
                    chatIntent.putExtra("socket_bundle",pushSocket);
                    startActivity(chatIntent);
                }


            }
        });
    }


    private boolean alreadyConnected(Inet4Address selectedIP) {
        //Todo: check if we are already connected to this socket as a server
        if (socketService.getIpSet().contains(selectedIP))
            Log.d(TAG, "already Connected: "+selectedIP.toString());
        else
            Log.d(TAG, "new Connection");
        return socketService.getIpSet().contains(selectedIP);
    }

    /*public void addChatLine(String line,boolean received) {
        chatArrayAdapter.add(new ChatMessage(true, line));//received value hard
    }*/

    private void init() {
        lvDiscoveryList = (ListView) findViewById(R.id.lvDiscoveryList);
        discoveryManager = new DiscoveryManager(getApplicationContext());
        discoveryListAdapter = new DiscoveryListAdapter(getApplicationContext(), discoveryManager.getAllContacts());//finish this up with the SavedListActivity forat in KayO
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nsdHelper != null) {
          nsdHelper.stopDiscovery();
        }
        if (bound) {
            getApplicationContext().unbindService(serviceConnection);
            bound = false;
        }
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
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute: starting register sequence");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: start of background process");
            while (true) {
                Log.d(TAG, "doInBackground: looping, port = "+ socketService.getPort());
                //if (chatConnection.getLocalPort() > -1) {
                if(socketService.getPort()>-1){
                    //port number is set
                    currentPort = socketService.getPort();
                    publishProgress();
                    break;
                }
            }
            return null;
        }



        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (currentPort > -1) {
                nsdHelper.registerService(currentPort);
                Log.d(TAG, "onProgressUpdate: registering");
                Toast.makeText(UserDiscoveryActivity.this, "Port: " + currentPort, Toast.LENGTH_SHORT).show();

            } else {
                Log.d(TAG, "ServerSocket isn't bound.LocalPort returned is: " + currentPort);
                Toast.makeText(UserDiscoveryActivity.this, "ServerSocket isn't bound.", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: post execute, discovering services");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nsdHelper.discoverServices();
                }
            });

        }
    }


}
