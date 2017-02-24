package com.finalyear.networkservicediscovery.services;

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

import com.finalyear.networkservicediscovery.activities.UserDiscoveryActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    ServerSocket serverSocket;
    DataInputStream din;
    DataOutputStream dout;
    String msgIn = "";
    private String TAG = "socket_service";
    private final IBinder socketBinder = new LocalBinder();
    int port = -1;
    private HashSet<InetAddress> ipSet = new HashSet<InetAddress>();

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

                din = new DataInputStream(socket.getInputStream());
                Log.d(TAG, "new Input stream");
                dout = new DataOutputStream(socket.getOutputStream());

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
}
