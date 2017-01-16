package test.superdroid.com.seoulguide.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import test.superdroid.com.seoulguide.R;

public class WeekSightFragment extends Fragment {

    private List<SightInfo> mList;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_week_sight, container, false);

        initData();

        RankingRecyclerViewAdapter adapter = new RankingRecyclerViewAdapter(mList, R.layout.i_home_ranking);
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.weekRankingRecyclerView);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        return layout;
    }

    private void initData() {
        //TODO DB와 연동 이후엔 DB를 통해 데이터를 가져와야 한다.
        mList = new ArrayList<>();

        SightInfo sightInfo = new SightInfo();
        sightInfo.setRank(1);
        sightInfo.setPicture(R.drawable.test);
        sightInfo.setRating(4.5);
        sightInfo.setLikeCount(311);
        sightInfo.setName("경복궁");
        mList.add(sightInfo);

        sightInfo = new SightInfo();
        sightInfo.setRank(2);
        sightInfo.setPicture(R.drawable.test);
        sightInfo.setRating(2.8);
        sightInfo.setLikeCount(123);
        sightInfo.setName("서울타워");
        mList.add(sightInfo);

        sightInfo = new SightInfo();
        sightInfo.setRank(3);
        sightInfo.setPicture(R.drawable.test);
        sightInfo.setRating(4.8);
        sightInfo.setLikeCount(112);
        sightInfo.setName("청계천");
        mList.add(sightInfo);

        sightInfo = new SightInfo();
        sightInfo.setRank(4);
        sightInfo.setPicture(R.drawable.test);
        sightInfo.setRating(4.2);
        sightInfo.setLikeCount(881);
        sightInfo.setName("63빌딩");
        mList.add(sightInfo);

        sightInfo = new SightInfo();
        sightInfo.setRank(5);
        sightInfo.setPicture(R.drawable.test);
        sightInfo.setRating(3.1);
        sightInfo.setLikeCount(82);
        sightInfo.setName("광화문");
        mList.add(sightInfo);
    }
}
