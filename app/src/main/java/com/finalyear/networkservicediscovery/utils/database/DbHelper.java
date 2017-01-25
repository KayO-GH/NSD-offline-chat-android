package com.finalyear.networkservicediscovery.utils.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by KayO on 29/12/2016.
 */
public class DbHelper extends SQLiteOpenHelper {
    public DbHelper(Context context) {
        super(context, DBUtil.DB_NAME, null, DBUtil.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DBUtil.CREATE_CONTACT_TABLE);
        sqLiteDatabase.execSQL(DBUtil.CREATE_MESSAGE_LOG_TABLE);
        sqLiteDatabase.execSQL(DBUtil.CREATE_APP_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if(oldVersion==DBUtil.DB_VERSION) {//HANDLES MODIFICATIONS TO DATABASE
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + DBUtil.CONTACT_TABLE + "'");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + DBUtil.MESSAGE_LOG_TABLE + "'");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + DBUtil.APP_INFO_TABLE + "'");
            onCreate(sqLiteDatabase);
        }
    }
}
