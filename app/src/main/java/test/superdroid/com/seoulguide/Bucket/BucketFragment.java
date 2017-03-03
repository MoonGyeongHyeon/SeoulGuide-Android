package test.superdroid.com.seoulguide.Bucket;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import test.superdroid.com.seoulguide.R;

public class BucketFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_bucket, container, false);

        if(true) {
            getFragmentManager().beginTransaction().replace(R.id.bucketMainLayout, new EmptySightFragment()).commit();
        } else {
            getFragmentManager().beginTransaction().replace(R.id.bucketMainLayout, new BucketListFragment()).commit();
        }

        return layout;
    }
}
