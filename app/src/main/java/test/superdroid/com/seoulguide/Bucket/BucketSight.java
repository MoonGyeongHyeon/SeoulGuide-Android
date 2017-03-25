package test.superdroid.com.seoulguide.Bucket;

import android.graphics.Bitmap;

public class BucketSight {
    private Bitmap mSightImage;
    private String mSightName;
    private int mSightId;
    private double mLocationX;
    private double mLocationY;

    public Bitmap getSightImage() {
        return mSightImage;
    }

    public void setSightImage(Bitmap mSightImage) {
        this.mSightImage = mSightImage;
    }

    public String getSightName() {
        return mSightName;
    }

    public void setSightName(String mSightName) {
        this.mSightName = mSightName;
    }

    public int getSightId() {
        return mSightId;
    }

    public void setSightId(int mSightId) {
        this.mSightId = mSightId;
    }

    public double getLocationX() {
        return mLocationX;
    }

    public void setLocationX(double mLocationX) {
        this.mLocationX = mLocationX;
    }

    public double getLocationY() {
        return mLocationY;
    }

    public void setLocationY(double mLocationY) {
        this.mLocationY = mLocationY;
    }
}
