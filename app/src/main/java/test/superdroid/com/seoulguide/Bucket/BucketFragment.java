package test.superdroid.com.seoulguide.Bucket;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import test.superdroid.com.seoulguide.R;

public class BucketFragment extends Fragment {
    private BucketInnerDB mBucketInnerDB;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBucketInnerDB = BucketInnerDB.getIntance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_bucket, container, false);

        if(mBucketInnerDB.isEmpty()) {
            Log.d("LOG/Bucket", "Bucket is empty");
            getFragmentManager().beginTransaction().replace(R.id.bucketMainLayout, new EmptySightFragment()).commit();
        } else {
            Log.d("LOG/Bucket", "Bucket is not empty");
            getFragmentManager().beginTransaction().replace(R.id.bucketMainLayout, new BucketListFragment()).commit();
        }

        return layout;
    }
}
