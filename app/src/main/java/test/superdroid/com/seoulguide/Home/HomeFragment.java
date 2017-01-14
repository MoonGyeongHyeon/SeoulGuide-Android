package test.superdroid.com.seoulguide.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import test.superdroid.com.seoulguide.R;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_home, container, false);

        RankingViewPagerAdapter adapter = new RankingViewPagerAdapter(getChildFragmentManager());
        ViewPager viewPager = (ViewPager) layout.findViewById(R.id.homeViewPager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) layout.findViewById(R.id.homeTabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return layout;
    }
}
