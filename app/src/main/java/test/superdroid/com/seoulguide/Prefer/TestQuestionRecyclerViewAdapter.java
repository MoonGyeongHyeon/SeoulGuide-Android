package test.superdroid.com.seoulguide.Prefer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import test.superdroid.com.seoulguide.R;

public class TestQuestionRecyclerViewAdapter extends RecyclerView.Adapter<TestQuestionRecyclerViewAdapter.ViewHolder> {

    private List<String> mQuestionList;
    private int mItemLayout;
    private int[] mAnswerList;

    public int[] getmAnswerList() {
        return mAnswerList;
    }

    public TestQuestionRecyclerViewAdapter(List<String> mQuestionList, int mItemLayout) {
        this.mQuestionList = mQuestionList;
        this.mItemLayout = mItemLayout;
        mAnswerList = new int[10];
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mItemLayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;

        String question = mQuestionList.get(position);
        RadioGroup radioGroup = holder.mRadioGroup;
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mAnswerList[pos] = checkedId;
            }
        });

        if(mAnswerList[pos] != 0) {
            radioGroup.check(mAnswerList[pos]);
        } else {
            radioGroup.clearCheck();
        }

        holder.mQuestionTextView.setText(question);
    }

    @Override
    public int getItemCount() {
        return mQuestionList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mQuestionTextView;
        private RadioGroup mRadioGroup;

        public ViewHolder(View itemView) {
            super(itemView);
            mQuestionTextView = (TextView) itemView.findViewById(R.id.preferTestQuestionTextView);
            mRadioGroup = (RadioGroup) itemView.findViewById(R.id.preferRadioGroup);
        }
    }
}
