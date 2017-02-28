package test.superdroid.com.seoulguide.Prefer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ResultDataInnerDB extends SQLiteOpenHelper {
    private static ResultDataInnerDB sInstance = null;
    private static String sDBName = "PREFER.db";
    private static int sVersion = 1;
    private String mTableName = "PREFER";

    private ResultDataInnerDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static ResultDataInnerDB getIntance(Context context) {
        if(sInstance == null) {
            sInstance = new ResultDataInnerDB(context, sDBName, null, sVersion);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + mTableName + "(" +
                "_index TEXT" +
                ");");
        Log.d("LOG/Prefer", "Inner DB is created");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insert(ContentValues values) {
        getWritableDatabase().insert(mTableName, null, values);
        Log.d("LOG/Prefer", "index is inserted");
    }
    public boolean isEmpty() {
        Cursor c = getReadableDatabase().rawQuery("select * from " + mTableName, null);
        return c.getCount() == 0;
    }
    public String selectIndex() {
        Cursor c = getReadableDatabase().rawQuery("select _index from " + mTableName, null);
        c.moveToNext();
        String index = c.getString(0);
        return index;
    }
    public void delete() {
        getWritableDatabase().delete(mTableName, null, null);
        Log.d("LOG/Prefer", "index is deleted");
    }
}
