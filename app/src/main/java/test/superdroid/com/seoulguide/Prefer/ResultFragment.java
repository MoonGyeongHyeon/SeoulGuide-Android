package test.superdroid.com.seoulguide.Prefer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import test.superdroid.com.seoulguide.R;

public class ResultFragment extends Fragment {

    private String mFinalResultIndex;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mFinalResultIndex = bundle.getString("finalResultIndex");

        Log.d("LOG/Prefer", mFinalResultIndex);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_prefer_result, container, false);

        String[] results = getResultTextByIndex(mFinalResultIndex);
        int resId = getResultImageIdByIndex(mFinalResultIndex);

        ImageView resultImageView = (ImageView) layout.findViewById(R.id.preferResultImageView);
        resultImageView.setImageResource(resId);

        TextView resultTitleTextView = (TextView) layout.findViewById(R.id.preferResultTitleTextView);
        resultTitleTextView.setText(results[0]);

        TextView resultContentTextView = (TextView) layout.findViewById(R.id.preferResultContentTextView);
        resultContentTextView.setText(results[1]);

        Button retestButton = (Button)layout.findViewById(R.id.preferRetestButton);
        retestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다시하기.
            }
        });
        Button goToSightListButton = (Button)layout.findViewById(R.id.preferGoToSightListButton);
        goToSightListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 여행지 리스트 보기.
            }
        });

        return layout;
    }

    private String[] getResultTextByIndex(String index) {
        String[] results = new String[2];
        InputStream is = getResources().openRawResource(R.raw.prefer_result);
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while(true) {
                if(bufferedReader.readLine().equals(index)) {
                    // 결과 제목
                    results[0] = bufferedReader.readLine();
                    // 결과 내용
                    results[1] = bufferedReader.readLine();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
    private int getResultImageIdByIndex(String index) {
        int resId = 0;
        switch (index) {
            case "ES":
                resId = R.drawable.prefer_es;
                break;
            case "EN":
                resId = R.drawable.prefer_en;
                break;
            case "IS":
                resId = R.drawable.prefer_is;
                break;
            case "IN":
                resId = R.drawable.prefer_in;
                break;
        }
        return resId;
    }
}