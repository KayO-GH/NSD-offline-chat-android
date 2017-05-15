package com.finalyear.networkservicediscovery.activities;

/**
 * Created by KayO on 17/12/2016.
 */

import android.app.Dialog;
import android.database.DataSetObserver;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.finalyear.networkservicediscovery.adapters.ChatArrayAdapter;
import com.finalyear.networkservicediscovery.pojos.ChatMessage;
import com.finalyear.networkservicediscovery.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ManualIpActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        init();
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(),R.layout.right);
        lvDisplay.setAdapter(chatArrayAdapter);

        //Use dialog to select between being a server or a client
        final Dialog ipDialog = new Dialog(ManualIpActivity.this);
        ipDialog.setTitle("Manual Connection");
        ipDialog.setContentView(R.layout.ip_dialog_layout);
        ipDialog.show();

        final EditText etServerIP = (EditText) ipDialog.findViewById(R.id.etServerIP);
        Button btSubmitIP = (Button) ipDialog.findViewById(R.id.btSubmitIP);
        Button btCancelIP = (Button) ipDialog.findViewById(R.id.btCancelIP);
        Button btServerYes = (Button) ipDialog.findViewById(R.id.btServerYes);
        Button btServerNo = (Button) ipDialog.findViewById(R.id.btServerNo);
        final LinearLayout llModeSelect = (LinearLayout) ipDialog.findViewById(R.id.llModeSelect);
        final LinearLayout llServerIP = (LinearLayout) ipDialog.findViewById(R.id.llServerIP);

        btSubmitIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip = etServerIP.getText().toString().trim();
                ipDialog.dismiss();
                new ConnectServer(socket, din, dout, serverSocket, isServer).execute();
            }
        });

        btCancelIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ipDialog.dismiss();
            }
        });

        btServerYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //user is the server
                isServer = true;
                ipDialog.dismiss();
                new ConnectServer(socket, din, dout, serverSocket, isServer).execute();
                WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                tvIpAndPort.setText(Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress())+"");
            }
        });

        btServerNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //user is NOT the server
                //Let the user input IP of server
                llModeSelect.setVisibility(View.GONE);
                llServerIP.setVisibility(View.VISIBLE);
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msgOut = etMessage.getText().toString().trim();
                try {
                    if(dout != null) {
                        dout.writeUTF(msgOut);//send message
                        if(isServer)
                            sendChatMessage("Server:\t" + msgOut);
                        else
                            sendChatMessage("Client:\t" + msgOut);
                    }
                    else
                        Toast.makeText(getApplicationContext(),"dout is null, no socket connection",Toast.LENGTH_LONG).show();

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
                lvDisplay.setSelection(chatArrayAdapter.getCount()-1);
            }
        });
    }

    private boolean sendChatMessage(String s) {
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
        public ConnectServer(Socket socket, DataInputStream din, DataOutputStream dout, ServerSocket serverSocket, boolean isServer) {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(!isServer){//not the server
                try {
                    if (socket == null){
                        socket = new Socket(ip, 1201);//server ip
                        //socket = new Socket("10.9.2.165", 1201);wifi knust given ip...didn't work
                    }
                    //socket = new Socket("192.168.43.28", 1201);//server ip

                    din = new DataInputStream(socket.getInputStream());
                    dout = new DataOutputStream(socket.getOutputStream());
                    while (!msgIn.equals("exit")) {
                        msgIn = din.readUTF();//get new incoming message
                        //Toast.makeText(getApplicationContext(),msgIn,Toast.LENGTH_LONG).show();
                        publishProgress();//update UI
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try{
                    serverSocket = new ServerSocket(1201);//server starts at port 1201
                    socket = serverSocket.accept();//server will accept connections

                    din = new DataInputStream(socket.getInputStream());
                    dout = new DataOutputStream(socket.getOutputStream());

                    while(!msgIn.equals("exit")){
                        msgIn = din.readUTF();//get new incoming message
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
            if(isServer)
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

        if(id==R.id.discovery_mode_item){
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

