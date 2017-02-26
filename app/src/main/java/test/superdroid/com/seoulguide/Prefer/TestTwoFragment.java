package test.superdroid.com.seoulguide.Prefer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import test.superdroid.com.seoulguide.R;

public class TestTwoFragment extends Fragment {
    private int mResultOne;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mResultOne = bundle.getInt("resultOne");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_prefer_test_two, container, false);

        List<String> questionList = loadQuestion();

        TestQuestionRecyclerViewAdapter adapter = new TestQuestionRecyclerViewAdapter(questionList, R.layout.i_prefer_test_question);

        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.preferTestTwoRecyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        final int[] answerList = adapter.getmAnswerList();

        Button OKButton = (Button) layout.findViewById(R.id.preferNextTestButton);
        OKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int resultTwo = getAnswerValue(answerList);
                String finalResultIndex = getFinalResultIndex(mResultOne, resultTwo);

                Fragment fragment = new ResultFragment();
                Bundle bundle = new Bundle();
                bundle.putString("finalResultIndex", finalResultIndex);
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = getFragmentManager();
                // 스택 비우기.
                for(int i=0; i<fragmentManager.getBackStackEntryCount(); i++)
                    fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                fragmentManager.beginTransaction().replace(R.id.preferMainLayout, fragment).commit();
            }
        });

        return layout;
    }

    private List<String> loadQuestion() {
        List<String> list = new ArrayList<>();

        InputStream is = getResources().openRawResource(R.raw.prefer_test_two);
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private int getAnswerValue(int[] answerList) {
        int sum = 0;
        for(int i=0; i<answerList.length; i++) {
            switch (answerList[i]) {
                case R.id.preferVeryYesRadioButton:
                    sum += 5;
                    break;
                case R.id.preferYesRadioButton:
                    sum += 4;
                    break;
                case R.id.preferSosoRadioButton:
                    sum += 3;
                    break;
                case R.id.preferNoRadioButton:
                    sum += 2;
                    break;
                case R.id.preferVeryNoRadioButton:
                    sum += 1;
                    break;
            }
        }
        return sum;
    }

    private String getFinalResultIndex(int resultOne, int resultTwo) {
        String finalResultIndex = "";

        if(resultOne > 30) {
            finalResultIndex += "E";
        } else {
            finalResultIndex += "I";
        }
        if(resultTwo > 27) {
            finalResultIndex += "N";
        } else {
            finalResultIndex += "S";
        }

        return finalResultIndex;
    }
}
