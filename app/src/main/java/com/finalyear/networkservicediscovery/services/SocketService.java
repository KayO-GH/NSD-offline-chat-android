package com.finalyear.networkservicediscovery.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.finalyear.networkservicediscovery.activities.MainActivity;
import com.finalyear.networkservicediscovery.activities.ProvidedIpActivity;
import com.finalyear.networkservicediscovery.activities.UserDiscoveryActivity;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/**
 * Created by KayO on 20/02/2017.
 * The Socket service will be running a server for the app
 */

public class SocketService extends Service {
    Socket socket;
    ServerSocket serverSocket, tempServerSocket;

    TempServerThread tempServerThread;

    //input streams
    InputStream inputStream;
    DataInputStream din;
    // TODO: 28/03/2017 for buffering image bytes
    BufferedInputStream bis;//from Image Sharer server
    //output streams
    OutputStream outputStream;
    DataOutputStream dout;
    // TODO: 28/03/2017 for streaming the bytes through the socket
    ObjectOutputStream oos;//from Image Sharer server

    String msgIn = "";
    private String TAG = "socket_service";
    private final IBinder socketBinder = new LocalBinder();
    int port = -1;
    private HashSet<InetAddress> ipSet = new HashSet<InetAddress>();
    String incoming = "";
    ProvidedIpActivity serverUIActivity = null;

    //A service is either acting as a primary server(for messages) or secondary server (for files)
    boolean isPrimaryServer = false;

    @Override
    public void onCreate() {
        super.onCreate();

        AsyncTask<Void,Void,Void> connectServer = new ConnectServer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            connectServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        else
            connectServer.execute((Void[])null);
        Log.d(TAG, "onCreate: Server created");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: service bound");
        return socketBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    private class ConnectServer extends AsyncTask<Void, Void, Void> {

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
            try{
                serverSocket = new ServerSocket(0);//server starts at random port
                port = serverSocket.getLocalPort();
                Log.d(TAG, "doInBackground: server port = "+port);
                socket = serverSocket.accept();//server will accept connections
                Log.d(TAG, "doInBackground: client IP: "+socket.getInetAddress());
                ipSet.add(socket.getInetAddress());
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                din = new DataInputStream(inputStream);
                Log.d(TAG, "new Input stream");
                dout = new DataOutputStream(outputStream);

                while(!msgIn.equals("exit")){
                    msgIn = din.readUTF();//get new incoming message
                    if(!msgIn.equals(""))
                        Log.d(TAG, "Incoming message: " + msgIn);
                    //display messages from client
                    publishProgress();//update UI
                }
            } catch (IOException ex) {
                //Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //Todo: Update the UI or log messages or both
            //display messages from client
            /*received = true;
            if(isServer)
                receiveChatMessage("Client:\t" + msgIn);
                //tvDisplay.setText(tvDisplay.getText().toString().trim() + "Client:\t" + msgIn);
            else
                receiveChatMessage("Server:\t" + msgIn);*/
            //tvDisplay.setText(tvDisplay.getText().toString().trim() + "\nServer:\t" + msgIn);

            if(serverUIActivity != null){//activity has sent itself
                serverUIActivity.receiveChatMessage("Client:\t"+msgIn);
            }
        }

    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SocketService getService(){
            return SocketService.this;
        }
    }

    //port of server
    public int getPort(){
        return port;
    }

    //set of currently connected IP Addresses
    public HashSet<InetAddress> getIpSet() {
        return ipSet;
    }

    //get incoming message
    public String getMsgIn() {
        return msgIn;
    }

    public boolean sendMessage(String message){
        try {
            if (dout != null) {
                dout.writeUTF(message);//send message
                return true;
            } else
                Toast.makeText(getApplicationContext(), "dout is null, no socket connection", Toast.LENGTH_LONG).show();
                return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public DataInputStream getDin() {
        return din;
    }

    public void setServerUIActivity(ProvidedIpActivity serverUIActivity) {
        this.serverUIActivity = serverUIActivity;
    }

    public void setPrimaryServer(boolean primaryServer) {
        isPrimaryServer = primaryServer;
    }

    public void sendImage(String path) {
        //start temporary server
        tempServerThread = new TempServerThread(path);
        tempServerThread.start();
    }

    private class FileTxThread extends Thread {
        String path;
        Socket tempSocket;

        FileTxThread(Socket tempSocket, String path) {
            this.path = path;
            this.tempSocket = tempSocket;
        }

        @Override
        public void run() {

            String queriedPath = path;
            File file = new File(queriedPath);
            Log.d(TAG, "queriedPath: "+queriedPath);

            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis;
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytes, 0, bytes.length);//store bytes in byte[] bytes

                ObjectOutputStream oos = new ObjectOutputStream(tempSocket.getOutputStream());
                oos.writeObject(bytes);//write bytes to the socket
                oos.flush();

                tempSocket.close();

                final String sentMsg = "File sent to: " + tempSocket.getInetAddress();
                serverUIActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(serverUIActivity,
                                sentMsg,
                                Toast.LENGTH_LONG).show();
                    }});

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    tempSocket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    private class TempServerThread extends Thread{
        String picPath;
        public TempServerThread(String path) {
            this.picPath = path;
        }

        @Override
        public void run() {
            Socket tempSocket = null;//temporary image transfer socket

            try {
                tempServerSocket = new ServerSocket(0);//temporary server socket on random available port
                serverUIActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serverUIActivity.sendMessage("##port:"+tempServerSocket.getLocalPort());
                    }
                });

                //send this port alert to the recipient to authorize transfer by connecting to this server

                while (true) {
                    tempSocket = tempServerSocket.accept();//blocks loop till a socket connection is accepted
                    //when a connection is established, send the file
                    FileTxThread fileTxThread = new FileTxThread(tempSocket,picPath);
                    fileTxThread.start();
                    //break; // TODO: 05/04/2017 find a way to break out of this loop without throwing an exception
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (tempSocket != null) {
                    try {
                        tempSocket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
