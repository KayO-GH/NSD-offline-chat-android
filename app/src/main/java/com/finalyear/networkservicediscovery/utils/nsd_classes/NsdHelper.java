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

import java.net.Inet4Address;
import java.util.ArrayList;

public class NsdHelper {

    Activity mContext;
    DiscoveryManager discoveryManager;
    DiscoveryListAdapter discoveryListAdapter;
    ArrayList<Contact> currentContactList;

    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;
    private boolean registerListenerInUse = false, discoverListenerInUse = false;

    //public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String SERVICE_TYPE = "_NsdChat._tcp.";
    //public static final String SERVICE_TYPE = "_NSDChat._tcp.local.";

    public static final String TAG = "NsdHelper";
    public String mServiceName = null;

    public NsdServiceInfo mService;

    public NsdHelper(Activity context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public NsdHelper(Activity context, DiscoveryManager discoveryManager, final DiscoveryListAdapter discoveryListAdapter) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        this.discoveryManager = discoveryManager;
        this.discoveryListAdapter = discoveryListAdapter;
       /* mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                discoveryListAdapter.notifyDataSetChanged();
            }
        });*/
        currentContactList = this.discoveryManager.getAllContacts();

        //remove redundant contacts shown here
        for (int i = 0; i < currentContactList.size() - 1; i++) {//start i from first element
            for (int j = 1; j < currentContactList.size(); j++) {//start j from second element
                if (currentContactList.get(i).getName().equals(currentContactList.get(j).getName())) {
                    //duplicate encountered, remove duplicate
                    currentContactList.remove(j);
                    //reduce j by one so that after j++ the new object in j's position can be tested
                    //--j;
                }
            }
        }
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
                } else {
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
                Toast.makeText(mContext, "Registration successful: " + mServiceName, Toast.LENGTH_SHORT).show();
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
        if (!registerListenerInUse) {
            NsdServiceInfo serviceInfo = new NsdServiceInfo();
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
        if (!discoverListenerInUse) {
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

            if (serviceInfo.getServiceName().startsWith(mServiceName)) {
                Log.d(TAG, "Same IP.");
                return;
            }

            //check if service exists in database
            Contact dummyContact = new Contact();
            dummyContact.setPhoneNumber(serviceInfo.getServiceName());
            //for now, set contact name to phone number as well... we'll sort out phone number issues later
            dummyContact.setName(serviceInfo.getServiceName());
            if (discoveryManager.getContactByNumber(dummyContact) == null) {
                //contact does not exist in database
                Log.d(TAG, "onServiceResolved: contact does not exist in database: " + dummyContact.getName());
                //create it
                if (discoveryManager.createContact(dummyContact)) {
                    Log.d(TAG, "onServiceResolved: new Contact stored: " + dummyContact.getName());
                } else {
                    Log.d(TAG, "onServiceResolved: new Contact NOT stored: " + dummyContact.getName());
                }
            } else {
                Log.d(TAG, "onServiceResolved: contact ALREADY exists in database: " + dummyContact.getName());
            }

            mService = serviceInfo;
            //see details for current service info resolved
            Toast.makeText(mContext, mService.toString(), Toast.LENGTH_SHORT).show();
            //add discovered contact to list of current online users in a ListView
            final Contact foundContact = new Contact(mService.getServiceName(), null, null, null);
            foundContact.setIpAddress((Inet4Address) mService.getHost());
            foundContact.setPort(mService.getPort());
            foundContact.setOnline(true);
            //get rid of possible duplicates
            /*if (currentContactList != null)
                for (int i = 0;i< currentContactList.size();i++) {
                    if (currentContactList.get(i).getPhoneNumber() != null && (currentContactList.get(i).getPhoneNumber().equals(foundContact.getPhoneNumber())))
                        currentContactList.remove(currentContactList.get(i));
                }*/
            if (currentContactList != null)
                for (Contact contact : currentContactList) {
                    if (contact.getName().equals(foundContact.getName()))
                        currentContactList.remove(contact);
                }


            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //add newly found contact at the top of the list
                    currentContactList.add(0, foundContact);
                    discoveryListAdapter.setAllContacts(currentContactList);
                    discoveryListAdapter.notifyDataSetChanged();
                }
            });


        }
    }
}
