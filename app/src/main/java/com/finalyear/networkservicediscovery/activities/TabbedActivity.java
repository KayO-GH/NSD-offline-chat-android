package com.finalyear.networkservicediscovery.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.finalyear.networkservicediscovery.R;
import com.finalyear.networkservicediscovery.adapters.DiscoveryListAdapter;
import com.finalyear.networkservicediscovery.adapters.ViewPagerAdapter;
import com.finalyear.networkservicediscovery.pojos.Contact;
import com.finalyear.networkservicediscovery.services.SocketService;
import com.finalyear.networkservicediscovery.utils.database.DiscoveryManager;
import com.finalyear.networkservicediscovery.utils.nsd_classes.ChatConnection;
import com.finalyear.networkservicediscovery.utils.nsd_classes.NsdHelper;

import java.net.Inet4Address;

public class TabbedActivity extends AppCompatActivity {
    private NsdHelper nsdHelper;
    private ListView lvDiscoveryList;
    private DiscoveryListAdapter discoveryListAdapter;

    public DiscoveryListAdapter getDiscoveryListAdapter() {
        return discoveryListAdapter;
    }

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
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;// Declaring the Toolbar Object

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            socketService = binder.getService();
            bound = true;
            Log.d(TAG, "about to register ");
            AsyncTask<Void, Void, Void> registerTask = new TabbedActivity.RegisterSequence();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                registerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            else
                registerTask.execute((Void[]) null);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
            Log.d(TAG, "onServiceDisconnected: Service Unbound");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Assigning view variables to thier respective view in xml
        by findViewByID method
         */
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        /*
        Creating Adapter and setting that adapter to the viewPager
        setSupportActionBar method takes the toolbar and sets it as
        the default action bar thus making the toolbar work like a normal
        action bar.
         */
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        setSupportActionBar(toolbar);

        /*
        TabLayout.newTab() method creates a tab view, Now a Tab view is not the view
        which is below the tabs, its the tab itself.
         */

        final TabLayout.Tab chatTab = tabLayout.newTab();
        final TabLayout.Tab groupTab = tabLayout.newTab();
        final TabLayout.Tab filesTab = tabLayout.newTab();

        /*
        Setting Title text for our tabs respectively
         */

        chatTab.setText("Chat");
        groupTab.setText("Group");
        filesTab.setText("Files");

        /*
        Adding the tab view to our tablayout at appropriate positions
        As I want home at first position I am passing chatTab and 0 as argument to
        the tablayout and like wise for other tabs as well
         */
        tabLayout.addTab(chatTab, 0);
        tabLayout.addTab(groupTab, 1);
        tabLayout.addTab(filesTab, 2);

        /*
        TabTextColor sets the color for the title of the tabs, passing a ColorStateList here makes
        tab change colors in different situations such as selected, active, inactive etc

        TabIndicatorColor sets the color for the indiactor below the tabs
         */
        tabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.tab_selector));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.indicator));

        /*
        Adding a onPageChangeListener to the viewPager
        1st we add the PageChangeListener and pass a TabLayoutPageChangeListener so that Tabs Selection
        changes when a viewpager page changes.
         */
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        init();

        //start the service now even before binding to ensure that port is ready when needed
        Intent serviceIntent = new Intent(getApplicationContext(), SocketService.class);
        startService(serviceIntent);
        Log.d(TAG, "onStart: Service started");

        lvDiscoveryList.setAdapter(discoveryListAdapter);
        //chatArrayAdapter = new ChatArrayAdapter(UserDiscoveryActivity.this,R.layout.right);

        Bundle receivedIdentity = getIntent().getBundleExtra("identity_bundle");
        userName = receivedIdentity.getString("identity");
        nsdHelper = new NsdHelper(ctx, discoveryManager, discoveryListAdapter);
        nsdHelper.mServiceName = userName;
        nsdHelper.initializeNsd();
    }

    private void init() {

    }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                Log.d(TAG, "doInBackground: looping, port = " + socketService.getPort());
                //if (chatConnection.getLocalPort() > -1) {
                if (socketService.getPort() > -1) {
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
                Toast.makeText(TabbedActivity.this, "Port: " + currentPort, Toast.LENGTH_SHORT).show();

            } else {
                Log.d(TAG, "ServerSocket isn't bound.LocalPort returned is: " + currentPort);
                Toast.makeText(TabbedActivity.this, "ServerSocket isn't bound.", Toast.LENGTH_SHORT).show();
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
