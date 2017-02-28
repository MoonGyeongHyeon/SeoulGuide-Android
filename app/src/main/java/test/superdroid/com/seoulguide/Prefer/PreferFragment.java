package test.superdroid.com.seoulguide.Prefer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import test.superdroid.com.seoulguide.R;

public class PreferFragment extends Fragment {

    private ResultDataInnerDB mInnerDB;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInnerDB = ResultDataInnerDB.getIntance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_prefer, container, false);

        if(mInnerDB.isEmpty()) {
            Log.d("LOG/Prefer", "Result is nothing");
            getFragmentManager().beginTransaction().replace(R.id.preferMainLayout, new NothingResultFragment()).commit();
        } else {
            Log.d("LOG/Prefer", "Result is existed");
            Fragment fragment = new ResultFragment();
            Bundle bundle = new Bundle();
            bundle.putString("finalResultIndex", mInnerDB.selectIndex());
            fragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.preferMainLayout, fragment).commit();
        }

        return layout;
    }
}
