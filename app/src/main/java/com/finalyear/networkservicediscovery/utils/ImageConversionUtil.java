package com.finalyear.networkservicediscovery.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Kwadwo Agyapon-Ntra on 17/08/2015.
 */
public class ImageConversionUtil {
    public static final int CAMERA_REQUEST_CODE=10;

    public static byte[] convertPhotoToByteArray(Bitmap photo){
        ByteArrayOutputStream bAOutStream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 50, bAOutStream);
        byte[] imageData = bAOutStream.toByteArray();
        return imageData;
    }

    public static Bitmap convertByteArrayToPhoto(byte[] imageData){
        ByteArrayInputStream bAInStream = new ByteArrayInputStream(imageData);
        return BitmapFactory.decodeStream(bAInStream);
    }
}
