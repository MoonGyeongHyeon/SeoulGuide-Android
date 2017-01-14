package test.superdroid.com.seoulguide.Home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class RankingViewPagerAdapter extends FragmentPagerAdapter {

    public static int TAB_COUNT = 2;

    public RankingViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new WeekSightFragment();
            case 1:
                return new MonthSightFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "주간 랭킹";
            case 1:
                return "월간 랭킹";
            default:
                return null;
        }
    }
}
