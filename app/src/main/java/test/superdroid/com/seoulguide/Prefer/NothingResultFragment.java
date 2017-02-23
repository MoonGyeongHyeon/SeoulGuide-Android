package test.superdroid.com.seoulguide.Prefer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import test.superdroid.com.seoulguide.R;

public class NothingResultFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_prefer_nothing, container, false);

        Button goTestButton = (Button) layout.findViewById(R.id.preferTestButton);
        goTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.preferMainLayout, new TestOneFragment()).addToBackStack(null).commit();
            }
        });
        return layout;
    }
}
