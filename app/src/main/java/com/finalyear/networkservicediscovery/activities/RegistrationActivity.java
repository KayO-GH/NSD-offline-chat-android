package com.finalyear.networkservicediscovery.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.finalyear.networkservicediscovery.R;
import com.finalyear.networkservicediscovery.pojos.LocalInfo;
import com.finalyear.networkservicediscovery.utils.database.LocalInfoManager;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etServiceName;
    private Button btSubmitNumber;
    private LocalInfo newInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //getSupportActionBar().setTitle("Registration");

        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();

        init();
        btSubmitNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send entry to datatbase and update first time run to false
                //Todo:Perform validation on phone number
                //Todo: use a dialog box to confirm that user is sure about what he's about to do
                newInfo.setFirstTime(false);
                String serviceName = etServiceName.getText().toString();
                newInfo.setIdentity(serviceName);
                if(new LocalInfoManager(getApplicationContext()).createLocalInfo(newInfo)){
                    Toast.makeText(RegistrationActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                    //bundle user identity to next activity
                    //move to discovery activity
                    Intent discoverIntent = new Intent(getApplicationContext(), UserDiscoveryActivity.class);
                    Bundle pushIdentity = new Bundle();
                    pushIdentity.putString("identity", newInfo.getIdentity());
                    discoverIntent.putExtra("identity_bundle",pushIdentity);
                    startActivity(discoverIntent);
                    finish();
                }
                else{
                    Toast.makeText(RegistrationActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void init() {
        etServiceName = (EditText) findViewById(R.id.etServiceName);
        btSubmitNumber = (Button) findViewById(R.id.btSubmitNumber);
        newInfo = new LocalInfo();
    }
}
