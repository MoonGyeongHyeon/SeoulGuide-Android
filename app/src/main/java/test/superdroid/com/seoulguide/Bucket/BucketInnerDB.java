package test.superdroid.com.seoulguide.Bucket;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BucketInnerDB extends SQLiteOpenHelper {
    private static BucketInnerDB sInstance = null;
    private static String sDBName = "SEOUL_GUIDE_BUCKET.db";
    private static int sVersion = 7;
    private String mTableName = "BUCKET";

    private BucketInnerDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static BucketInnerDB getIntance(Context context) {
        if(sInstance == null) {
            sInstance = new BucketInnerDB(context, sDBName, null, sVersion);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + mTableName + "(" +
                "_bitmap BLOB, " +
                "_sight_name TEXT, " +
                "_sight_id INTEGER, " +
                "_sight_x REAL, " +
                "_sight_y REAL" +
                ");");
        Log.d("LOG/Bucket", "Inner DB is created");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            Log.d("LOG/Bucket", "Inner DB is upgraded");
            db.execSQL("DROP TABLE IF EXISTS " + mTableName);
            onCreate(db);
        }
    }

    public void insert(ContentValues values) {
        getWritableDatabase().insert(mTableName, null, values);
        Log.d("LOG/Bucket", "Sight "+ values.get("_sight_name") +" is inserted");
    }
    public boolean isEmpty() {
        Cursor c = getReadableDatabase().rawQuery("select * from " + mTableName, null);
        boolean isEmpty = c.getCount() == 0;
        c.close();
        return isEmpty;
    }
    public List<BucketSight> selectSightList() {
        // 저장된 버킷리스트를 담을 리스트 생성
        List<BucketSight> bucketSightList = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery("select * from " + mTableName, null);

        while (c.moveToNext()) {
            // 버킷리스트 하나의 객체 생성
            BucketSight bucketSight = new BucketSight();
            // 여행지의 아이디 가져옴
            bucketSight.setSightId(c.getInt(c.getColumnIndex("_sight_id")));
            // 여행지의 이름 가져옴
            bucketSight.setSightName(c.getString(c.getColumnIndex("_sight_name")));
            // 여행지의 X 좌표를 가져옴
            bucketSight.setLocationX(c.getDouble(c.getColumnIndex("_sight_x")));
            // 여행지의 Y 좌표를 가져옴
            bucketSight.setLocationY(c.getDouble(c.getColumnIndex("_sight_y")));

            // 현재 DB에는 이미지가 BLOB 형태로 저장되어 있기 때문에 Bitmap 형태로 바꿔주어야 한다.
            // 일단 Bytes를 가져온다.
            byte[] imageBytes = c.getBlob(c.getColumnIndex("_bitmap"));
            // Bytes를 BitmapFactory 클래스를 이용하여 Bitmap으로 변환한다.
            Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            // 여행지의 이미지를 가져옴
            bucketSight.setSightImage(image);
            // 완성된 버킷리스트를 리스트에 추가
            bucketSightList.add(bucketSight);
        }
        c.close();
        return bucketSightList;
    }
    public boolean isExistedSight(int sightId) {
        Cursor c = getReadableDatabase().rawQuery("select _sight_id from " + mTableName + " where _sight_id = ?",
                new String[] {String.valueOf(sightId)});
        // 존재한다면 true, 존재하지 않는다면 false
        return c.moveToNext();
    }

    public void delete(int sightId) {
        getWritableDatabase().delete(mTableName,"_sight_id=?", new String[] {String.valueOf(sightId)} );
        Log.d("LOG/Bucket", "Remove bucket sight : " + sightId);
    }
}
