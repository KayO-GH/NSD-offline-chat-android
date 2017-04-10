package com.finalyear.networkservicediscovery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.finalyear.networkservicediscovery.R;

public class SendFileActivity extends AppCompatActivity {

    private static final int PICK_FILE = 100;
    private ImageView ivImageToSend;
    private Button btConfirmYes, btConfirmNo;
    private TextView tvPrompt, tvAudioOrFile;
    private VideoView vvVidToSend;
    Bitmap imageBitmap = null;
    Uri fileUri;
    private static final Integer READ_EXST = 0x4;
    String fileType;
    String queriedPath;
    MediaController vidControl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

        fileType = this.getIntent().getStringExtra("type");


        init();

        //check if we are dealing with Marshmallow or higher
        if(Build.VERSION.SDK_INT >= 23){
            askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);
        }else{
            switch (fileType) {
                case "image":
                    Intent gallery =
                            new Intent(Intent.ACTION_PICK,
                                    //android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(gallery, PICK_FILE);
                    break;
                case "file":
                    //Todo: start logic to retrieve a file
                    Intent chooseFile;
                    Intent intent;
                    chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.setType("file/*");
                    intent = Intent.createChooser(chooseFile, "Choose a file");
                    startActivityForResult(intent, PICK_FILE);
                    break;
                case "video":
                    Intent videosIntent =
                            new Intent(Intent.ACTION_PICK,
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(videosIntent, PICK_FILE);
                    break;
                case "audio":
                    Intent audiosIntent =
                            new Intent(Intent.ACTION_PICK,
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(audiosIntent, PICK_FILE);
                    break;
            }
        }




        btConfirmYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //convert bitmap to byte array
                //TODO byte[] imageArray = ImageConversionUtil.convertPhotoToByteArray(imageBitmap);
                //sending data back
                Intent sendDataIntent = new Intent();//we could have used getIntent() in place of new Intent
                //binding result to the intent
                //TODO sendDataIntent.putExtra("imageArray", imageArray);
                sendDataIntent.putExtra("image_path", queriedPath);
                //send it through setResult
                setResult(RESULT_OK, sendDataIntent);
                finish();//Disposes of this activity after working
                //TODO}
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

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void init() {
        ivImageToSend = (ImageView) findViewById(R.id.ivImageToSend);
        btConfirmYes = (Button) findViewById(R.id.btConfirmYes);
        btConfirmNo = (Button) findViewById(R.id.btConfirmNo);
        tvAudioOrFile = (TextView) findViewById(R.id.tvAudioOrFileToSend);
        tvPrompt = (TextView) findViewById(R.id.tvPrompt);
        vvVidToSend = (VideoView) findViewById(R.id.vvVideoToSend);
        vidControl = new MediaController(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK) {
            fileUri = data.getData();
            /*try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
                ivImageToSend.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                ivImageToSend.setImageURI(fileUri);
            }*/

            if (fileType.equals("file"))
                queriedPath = fileUri.getPath();
            else
                queriedPath = getPath(fileUri);//decode the path from the file URI

            switch (fileType){
                case "image":
                    //set imageView to show selected image, and text view to display appropriately
                    ivImageToSend.setImageURI(fileUri);
                    ivImageToSend.setVisibility(View.VISIBLE);
                    tvPrompt.setText(R.string.image_prompt);
                    break;
                case "video":
                    vvVidToSend.setVideoURI(fileUri);
                    vvVidToSend.setVisibility(View.VISIBLE);
                    vidControl.setAnchorView(vvVidToSend);
                    vvVidToSend.setMediaController(vidControl);
                    tvPrompt.setText(R.string.video_prompt);
                    break;
                case "file":
                    tvAudioOrFile.setText(queriedPath.substring(queriedPath.lastIndexOf('/')+1));//set text to file name
                    tvAudioOrFile.setVisibility(View.VISIBLE);
                    tvPrompt.setText(R.string.file_prompt);
                    break;
                case "audio":
                    tvAudioOrFile.setText(queriedPath.substring(queriedPath.lastIndexOf('/')+1));//set text to audio name
                    tvAudioOrFile.setVisibility(View.VISIBLE);
                    tvPrompt.setText(R.string.audio_prompt);
                    break;
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == READ_EXST){//this test is actually redundant since we are just testing for read permissions
            switch (fileType) {
                case "image":
                    Intent gallery =
                            new Intent(Intent.ACTION_PICK,
                                    //android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(gallery, PICK_FILE);
                    break;
                case "file":
                    //Todo: start logic to retrieve a file
                    Intent chooseFile;
                    Intent intent;
                    chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.setType("file/*");
                    intent = Intent.createChooser(chooseFile, "Choose a file");
                    startActivityForResult(intent, PICK_FILE);
                    break;
                case "video":
                    Intent videosIntent =
                            new Intent(Intent.ACTION_PICK,
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(videosIntent, PICK_FILE);
                    break;
                case "audio":
                    Intent audiosIntent =
                            new Intent(Intent.ACTION_PICK,
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(audiosIntent, PICK_FILE);
                    break;
            }
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(SendFileActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(SendFileActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(SendFileActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }
}
