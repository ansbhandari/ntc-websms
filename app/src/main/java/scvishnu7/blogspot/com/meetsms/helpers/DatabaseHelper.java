package scvishnu7.blogspot.com.meetsms.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;

import scvishnu7.blogspot.com.meetsms.models.Message;

/**
 * Created by vishnu on 12/24/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DBNAME="sms_history.db";
    private static final int DBVERSION=1;

    private static final String TBL_NAME_HISTORY="history";
    public static final String KEY_ID="_id";
    public static final String KEY_TO="recipient";
    public static final String KEY_TIME="time";
    public static final String KEY_MSG="msg";
    public static final String KEY_STATUS="status";

    private Context context;


    public DatabaseHelper(Context context){
        super(context, DBNAME,null, DBVERSION);
        this.context = context;
    }
    private void createHistoryTable(SQLiteDatabase db){
        db.execSQL("CREATE TABLE "+TBL_NAME_HISTORY+" ("+
        KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
        KEY_MSG+" TEXT,"+
        KEY_TO+" TEXT,"+
        KEY_TIME+" TEXT,"+
        KEY_STATUS+" TEXT );");
    }


    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
            createHistoryTable(db);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long addHistory(String msg, String to, boolean Status){
        Message message = new Message();
        String timeMili= Calendar.getInstance().getTimeInMillis()+"";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MSG,msg);
        values.put(KEY_TO,to);
        values.put(KEY_TIME,timeMili );
        long rowId = db.insert(TBL_NAME_HISTORY, null, values);
        db.close();
       return rowId;
    }

    public ArrayList<Message> getAllHistory() {
        ArrayList<Message> msgList = new ArrayList<Message>();

        String selectQuery = "SELECT * FROM " + TBL_NAME_HISTORY +" ORDER BY "+KEY_ID+" DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Message msg = new Message();
                msg.id = cursor.getInt(0);
                msg.msg = cursor.getString(1);
                msg.to = cursor.getString(2);
                msg.date = cursor.getString(3);
                msg.status = cursor.getInt(4) == 1;

                msgList.add(msg);

            } while (cursor.moveToNext());

        }
        return msgList;
    }

    public boolean deleteHistory(Message msg){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TBL_NAME_HISTORY,KEY_ID + " = "+msg.id,null);
        db.close();
        return true;
    }



}
