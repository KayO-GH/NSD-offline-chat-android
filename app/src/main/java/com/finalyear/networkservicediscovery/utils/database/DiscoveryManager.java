package com.finalyear.networkservicediscovery.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.finalyear.networkservicediscovery.pojos.Contact;

import java.util.ArrayList;

/**
 * Created by KayO on 29/12/2016.
 */
public class DiscoveryManager {

    private SQLiteDatabase db;
    private Context context;
    private DbHelper dbHelper;
    private ArrayList<Contact>allContacts;
    private ContentValues cv;
    private Cursor cr;
    private Contact contact;

    private long  rowsEffected;
    private boolean isOk = false;
    private String whereConditions;
    private String[] whereArgs;

    //constructor
    public DiscoveryManager(Context context) {
        this.context = context;
        dbHelper = new DbHelper(this.context);//creates tables
        cv = new ContentValues();
        allContacts = new ArrayList<>();
    }

    //column indexes
    private int iID, iNumber, iName, iLastMessage, iImage;
    
    //columns under contacts
    private String[] columns = new String[]{
            DBUtil.COLUMN_CONTACT_ID,
            DBUtil.COLUMN_CONTACT_NUMBER,
            DBUtil.COLUMN_CONTACT_NAME,
            DBUtil.COLUMN_CONTACT_LAST_MESSAGE,
            DBUtil.COLUMN_CONTACT_IMAGE
    };
    
    private void openForRead(){
        db = dbHelper.getReadableDatabase();
    }
    
    private void openForWrite(){
        db = dbHelper.getWritableDatabase();
    }
    
    private void close(){
        db.close();
        dbHelper.close();
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //PERFORM CRUD
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean createContact(Contact contact){
        openForWrite();
        cv.put(DBUtil.COLUMN_CONTACT_NUMBER,contact.getPhoneNumber());
        cv.put(DBUtil.COLUMN_CONTACT_NAME,contact.getName());
        cv.put(DBUtil.COLUMN_CONTACT_LAST_MESSAGE,contact.getLastMessage());
        cv.put(DBUtil.COLUMN_CONTACT_IMAGE,contact.getImage());
        
        rowsEffected = db.insert(DBUtil.CONTACT_TABLE, null, cv);
        if(rowsEffected != -1)//everything is OK
            isOk = true;
        
        close();
        return isOk;
    }

    public boolean updateContact(Contact contact){
        /*
        * update contact_tbl where contact_number=?
        */
        openForWrite();
        whereConditions = DBUtil.COLUMN_CONTACT_NUMBER+"=?";
        whereArgs = new String[]{String.valueOf(contact.getPhoneNumber().trim())};

        cv.put(DBUtil.COLUMN_CONTACT_NUMBER,contact.getPhoneNumber());
        cv.put(DBUtil.COLUMN_CONTACT_NAME,contact.getName());
        cv.put(DBUtil.COLUMN_CONTACT_LAST_MESSAGE,contact.getLastMessage());
        cv.put(DBUtil.COLUMN_CONTACT_IMAGE,contact.getImage());

        rowsEffected = db.update(DBUtil.CONTACT_TABLE, cv, whereConditions, whereArgs);
        if (rowsEffected!=0){
            isOk=true;
        }

        close();
        return isOk;
    }

    public boolean deleteContact(Contact contact){
        openForWrite();
        whereConditions = DBUtil.COLUMN_CONTACT_NUMBER+"=?";
        whereArgs = new String[]{String.valueOf(contact.getPhoneNumber().trim())};

        rowsEffected = db.delete(DBUtil.CONTACT_TABLE, whereConditions, whereArgs);
        if (rowsEffected!=0){
            isOk=true;
        }
        close();
        return isOk;
    }
    
    //We'll have to get Contacts specifically by their phone numbers

    //getting contacts SPECIFICALLY BY PHONE NUMBER
    public Contact getContactByNumber(Contact contact){
        openForRead();
        whereConditions = DBUtil.COLUMN_CONTACT_NUMBER+ "=?" ;
        whereArgs = new String[]{contact.getPhoneNumber().trim()};

        cr = db.query(DBUtil.CONTACT_TABLE, columns, whereConditions, whereArgs, null, null, null);
        iID = cr.getColumnIndex(DBUtil.COLUMN_CONTACT_ID);
        iNumber = cr.getColumnIndex(DBUtil.COLUMN_CONTACT_NUMBER);
        iName = cr.getColumnIndex(DBUtil.COLUMN_CONTACT_NAME);
        iLastMessage = cr.getColumnIndex(DBUtil.COLUMN_CONTACT_LAST_MESSAGE);
        iImage = cr.getColumnIndex(DBUtil.COLUMN_CONTACT_IMAGE);

        if(cr.getCount()<1)
            return null;

        if (cr!=null){
            cr.moveToFirst();
            this.contact = new Contact(
                    cr.getString(iName),
                    cr.getBlob(iImage),
                    cr.getString(iLastMessage),
                    cr.getString(iNumber)
            );
        }

        close();
        return this.contact;
    }

    public ArrayList<Contact>getAllContacts(){
        //To start with, we want to find people online
        // before comparing to find out if they are in the contact database
        openForRead();
        cr = db.query(DBUtil.CONTACT_TABLE, columns, null, null, null, null, null);

        iID = cr.getColumnIndex(DBUtil.COLUMN_CONTACT_ID);
        iNumber = cr.getColumnIndex(DBUtil.COLUMN_CONTACT_NUMBER);
        iName = cr.getColumnIndex(DBUtil.COLUMN_CONTACT_NAME);
        iLastMessage = cr.getColumnIndex(DBUtil.COLUMN_CONTACT_LAST_MESSAGE);
        iImage = cr.getColumnIndex(DBUtil.COLUMN_CONTACT_IMAGE);

        for (cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()){
            contact = new Contact(
                    cr.getString(iName),
                    cr.getBlob(iImage),
                    cr.getString(iLastMessage),
                    cr.getString(iNumber)
            );
            allContacts.add(contact);
        }

        close();
        return allContacts;
    }
}
