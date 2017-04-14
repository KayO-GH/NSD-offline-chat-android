package com.finalyear.networkservicediscovery.activities;

/*
Forced data to activity from within service by passing the calling activity to the service
*/
// TODO: 28/03/2017 receive path to the image to be sent from SendFileActivity and call function in service to send the file

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.finalyear.networkservicediscovery.services.SocketService;
import com.finalyear.networkservicediscovery.utils.database.MessageManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

//// TODO: 07/02/2017 on destroying this activity, close the socket. Everyone enters this activity as a client
// you may be required to change status to server while here, if te person you have been messaging opens their chat window
//An opening of the chat window by the other party will send u a message informing you to change status immediately
public class ProvidedIpActivity extends AppCompatActivity {
    private static final int PICK_FILE = 100;
    String fileType = "type";

    private static final String TAG = "socket_service";
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
    AsyncTask<Void, Void, Void> connectTask;
    SocketService socketService;
    boolean bound = false;
    InputStream inputStream;
    OutputStream outputStream;
    Toolbar toolbar;

    static final Integer WRITE_EXST = 0x3;

    //post-permission variables (for Marshmallow)
    String pp_ip;
    int pp_port;
    String pp_fileName;

    Calendar calendar = Calendar.getInstance();
    MessageManager messageManager;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            socketService = binder.getService();
            Log.d(TAG, "onServiceConnected: socketService created");
            bound = true;

            //Todo: if the other person is joining a conversation you started
            //Todo: they'll inform you to become the server and they become the client
            connectTask = new ConnectServer();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                connectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            else
                connectTask.execute((Void[]) null);

