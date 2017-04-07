package com.finalyear.networkservicediscovery.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.finalyear.networkservicediscovery.adapters.ChatArrayAdapter;
import com.finalyear.networkservicediscovery.pojos.Contact;
import com.finalyear.networkservicediscovery.utils.nsd_classes.ChatConnection;
import com.finalyear.networkservicediscovery.pojos.ChatMessage;
import com.finalyear.networkservicediscovery.utils.nsd_classes.NsdHelper;
import com.finalyear.networkservicediscovery.R;

public class DiscoveryChatActivity extends AppCompatActivity {

    //Todo: deal with bundles from UserDiscoveryActivity

    private static final int PICK_IMAGE = 100;
    NsdHelper mNsdHelper;
    private Contact contact;

    //private TextView mStatusView;
    private ListView lvDisplay;
    private boolean alreadyConnected = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            /*byte[] bytesToSend = data.getByteArrayExtra("imageArray");
            //from here...convert byte array to bitmap and display
            Bitmap bitmapToShow = ImageConversionUtil.convertByteArrayToPhoto(bytesToSend);*/
            //send the byte array over the socket

        } else {
            Toast.makeText(DiscoveryChatActivity.this,
                    "Something went wrong... the image is lost", Toast.LENGTH_SHORT).show();
        }
    }

    private Button connect_btn, advertise_btn, discover_btn;
    private Handler mUpdateHandler;

    public static final String TAG = "NsdChat";
    private String userName = null;
    private Activity ctx = this;
    private ChatArrayAdapter chatArrayAdapter;
    private boolean received = false;

    ChatConnection mConnection;
    NsdServiceInfo service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_chat);

        init();
        chatArrayAdapter = new ChatArrayAdapter(DiscoveryChatActivity.this, R.layout.right);
        lvDisplay.setAdapter(chatArrayAdapter);

        // TODO: remove dialog code
        final Dialog nameDialog = new Dialog(DiscoveryChatActivity.this);
        nameDialog.setTitle("Discovery Name");
        nameDialog.setContentView(R.layout.name_dialog);
        //nameDialog.show();

        final EditText etSubmitName = (EditText) nameDialog.findViewById(R.id.etSubmitName);
        Button btSubmitName = (Button) nameDialog.findViewById(R.id.btSubmitName);

        btSubmitName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = etSubmitName.getText().toString().trim();
                mNsdHelper = new NsdHelper(ctx);
                mNsdHelper.mServiceName = userName;
                mNsdHelper.initializeNsd();
                nameDialog.dismiss();
            }
        });

        // TODO: end


        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                if (chatLine.startsWith("me")) {
                    addChatLine(chatLine, false);
                } else {
                    addChatLine(chatLine, true);
                }

            }
        };

        if (getIntent().getBundleExtra("connnection_bundle") != null) {
            //bundle contains connection
            mConnection = (ChatConnection) getIntent()
                    .getBundleExtra("connnection_bundle")
                    .getSerializable("connection");
            mConnection.setUpdateHandler(mUpdateHandler);
            alreadyConnected = true;
        } else {
            //bundle does not contain connection. Start new connection
            mConnection = new ChatConnection(mUpdateHandler);
            alreadyConnected = false;
        }


        /*mNsdHelper = new NsdHelper(ctx);
        mNsdHelper.mServiceName = userName;
        mNsdHelper.initializeNsd();*/

        lvDisplay.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lvDisplay.setAdapter(chatArrayAdapter);

        //to scroll the list view to the bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                lvDisplay.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        //retrieving contact info to connect
        Bundle receivedContact = getIntent().getBundleExtra("contact_bundle");
        if (receivedContact != null) {
            contact = (Contact) receivedContact.getSerializable("contact");
            getSupportActionBar().setTitle(contact.getName());
            if (!alreadyConnected)
                connect();
        }
    }

    private void connect() {
        mConnection.connectToServer(contact.getIpAddress(), contact.getPort());
    }

    private void init() {
        lvDisplay = (ListView) findViewById(R.id.lvDisplay);
        //mStatusView = (TextView) findViewById(R.id.status);
        advertise_btn = (Button) findViewById(R.id.advertise_btn);
        discover_btn = (Button) findViewById(R.id.discover_btn);
        connect_btn = (Button) findViewById(R.id.connect_btn);
    }

    public void clickAdvertise(View v) {
        // Register service
        if (mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
            Toast.makeText(DiscoveryChatActivity.this, "Port: " + mConnection.getLocalPort(), Toast.LENGTH_SHORT).show();
            //to prevent crashing, deactivate after first click
            advertise_btn.setClickable(false);
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
            Toast.makeText(DiscoveryChatActivity.this, "ServerSocket isn't bound.", Toast.LENGTH_SHORT).show();
        }
    }

    public void clickDiscover(View v) {
        mNsdHelper.discoverServices();
        //to prevent crashing, deactivate after first click
        discover_btn.setClickable(false);
    }

    public void clickConnect(View v) {
        service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");

        }
    }

    public void clickSend(View v) {
        EditText messageView = (EditText) this.findViewById(R.id.chatInput);
        if (messageView != null) {
            String messageString = messageView.getText().toString();
            if (!messageString.isEmpty()) {
                received = false;
                mConnection.sendMessage(messageString);
                //chatArrayAdapter.add(new ChatMessage(received,messageString));
            }
            messageView.setText("");
        }
    }


    public void addChatLine(String line, boolean received) {
        //mStatusView.append("\n" + line);
        chatArrayAdapter.add(new ChatMessage(received, line));
    }

    @Override
    protected void onPause() {
        //if (mNsdHelper != null) {
        //  mNsdHelper.stopDiscovery();
        // }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        if (mNsdHelper != null)
            mNsdHelper.tearDown();
        mConnection.tearDown();
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        menu.removeItem(R.id.discovery_mode_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.register_item) {
            if (mConnection.getLocalPort() > -1) {
                mNsdHelper.registerService(mConnection.getLocalPort());
                Toast.makeText(DiscoveryChatActivity.this, "Port: " + mConnection.getLocalPort(), Toast.LENGTH_SHORT).show();
                //to prevent crashing, deactivate after first click
                advertise_btn.setClickable(false);
            } else {
                Log.d(TAG, "ServerSocket isn't bound.");
                Toast.makeText(DiscoveryChatActivity.this, "ServerSocket isn't bound.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.discover_item) {
            mNsdHelper.discoverServices();
            //to prevent crashing, deactivate after first click
            discover_btn.setClickable(false);
        } else if (id == R.id.connect_item) {
            NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
            if (service != null) {
                Log.d(TAG, "Connecting.");
                mConnection.connectToServer(service.getHost(),
                        service.getPort());
            } else {
                Log.d(TAG, "No service to connect to!");
            }
        } else if (id == R.id.manual_ip_item) {
            if (mNsdHelper != null) {
                mNsdHelper.stopDiscovery();
            }
            Intent manualIntent = new Intent(getApplicationContext(), ManualIpActivity.class);
            startActivity(manualIntent);
        } else if (id == R.id.send_image_item) {
            Intent sendImageIntent = new Intent(getApplicationContext(), SendFileActivity.class);
            Bundle pushRecipient = new Bundle();
            //// TODO: 13/01/2017 send the name of the person you are talking with
            //pushRecipient.putString("recipient", service);
            //// TODO: 13/01/2017 bundle recipient's identity
            //sendImageIntent.putExtra("identity_bundle",pushRecipient);
            startActivityForResult(sendImageIntent, PICK_IMAGE);


        }


        return super.onOptionsItemSelected(item);
    }

}

