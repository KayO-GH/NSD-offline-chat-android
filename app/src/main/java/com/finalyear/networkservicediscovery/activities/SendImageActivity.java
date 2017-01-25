package com.finalyear.networkservicediscovery.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.finalyear.networkservicediscovery.R;
import com.finalyear.networkservicediscovery.utils.ImageConversionUtil;

import java.io.IOException;

public class SendImageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private ImageView ivImageToSend;
    private Button btConfirmYes, btConfirmNo;
    Bitmap imageBitmap = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);

        init();

        //Todo: start logic to retrieve an image
        /*Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image*//*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image*//*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);*/

        Intent gallery =
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);

        btConfirmYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageBitmap != null){
                    //image has been selected
                    //so send back to calling activity

                    //convert bitmap to byte array
                    byte[] imageArray = ImageConversionUtil.convertPhotoToByteArray(imageBitmap);
                    //sending data back
                    Intent sendDataIntent = new Intent();//we could have used getIntent() in place of new Intent
                    //binding result to the intent
                    sendDataIntent.putExtra("imageArray", imageArray);
                    //send it through setResult
                    setResult(RESULT_OK,sendDataIntent);
                    finish();//Disposes of this activity after working
                }
            }
        });

        btConfirmNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void init() {
        ivImageToSend = (ImageView) findViewById(R.id.ivImageToSend);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ivImageToSend.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                ivImageToSend.setImageURI(imageUri);
            }

        }

    }
}
