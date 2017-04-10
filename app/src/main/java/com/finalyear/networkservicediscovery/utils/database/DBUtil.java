package com.finalyear.networkservicediscovery.utils.database;

/**
 * Created by KayO on 29/12/2016.
 * t for TEXT
 * b for BLOB
 * i for INTEGER
 */
public class DBUtil {
    public static final String DB_NAME = "intranet_messenger_db.db";

    //contact table
    public static final String CONTACT_TABLE = "contact_tbl";
    public static final String COLUMN_CONTACT_ID = "i_contact_id";
    public static final String COLUMN_CONTACT_NUMBER = "t_contact_number";
    public static final String COLUMN_CONTACT_NAME = "t_contact_name";
    public static final String COLUMN_CONTACT_LAST_MESSAGE = "t_contact_last_message";
    public static final String COLUMN_CONTACT_IMAGE = "b_contact_image";

    //message log table
    public static final String MESSAGE_LOG_TABLE = "message_log_tbl";
    public static final String COLUMN_MESSAGE_ID = "i_message_id";
    public static final String COLUMN_MESSAGE_CONTENT = "t_message_content";
    public static final String COLUMN_MESSAGE_SENDER = "t_message_sender";
    public static final String COLUMN_MESSAGE_RECIPIENT = "t_message_recipient";
    public static final String COLUMN_MESSAGE_TIME = "t_message_time_sent";
    public static final String COLUMN_MESSAGE_SENT_STATUS = "i_message_sent_status";//1 for sent(true), 0 for not sent (false)
    public static final String COLUMN_MESSAGE_RECEIVED_STATUS = "i_message_received_status";//received messages go to the left, sent go to the right on the chat screen

    //local app info table
    //this table is to check for first time runs and keep information like my service_name (phone number) for use in the app
    //This table is meant to work with only one row
    public static final String APP_INFO_TABLE = "app_info_tbl";
    public static final String COLUMN_INFO_FIRST_RUN = "i_first_run_state";//1 for FRIST RUN(true), 0 for false
    public static final String COLUMN_INFO_LOCAL_IDENTITY = "t_identity";
    public static final String COLUMN_INFO_ID = "i_info_id";//we need this to use for updates

    //current database version
    public static final int DB_VERSION = 2;//MAKE CHANGES AS NECESSARY WHEN APP IS IN PRODUCTION

    //create tables
    public static final String  CREATE_CONTACT_TABLE =
            "CREATE TABLE " + CONTACT_TABLE + " (" +
                    COLUMN_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    COLUMN_CONTACT_NUMBER + " TEXT, "+
                    COLUMN_CONTACT_NAME + " TEXT, "+
                    COLUMN_CONTACT_LAST_MESSAGE + " TEXT, "+
                    COLUMN_CONTACT_IMAGE + " BLOB ) ";

    public static final String CREATE_MESSAGE_LOG_TABLE =
            "CREATE TABLE " + MESSAGE_LOG_TABLE + " (" +
                    COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    COLUMN_MESSAGE_CONTENT + " TEXT, "+
                    COLUMN_MESSAGE_SENDER + " TEXT, "+//me or recipient
                    COLUMN_MESSAGE_RECIPIENT + " TEXT, "+//me or recipient
                    COLUMN_MESSAGE_TIME + " TEXT, "+//time will always be now. No delay in serverless architecture
                    COLUMN_MESSAGE_SENT_STATUS + " INTEGER, "+//will become redundant 09/04/2017
                    COLUMN_MESSAGE_RECEIVED_STATUS + " INTEGER ) ";//will become redundant 09/04/2017

    public static final String CREATE_APP_INFO_TABLE =
            "CREATE TABLE " + APP_INFO_TABLE + " (" +
                    COLUMN_INFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    COLUMN_INFO_FIRST_RUN + " INTEGER, "+
                    COLUMN_INFO_LOCAL_IDENTITY + " TEXT ) ";
}
