package test.superdroid.com.seoulguide.Tag;

import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

import test.superdroid.com.seoulguide.R;

public class TagFragment extends Fragment {
    private RecyclerView mSightRecyclerView;
    private LinearLayout mMoreLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_tag, container, false);

        mSightRecyclerView = (RecyclerView) layout.findViewById(R.id.tagSightRecyclerView);
        mMoreLayout = (LinearLayout) layout.findViewById(R.id.tagMoreLayout);

        initCheckBox(layout);

        return layout;
    }

    private void initCheckBox(View layout) {
        CheckBox checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryAllCheckBox);
        checkBox.setButtonDrawable(new StateListDrawable());

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox1);
        checkBox.setButtonDrawable(new StateListDrawable());

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox2);
        checkBox.setButtonDrawable(new StateListDrawable());

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox3);
        checkBox.setButtonDrawable(new StateListDrawable());

        checkBox = (CheckBox) layout.findViewById(R.id.tagMoreCheckBox);
        checkBox.setButtonDrawable(new StateListDrawable());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mMoreLayout.setVisibility(View.VISIBLE);
                    mSightRecyclerView.setVisibility(View.GONE);
                } else {
                    mMoreLayout.setVisibility(View.GONE);
                    mSightRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox4);
        checkBox.setButtonDrawable(new StateListDrawable());

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox5);
        checkBox.setButtonDrawable(new StateListDrawable());

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox6);
        checkBox.setButtonDrawable(new StateListDrawable());

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox7);
        checkBox.setButtonDrawable(new StateListDrawable());

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox8);
        checkBox.setButtonDrawable(new StateListDrawable());
    }
}