            socketService.setServerUIActivity(ProvidedIpActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//upon getting file to send from sendFileActivity
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            byte[] imageByteArray = data.getByteArrayExtra("imageArray");
            String filePath = data.getStringExtra("image_path");
            //from here...convert byte array to bitmap and display
            //TODO Bitmap bitmapToShow = ImageConversionUtil.convertByteArrayToPhoto(imageByteArray);

            //call file transfer thread in service
            socketService.setComplete(false);
            socketService.sendFile(filePath);

        } else {
            Toast.makeText(ProvidedIpActivity.this,
                    "Something went wrong... the image is lost", Toast.LENGTH_SHORT).show();
        }
    }

    /*private void sendFile(byte[] bytesToSend) {
        //code depends on whether you are the sender or the receiver
        if(isServer){
            socketService.sendFile(bytesToSend);
        }
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        Intent bindIntent = new Intent(getApplicationContext(), SocketService.class);
        if (getApplicationContext().bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)) {
            Log.d(TAG, "onStart: bindService succeeded");
        } else {
            Log.d(TAG, "onStart: bindService failed");
        }

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        lvDisplay.setAdapter(chatArrayAdapter);

        Bundle receivedSocketData = getIntent().getBundleExtra("socket_bundle");
        isServer = receivedSocketData.getBoolean("isServer");
        contact = (Contact) receivedSocketData.getSerializable("contact");
        myPort = receivedSocketData.getInt("myPort");

        //set activity title so user knows who he's talking to
        getSupportActionBar().setTitle(contact.getName());

        //load messages from database to be displayed in listview
        //make database call
        ArrayList<ChatMessage> savedMessages = messageManager.getChatsWithThisUser(contact);
        for (ChatMessage msg : savedMessages)
            chatArrayAdapter.add(new ChatMessage(msg.isReceived(), msg.getMessageContent()));

        ip = contact.getIpAddress().toString().substring(1);//eliminate '/' at the beginning of ip address
        port = contact.getPort();
        Toast.makeText(ProvidedIpActivity.this, ip + "  " + port, Toast.LENGTH_SHORT).show();

                /*//user is the server
                isServer = true;
                new ConnectServer(socket, din, dout, serverSocket, isServer).execute();*/

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msgOut = etMessage.getText().toString().trim();
                sendMessage(msgOut);
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

    //function to send message
    public void sendMessage(String msgOut) {
        //If server, interact with service, else do dout.writeUTF()
        if (isServer) {
            //interact with service
            if (socketService.sendMessage(msgOut)) {
                //message sent successfully
                showChatMessage("Server:\t" + msgOut, false);
            } else {
                Toast.makeText(getApplicationContext(), "Error sending message", Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                if (dout != null) {
                    dout.writeUTF(msgOut);//send message
                    showChatMessage("Client:\t" + msgOut, false);
                } else
                    Toast.makeText(getApplicationContext(), "dout is null, no socket connection", Toast.LENGTH_LONG).show();

                etMessage.requestFocus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        setContentView(R.layout.activity_manual_ip);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        init();
    }

    public boolean showChatMessage(String s, boolean isReceived) {
        received = isReceived;
        chatArrayAdapter.add(new ChatMessage(received, s));
        if (!isReceived)//outgoing message
            etMessage.setText("");

        //store messages in database over here
        // TODO: 10/04/2017 Find a more appropriate place to move the store message function.
        // TODO: 10/04/2017 Messages should be stored when received, even at the service level... not just when shown on screen
        storeMessage(s, contact, isReceived);//params: message, other party, reception state
        return true;
    }

    private void storeMessage(String message, Contact contact, boolean isReceived) {
        ChatMessage thisMessage;
        if (isReceived) {
            thisMessage = new ChatMessage(0, message, contact.getName(), "##me", calendar.get(Calendar.SECOND), false, true);//0 value for messageID is ignored. A better value is null, but incompatible with long
        } else {
            thisMessage = new ChatMessage(0, message, "##me", contact.getName(), calendar.get(Calendar.SECOND), true, false);
        }
        if (messageManager.createChatMessage(thisMessage)) {
            Toast.makeText(this, "Message saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        //conversation screen items
        lvDisplay = (ListView) findViewById(R.id.lvDisplay);
        btSend = (Button) findViewById(R.id.btSend);
        etMessage = (EditText) findViewById(R.id.etMessage);
        tvIpAndPort = (TextView) findViewById(R.id.tvIpAndPort);
        messageManager = new MessageManager(getApplicationContext());
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProvidedIpActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(ProvidedIpActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(ProvidedIpActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
            ClientRxThread clientRxThread =
                    new ClientRxThread(pp_ip, pp_port, pp_fileName);

            clientRxThread.start();

        }
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        closeSocket();
    }*/

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
                // TODO: 24/02/2017 Server doesn't change anymore, so clean up switch server code
                try {
                    if (socket == null) {
                        socket = new Socket(ip, port);//server ip
                        Log.d(TAG, "doInBackground: new socket created");
                    } else {
                        Log.d(TAG, "doInBackground: socket already exists");
                    }
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();

                    din = new DataInputStream(inputStream);
                    dout = new DataOutputStream(outputStream);
                    while (!msgIn.equals("##exit")) {//close socket when msgIn is ##exit
                        if (msgIn.contains("##port:")) {
                            //extract new port number and connect to new socket
                            connectToFilePort(
                                    ip,
                                    Integer.valueOf(msgIn.substring(msgIn.indexOf(':') + 1, msgIn.indexOf('/'))),
                                    msgIn.substring(msgIn.lastIndexOf('/') + 1));
                        } else if (msgIn.contains("##transfer_complete")) {
                            socketService.setComplete(true);//set complete to terminate infinite loop
                        }
                        msgIn = din.readUTF();//get new incoming message
                        //Toast.makeText(getApplicationContext(),msgIn,Toast.LENGTH_LONG).show();
                        Log.d("incoming", msgIn);
                        publishProgress();//update UI
                    }

                } catch (IOException e) {
                    Log.d(TAG, "doInBackground: exception in code");
                    e.printStackTrace();

                } /*finally {
                    //close socket
                    closeSocket();
                }*/
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //display messages from client
            received = true;
            if (isServer)
                showChatMessage("Client:\t" + msgIn, received);
                //tvDisplay.setText(tvDisplay.getText().toString().trim() + "Client:\t" + msgIn);
            else
                showChatMessage("Server:\t" + msgIn, received);
            //tvDisplay.setText(tvDisplay.getText().toString().trim() + "\nServer:\t" + msgIn);
        }

    }

    private void closeSocket() {

        if (!isServer)
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void connectToFilePort(String ip, Integer port, String fileName) {
        //check if we're on Marshmallow or higher
        if (Build.VERSION.SDK_INT >= 23) {
            //define post permission variables
            pp_ip = ip;
            pp_port = port;
            pp_fileName = fileName;
            askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, WRITE_EXST);
            //client thread will be made to run in call back of ask permission function
        }else{
            ClientRxThread clientRxThread =
                    new ClientRxThread(ip, port, fileName);

            clientRxThread.start();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXST) {//redundant test since this is the only permission we ask for
            ClientRxThread clientRxThread =
                    new ClientRxThread(pp_ip, pp_port, pp_fileName);

            clientRxThread.start();
        }
    }

    /*public void receiveChatMessage(String s) {
        received = true;
        chatArrayAdapter.add(new ChatMessage(received, s));
    }*/


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
        Intent sendFileIntent = new Intent(getApplicationContext(), SendFileActivity.class);
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.send_image_item:
                sendFileIntent.putExtra(fileType, "image");
                //Bundle pushRecipient = new Bundle();
                //// TODO: 13/01/2017 send the name of the person you are talking with
                //pushRecipient.putString("recipient", service);
                //// TODO: 13/01/2017 bundle recipient's identity
                //sendImageIntent.putExtra("identity_bundle",pushRecipient);
                startActivityForResult(sendFileIntent, PICK_FILE);
                break;
            case R.id.send_audio_item:
                sendFileIntent.putExtra(fileType, "audio");
                //Bundle pushRecipient = new Bundle();
                //// TODO: 13/01/2017 send the name of the person you are talking with
                //pushRecipient.putString("recipient", service);
                //// TODO: 13/01/2017 bundle recipient's identity
                //sendImageIntent.putExtra("identity_bundle",pushRecipient);
                startActivityForResult(sendFileIntent, PICK_FILE);
                break;
            case R.id.send_video_item:
                sendFileIntent.putExtra(fileType, "video");
                //Bundle pushRecipient = new Bundle();
                //// TODO: 13/01/2017 send the name of the person you are talking with
                //pushRecipient.putString("recipient", service);
                //// TODO: 13/01/2017 bundle recipient's identity
                //sendImageIntent.putExtra("identity_bundle",pushRecipient);
                startActivityForResult(sendFileIntent, PICK_FILE);
                break;
            case R.id.send_file_item:
                sendFileIntent.putExtra(fileType, "file");
                //Bundle pushRecipient = new Bundle();
                //// TODO: 13/01/2017 send the name of the person you are talking with
                //pushRecipient.putString("recipient", service);
                //// TODO: 13/01/2017 bundle recipient's identity
                //sendImageIntent.putExtra("identity_bundle",pushRecipient);
                startActivityForResult(sendFileIntent, PICK_FILE);
                break;
            case R.id.discovery_mode_item:
                this.finish();
                break;
        }
        /*if (id == R.id.discovery_mode_item) {
            this.finish();
        } else if (id == R.id.send_image_item) {
            Intent sendImageIntent = new Intent(getApplicationContext(), SendFileActivity.class);
            Bundle pushRecipient = new Bundle();
            //// TODO: 13/01/2017 send the name of the person you are talking with
            //pushRecipient.putString("recipient", service);
            //// TODO: 13/01/2017 bundle recipient's identity
            //sendImageIntent.putExtra("identity_bundle",pushRecipient);
            startActivityForResult(sendImageIntent, PICK_FILE);
        }*/

        return super.onOptionsItemSelected(item);
    }

    private class ClientRxThread extends Thread {
        String dstAddress;
        int dstPort;
        String fileName;

        ClientRxThread(String address, int port, String fileName) {
            dstAddress = address;
            dstPort = port;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            Socket tempSocket = null;

            try {
                tempSocket = new Socket(dstAddress, dstPort);
                String completePath = Environment.getExternalStorageDirectory().toString() + "/Wi-Files";
                //make directory for our incoming files
                //note that a File object can be either an actual file or a directory
                if (isImage(fileName)) {
                    //file is an image
                    completePath += "/Wi-Files Images";
                } else if (isVideo(fileName)) {
                    completePath += "/Wi-Files Videos";
                } else if (isAudio(fileName)) {
                    completePath += "/Wi-Files Audios";
                } else {
                    completePath += "/Wi-Files";
                }
                File wifilesDirectory = new File(completePath);
                wifilesDirectory.mkdirs();
                File file = new File(
                        wifilesDirectory,
                        fileName);

                ObjectInputStream ois = new ObjectInputStream(tempSocket.getInputStream());
                byte[] bytes;
                FileOutputStream fos = null;
                try {
                    bytes = (byte[]) ois.readObject();
                    fos = new FileOutputStream(file);
                    fos.write(bytes);
                    //moved fos.close here //TODO check if problem is solved by moving content of finally block here
                    fos.close();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d(TAG, "exception reading FOS: " + e.toString());
                } /*finally {
                    if (fos != null) {
                        fos.close();

                    }

                }*/

                tempSocket.close();

                ProvidedIpActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ProvidedIpActivity.this,
                                "Transfer Finished",
                                Toast.LENGTH_LONG).show();
                        sendMessage("##transfer_complete");
                    }
                });

            } catch (IOException e) {

                e.printStackTrace();

                final String eMsg = "Something wrong: " + e.getMessage();
                Log.d(TAG, "IOException: " + e.toString());
                ProvidedIpActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ProvidedIpActivity.this,
                                eMsg,
                                Toast.LENGTH_LONG).show();
                        sendMessage(eMsg);
                    }
                });

            } finally {
                if (tempSocket != null) {
                    try {
                        tempSocket.close();
                        Log.d(TAG, "closeSocket: tempSocket closed");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.d(TAG, "closeSocket: tempSocket could NOT be closed");
                    }
                }
            }
        }
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
