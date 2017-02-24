package com.finalyear.networkservicediscovery.activities;

import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

import com.finalyear.networkservicediscovery.R;
import com.finalyear.networkservicediscovery.adapters.ChatArrayAdapter;
import com.finalyear.networkservicediscovery.pojos.ChatMessage;
import com.finalyear.networkservicediscovery.pojos.Contact;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//// TODO: 07/02/2017 on destroying this activity, close the socket. Everyone enters this activity as a client
// you may be required to change status to server while here, if te person you have been messaging opens their chat window
//An opening of the chat window by the other party will send u a message informing you to change status immediately
public class ProvidedIpActivity extends AppCompatActivity {
    private static final String TAG = "connect_server";
    private TextView tvIpAndPort;
    private ListView lvDisplay;
    private Button btSend;
    private EditText etMessage;
    static Socket socket;
    static DataInputStream din;
    static DataOutputStream dout;
    private String msgIn = "";
    //for server mode
    static ServerSocket serverSocket;
    private String ip;
    private boolean isServer = false;
    private boolean received = false;
    private ChatArrayAdapter chatArrayAdapter;
    private Contact contact;
    private int port;
    private int myPort;
    AsyncTask<Void,Void,Void> connectTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_ip);

        init();
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        lvDisplay.setAdapter(chatArrayAdapter);

        Bundle receivedSocketData = getIntent().getBundleExtra("socket_bundle");
        contact = (Contact) receivedSocketData.getSerializable("contact");
        myPort = receivedSocketData.getInt("myPort");

        ip = contact.getIpAddress().toString().substring(1);//eliminate '/' at the beginning of ip address
        port = contact.getPort();
        Toast.makeText(ProvidedIpActivity.this, ip+"  "+port, Toast.LENGTH_SHORT).show();

        //Todo: if the other person is joining a conversation you started
        //Todo: they'll inform you to become the server and they become the client
        connectTask = new ConnectServer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            connectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        else
            connectTask.execute((Void[])null);

                /*//user is the server
                isServer = true;
                new ConnectServer(socket, din, dout, serverSocket, isServer).execute();*/


        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msgOut = etMessage.getText().toString().trim();
                try {
                    if (dout != null) {
                        dout.writeUTF(msgOut);//send message
                        if (isServer)
                            showChatMessage("Server:\t" + msgOut);
                        else
                            showChatMessage("Client:\t" + msgOut);
                    } else
                        Toast.makeText(getApplicationContext(), "dout is null, no socket connection", Toast.LENGTH_LONG).show();

                    etMessage.requestFocus();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

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
    }

    private boolean showChatMessage(String s) {
        received = false;
        chatArrayAdapter.add(new ChatMessage(received, s));
        etMessage.setText("");
        return true;
    }

    private void init() {
        //conversation screen items
        lvDisplay = (ListView) findViewById(R.id.lvDisplay);
        btSend = (Button) findViewById(R.id.btSend);
        etMessage = (EditText) findViewById(R.id.etMessage);
        tvIpAndPort = (TextView) findViewById(R.id.tvIpAndPort);
    }


    private class ConnectServer extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //onPostExecute, server status has been switched... start AsyncTask again
            //new ConnectServer(socket, din, dout, serverSocket, isServer).execute();
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                connectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
            else
                connectTask.execute((Void[])null);*/
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (!isServer) {//not the server
                try {
                    if (socket == null) {
                        socket = new Socket(ip, port);//server ip
                        Log.d(TAG, "doInBackground: new socket created");
                    }else{
                        Log.d(TAG, "doInBackground: socket already exists");
                    }
                    din = new DataInputStream(socket.getInputStream());
                    dout = new DataOutputStream(socket.getOutputStream());
                    while (!msgIn.equals("##switch_server")) {
                        msgIn = din.readUTF();//get new incoming message
                        //Toast.makeText(getApplicationContext(),msgIn,Toast.LENGTH_LONG).show();
                        Log.d("incoming", msgIn);
                        publishProgress();//update UI
                    }
                    if(msgIn.equals("##switch_server")){
                        //change to server
                        isServer = !isServer;//isServer set to true
                        Log.d("switch_server", "##switch_server encountered");
                    }
                } catch (IOException e) {
                    Log.d(TAG, "doInBackground: exception in code");
                    e.printStackTrace();

                }
            } else {//I'm the server
                try {
                    serverSocket = new ServerSocket(myPort);//server starts at port 1201
                    socket = serverSocket.accept();//server will accept connections

                    din = new DataInputStream(socket.getInputStream());
                    dout = new DataOutputStream(socket.getOutputStream());

                    while (!msgIn.equals("exit")) {
                        msgIn = din.readUTF();//get new incoming message
                        Log.d("incoming", msgIn);
                        //display messages from client
                        publishProgress();//update UI
                    }
                } catch (IOException ex) {
                    //Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //display messages from client
            received = true;
            if (isServer)
                receiveChatMessage("Client:\t" + msgIn);
                //tvDisplay.setText(tvDisplay.getText().toString().trim() + "Client:\t" + msgIn);
            else
                receiveChatMessage("Server:\t" + msgIn);
            //tvDisplay.setText(tvDisplay.getText().toString().trim() + "\nServer:\t" + msgIn);
        }

        private void receiveChatMessage(String s) {
            received = true;
            chatArrayAdapter.add(new ChatMessage(received, s));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        menu.removeItem(R.id.connect_item);
        menu.removeItem(R.id.discover_item);
        menu.removeItem(R.id.register_item);
        menu.removeItem(R.id.manual_ip_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.discovery_mode_item) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
