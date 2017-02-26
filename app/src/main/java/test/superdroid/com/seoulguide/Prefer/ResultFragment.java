package test.superdroid.com.seoulguide.Prefer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
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
    private String[] mTagIds;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mFinalResultIndex = bundle.getString("finalResultIndex");

        Log.d("LOG/Prefer", mFinalResultIndex);

        mTagIds = getTagIdsByIndex(mFinalResultIndex);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("테스트를 다시 진행하시겠습니까? 기존의 데이터는 삭제됩니다.")
                        .setNegativeButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getFragmentManager().beginTransaction().replace(R.id.preferMainLayout, new TestOneFragment()).addToBackStack(null).commit();
                            }
                        })
                        .setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        Button goToSightListButton = (Button)layout.findViewById(R.id.preferGoToSightListButton);
        goToSightListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 여행지 리스트 보기.
                Fragment fragment = new SightListFragment();
                Bundle bundle = new Bundle();
                bundle.putStringArray("tagIds", mTagIds);
                fragment.setArguments(bundle);

                getFragmentManager().beginTransaction().replace(R.id.preferMainLayout, fragment).addToBackStack(null).commit();
            }
        });

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        // 뒤로가기 등으로 다시 태그 프래그먼트에 돌아왔을 때 툴바 타이틀을 복구시킨다.
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.mainToolbar);
        toolbar.setTitle("사용자 성향");
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

    private String[] getTagIdsByIndex(String index) {
        switch (index) {
            case "ES":
                return new String[] {"6", "10", "17"};
            case "EN":
                return new String[] {"9", "12", "15"};
            case "IS":
                return new String[] {"7", "11", "16"};
            case "IN":
                return new String[] {"8", "13", "14"};
            default:
                return null;
        }
    }
}