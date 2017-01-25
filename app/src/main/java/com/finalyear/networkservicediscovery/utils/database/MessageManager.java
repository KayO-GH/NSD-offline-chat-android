package com.finalyear.networkservicediscovery.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.finalyear.networkservicediscovery.pojos.ChatMessage;

import java.util.ArrayList;

/**
 * Created by KayO on 29/12/2016.
 */
public class MessageManager {
    private SQLiteDatabase db;
    private Context context;
    private DbHelper dbHelper;
    private ArrayList<ChatMessage> allChatMessages;
    private ContentValues cv;
    private Cursor cr;
    private ChatMessage chatMessage;

    private long rowsEffected;
    private boolean isOk = false;
    private String whereConditions;
    private String[] whereArgs;

    //constructor
    public MessageManager(Context context) {
        this.context = context;
        dbHelper = new DbHelper(this.context);//creates tables
        cv = new ContentValues();
        allChatMessages = new ArrayList<>();
    }

    //columns under chatMessages
    private String[] columns = new String[]{
            DBUtil.COLUMN_MESSAGE_ID,
            DBUtil.COLUMN_MESSAGE_CONTENT,
            DBUtil.COLUMN_MESSAGE_SENDER,
            DBUtil.COLUMN_MESSAGE_RECIPIENT,
            DBUtil.COLUMN_MESSAGE_TIME_SENT,
            DBUtil.COLUMN_MESSAGE_TIME_DELIVERED,
            DBUtil.COLUMN_MESSAGE_SENT_STATUS
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
    public boolean createChatMessage(ChatMessage chatMessage) {
        openForWrite();
        cv.put(DBUtil.COLUMN_MESSAGE_CONTENT, chatMessage.getMessageContent());
        cv.put(DBUtil.COLUMN_MESSAGE_SENDER, chatMessage.getSender());
        cv.put(DBUtil.COLUMN_MESSAGE_RECIPIENT, chatMessage.getRecipient());
        cv.put(DBUtil.COLUMN_MESSAGE_TIME_SENT, chatMessage.getTimeSent());
        cv.put(DBUtil.COLUMN_MESSAGE_TIME_DELIVERED, chatMessage.getTimeDelivered());
        cv.put(DBUtil.COLUMN_MESSAGE_SENT_STATUS, chatMessage.isSent() ? 1 : 0);//1 if sent, 0 if not
        cv.put(DBUtil.COLUMN_MESSAGE_SENT_STATUS, chatMessage.isReceived() ? 1 : 0);//1 if received(going to the left), 0 if not

        rowsEffected = db.insert(DBUtil.MESSAGE_LOG_TABLE, null, cv);
        if (rowsEffected != -1)//everything is OK
            isOk = true;

        close();
        return isOk;
    }

    public boolean updateChatMessage(ChatMessage chatMessage) {
        /*
        * update message_log_tbl where message_id=?
        */
        openForWrite();
        whereConditions = DBUtil.COLUMN_MESSAGE_ID + "=?";
        whereArgs = new String[]{String.valueOf(chatMessage.getMessageID())};

        cv.put(DBUtil.COLUMN_MESSAGE_CONTENT, chatMessage.getMessageContent());
        cv.put(DBUtil.COLUMN_MESSAGE_SENDER, chatMessage.getSender());
        cv.put(DBUtil.COLUMN_MESSAGE_RECIPIENT, chatMessage.getRecipient());
        cv.put(DBUtil.COLUMN_MESSAGE_TIME_SENT, chatMessage.getTimeSent());
        cv.put(DBUtil.COLUMN_MESSAGE_TIME_DELIVERED, chatMessage.getTimeDelivered());
        cv.put(DBUtil.COLUMN_MESSAGE_SENT_STATUS, chatMessage.isSent() ? 1 : 0);//1 if sent, 0 if not
        cv.put(DBUtil.COLUMN_MESSAGE_SENT_STATUS, chatMessage.isReceived() ? 1 : 0);//1 if received(going to the left), 0 if not

        rowsEffected = db.update(DBUtil.MESSAGE_LOG_TABLE, cv, whereConditions, whereArgs);
        if (rowsEffected != 0) {
            isOk = true;
        }

        close();
        return isOk;
    }

    public boolean deleteChatMessage(ChatMessage chatMessage) {
        openForWrite();
        whereConditions = DBUtil.COLUMN_MESSAGE_ID + "=?";
        whereArgs = new String[]{String.valueOf(chatMessage.getMessageID())};

        rowsEffected = db.delete(DBUtil.MESSAGE_LOG_TABLE, whereConditions, whereArgs);
        if (rowsEffected != 0) {
            isOk = true;
        }
        close();
        return isOk;
    }

    //We'll have to get ChatMessages specifically by their sender, recipient and time sent
    //these parameters may change for group messages when we implement group chat

    //column indexes
    private int iID, iContent, iSender, iRecipient, iTimeSent, iTimeDelivered, iSent, iReceived;

    public ChatMessage getChatMessageByNumber(ChatMessage chatMessage) {
        openForRead();
        whereConditions = DBUtil.COLUMN_MESSAGE_SENDER+"=? AND "
                +DBUtil.COLUMN_MESSAGE_RECIPIENT+"=? AND "
                +DBUtil.COLUMN_MESSAGE_TIME_SENT+"=?";
        whereArgs = new String[]{chatMessage.getSender(),chatMessage.getRecipient(), chatMessage.getTimeSent()};

        cr = db.query(DBUtil.MESSAGE_LOG_TABLE, columns, whereConditions, whereArgs, null, null, null);
        iID = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_ID);
        iContent = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_CONTENT);
        iSender = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_SENDER);
        iRecipient = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_RECIPIENT);
        iTimeSent = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_TIME_SENT);
        iTimeDelivered = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_TIME_DELIVERED);
        iSent = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_SENT_STATUS);
        iReceived = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_RECEIVED_STATUS);

        if (cr.getCount() < 1)
            return null;

        if (cr != null) {
            cr.moveToFirst();
            this.chatMessage = new ChatMessage(
                    cr.getLong(iID),
                    cr.getString(iContent),
                    cr.getString(iSender),
                    cr.getString(iRecipient),
                    cr.getString(iTimeSent),
                    cr.getString(iTimeDelivered),
                    //to cast to boolean, first cast to string then use Boolean.valueOf() method
                    Boolean.valueOf(String.valueOf(cr.getInt(iSent))),
                    Boolean.valueOf(String.valueOf(cr.getInt(iReceived)))
            );
        }

        close();
        return this.chatMessage;
    }

    public ArrayList<ChatMessage> getAllChatMessages() {
        openForRead();
        cr = db.query(DBUtil.MESSAGE_LOG_TABLE, columns, null, null, null, null, null);

        iID = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_ID);
        iContent = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_CONTENT);
        iSender = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_SENDER);
        iRecipient = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_RECIPIENT);
        iTimeSent = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_TIME_SENT);
        iTimeDelivered = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_TIME_DELIVERED);
        iSent = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_SENT_STATUS);
        iReceived = cr.getColumnIndex(DBUtil.COLUMN_MESSAGE_RECEIVED_STATUS);

        for (cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
            chatMessage = new ChatMessage(
                    cr.getLong(iID),
                    cr.getString(iContent),
                    cr.getString(iSender),
                    cr.getString(iRecipient),
                    cr.getString(iTimeSent),
                    cr.getString(iTimeDelivered),
                    //to cast to boolean, first cast to string then use Boolean.valueOf() method
                    Boolean.valueOf(String.valueOf(cr.getInt(iSent))),
                    Boolean.valueOf(String.valueOf(cr.getInt(iReceived)))
            );
            allChatMessages.add(chatMessage);
        }

        close();
        return allChatMessages;
    }
}
