package com.finalyear.networkservicediscovery.utils.nsd_classes;

/**
 * Created by KayO on 07/12/2016.
 */
import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;
import android.util.Log;
import android.widget.Toast;

import com.finalyear.networkservicediscovery.adapters.DiscoveryListAdapter;
import com.finalyear.networkservicediscovery.pojos.Contact;
import com.finalyear.networkservicediscovery.utils.database.DiscoveryManager;

import java.util.ArrayList;

public class NsdHelper {

    Activity mContext;
    DiscoveryManager discoveryManager;
    DiscoveryListAdapter discoveryListAdapter;
    ArrayList<Contact>currentContactList;

    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;
    private boolean registerListenerInUse = false, discoverListenerInUse = false;

    //public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String SERVICE_TYPE = "_NsdChat._tcp.";

    public static final String TAG = "NsdHelper";
    public String mServiceName = null;

    public NsdServiceInfo mService;

    public NsdHelper(Activity context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public NsdHelper(Activity context, DiscoveryManager discoveryManager, DiscoveryListAdapter discoveryListAdapter) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        this.discoveryManager = discoveryManager;
        this.discoveryListAdapter = discoveryListAdapter;
        currentContactList = this.discoveryManager.getAllContacts();
    }

    public void initializeNsd() {
        //initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();

        //mNsdManager.init(mContext.getMainLooper(), this);

    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else{
                    //resolve service
                    mNsdManager.resolveService(service, new MyResolveListener());
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if (mService == service) {
                    mService = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    /*public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    //remove return so code is no halted
                    return;
                }
                mService = serviceInfo;
                //see details for current service info resolved
                Toast.makeText(mContext, mService.toString(), Toast.LENGTH_SHORT).show();
                //add discovered contact to list of current online users in a Listview
                Contact foundContact = new Contact(mService.getServiceName(),null,null,null);
                foundContact.setIpAddress(mService.getHost());
                foundContact.setPort(mService.getPort());
                currentContactList.add(foundContact);
                discoveryListAdapter.setAllContacts(currentContactList);
                discoveryListAdapter.notifyDataSetChanged();

            }
        };
    }*/

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                Toast.makeText(mContext, "Registration successful: "+mServiceName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Toast.makeText(mContext, "Registration failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }

        };
    }

    public void registerService(int port) {
        //check if listener is already in use before attempting to register service
        if(!registerListenerInUse){
            NsdServiceInfo serviceInfo  = new NsdServiceInfo();
            serviceInfo.setPort(port);
            serviceInfo.setServiceName(mServiceName);
            serviceInfo.setServiceType(SERVICE_TYPE);

            mNsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

            //set flag to show register Listener is in use
            registerListenerInUse = true;
        }


    }

    public void discoverServices() {
        if(!discoverListenerInUse) {
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

            //set flag to show register Listener is in use
            discoverListenerInUse = true;
        }
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }

    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    private class MyResolveListener implements NsdManager.ResolveListener {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, "Resolve failed" + errorCode);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

            if (serviceInfo.getServiceName().equals(mServiceName)) {
                Log.d(TAG, "Same IP.");
                //remove return so code is no halted
                return;
            }
            mService = serviceInfo;
            //see details for current service info resolved
            Toast.makeText(mContext, mService.toString(), Toast.LENGTH_SHORT).show();
            //add discovered contact to list of current online users in a Listview
            Contact foundContact = new Contact(mService.getServiceName(),null,null,null);
            foundContact.setIpAddress(mService.getHost());
            foundContact.setPort(mService.getPort());
            currentContactList.add(foundContact);
            discoveryListAdapter.setAllContacts(currentContactList);
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    discoveryListAdapter.notifyDataSetChanged();
                }
            });


        }
    }
}
