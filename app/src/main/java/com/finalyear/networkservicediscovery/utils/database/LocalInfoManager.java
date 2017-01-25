package com.finalyear.networkservicediscovery.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.finalyear.networkservicediscovery.pojos.LocalInfo;

import java.util.ArrayList;

/**
 * Created by KayO on 29/12/2016.
 */
public class LocalInfoManager {
    private SQLiteDatabase db;
    private Context context;
    private DbHelper dbHelper;
    private ArrayList<LocalInfo> allLocalInfo;
    private ContentValues cv;
    private Cursor cr;
    private LocalInfo localInfo;

    private long rowsEffected;
    private boolean isOk = false;
    private String whereConditions;
    private String[] whereArgs;

    //constructor
    public LocalInfoManager(Context context) {
        this.context = context;
        dbHelper = new DbHelper(this.context);//creates tables
        cv = new ContentValues();
        allLocalInfo = new ArrayList<>();
    }

    //columns under localInfo
    private String[] columns = new String[]{
            DBUtil.COLUMN_INFO_ID,
            DBUtil.COLUMN_INFO_FIRST_RUN,
            DBUtil.COLUMN_INFO_LOCAL_IDENTITY
    };

    private void openForRead() {
        db = dbHelper.getReadableDatabase();
    }

    private void openForWrite() {
        db = dbHelper.getWritableDatabase();
    }

    private void close() {
        db.close();
        dbHelper.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //PERFORM CRUD
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean createLocalInfo(LocalInfo localInfo) {
        openForWrite();
        cv.put(DBUtil.COLUMN_INFO_LOCAL_IDENTITY, localInfo.getIdentity());
        cv.put(DBUtil.COLUMN_INFO_FIRST_RUN, localInfo.isFirstTime() ? 1 : 0);//1 if first run, 0 if not

        rowsEffected = db.insert(DBUtil.APP_INFO_TABLE, null, cv);
        if (rowsEffected != -1)//everything is OK
            isOk = true;

        close();
        return isOk;
    }

    public boolean updateLocalInfo(LocalInfo localInfo) {
        /*
        * There should only be one instance ever in the table
        * update only the identity
        * we need an ID as the key
        */
        openForWrite();
        whereConditions = DBUtil.COLUMN_INFO_ID + "=?";
        whereArgs = new String[]{String.valueOf(localInfo.getInfoID())};

        cv.put(DBUtil.COLUMN_INFO_LOCAL_IDENTITY, localInfo.getIdentity());
        cv.put(DBUtil.COLUMN_INFO_FIRST_RUN, 0);//straight 0 cannot update on your first run

        rowsEffected = db.update(DBUtil.APP_INFO_TABLE, cv, whereConditions, whereArgs);
        if (rowsEffected != 0) {
            isOk = true;
        }

        close();
        return isOk;
    }

    public boolean deleteLocalInfo(LocalInfo localInfo) {
        openForWrite();
        //USING THE ID AS A KEY FOR DELETING TOO
        whereConditions = DBUtil.COLUMN_INFO_ID + "=?";
        whereArgs = new String[]{String.valueOf(localInfo.getInfoID())};

        rowsEffected = db.delete(DBUtil.APP_INFO_TABLE, whereConditions, whereArgs);
        if (rowsEffected != 0) {
            isOk = true;
        }
        close();
        return isOk;
    }


    //column indexes
    private int iID, iLocalIdentity, iFirstRun;
    
    public LocalInfo getLocalInfo() {
        openForRead();
        cr = db.query(DBUtil.APP_INFO_TABLE, columns, null, null, null, null, null, "1");

        iID = cr.getColumnIndex(DBUtil.COLUMN_INFO_ID);
        iLocalIdentity = cr.getColumnIndex(DBUtil.COLUMN_INFO_LOCAL_IDENTITY);
        iFirstRun = cr.getColumnIndex(DBUtil.COLUMN_INFO_FIRST_RUN);

        if(cr.getCount()>0){//there's something in the table
            cr.moveToFirst();//move to first item
            localInfo = new LocalInfo(
                    //to cast to boolean, first cast to string then use Boolean.valueOf() method
                    Boolean.valueOf(String.valueOf(cr.getInt(iFirstRun))),
                    cr.getString(iLocalIdentity),
                    cr.getInt(iID)
            );
        }else{//there's nothing in the table
            localInfo = new LocalInfo(//hardcode a true for the first time run
                    true,
                    null,
                    0//hardcoded zero...which will naturally be replaced upon registration
            );
        }

        close();
        return localInfo;
    }
}
