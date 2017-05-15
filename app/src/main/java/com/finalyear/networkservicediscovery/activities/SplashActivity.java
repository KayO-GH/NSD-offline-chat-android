package com.finalyear.networkservicediscovery.activities;

//check for first time run, if it's the first time take the user to the registration screen
//if it's not, take him/her to the discovery page
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.finalyear.networkservicediscovery.R;
import com.finalyear.networkservicediscovery.pojos.LocalInfo;
import com.finalyear.networkservicediscovery.utils.database.LocalInfoManager;

public class SplashActivity extends AppCompatActivity {
    boolean isFirstRun = false;
    private LocalInfo localInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //getSupportActionBar().hide();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new CheckRunState().execute();
            }
        }, 1200);


    }

    private class CheckRunState extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show welcome image
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(isFirstRun){
                //move to registration activity
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
                finish();
            }else{
                //bundle user identity to next activity
                //move to discovery activity
                Intent discoverIntent = new Intent(getApplicationContext(), UserDiscoveryActivity.class);
                Bundle pushIdentity = new Bundle();
                pushIdentity.putString("identity", localInfo.getIdentity());
                discoverIntent.putExtra("identity_bundle",pushIdentity);
                startActivity(discoverIntent);
                finish();
            }
            finish();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //DO DATABASE CHECK OVER HERE
            localInfo = new LocalInfoManager(getApplicationContext()).getLocalInfo();
            isFirstRun = localInfo.isFirstTime();
            return null;
        }
    }
}
