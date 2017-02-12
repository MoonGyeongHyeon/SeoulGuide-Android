package test.superdroid.com.seoulguide;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import test.superdroid.com.seoulguide.Bucket.BucketFragment;
import test.superdroid.com.seoulguide.Home.HomeFragment;
import test.superdroid.com.seoulguide.Map.MapFragment;
import test.superdroid.com.seoulguide.Planner.PlannerFragment;
import test.superdroid.com.seoulguide.Prefer.PreferFragment;
import test.superdroid.com.seoulguide.Tag.TagFragment;
import test.superdroid.com.seoulguide.Util.OnBackPressedListener;

/* develop*/
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnFocusChangeListener {

    private DrawerLayout mDrawerLayout;
    private MenuItem mSearchMenuItem;
    private Toolbar mToolbar;
    private final String mMenuHome = "홈";
    private final String mMenuTag = "태그별 보기";
    private final String mMenuPrefer = "사용자 성향";
    private final String mMenuBucket = "버킷리스트";
    private final String mMenuPlanner = "플래너";
    private final String mMenuMap = "지도";
    private OnBackPressedListener mOnBackPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);
        mToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.mainDrawerLayout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.mainNavigationView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawerOpen, R.string.drawerClose);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().beginTransaction().add(R.id.fragmentComponentLayout, new HomeFragment(), mMenuHome).commit();
        mToolbar.setTitle(mMenuHome);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_view, menu);

        mSearchMenuItem = menu.findItem(R.id.searchView);
        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setQueryHint("여행지 입력");
        searchView.setOnQueryTextFocusChangeListener(this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //TODO 검색 결과를 보여줌
                mSearchMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //TODO 검색 키워드가 추가될 때마다 검색내용 변경
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        }
        else {
            // 툴바의 타이틀 값을 통해 현재 보여지고 있는 프래그먼트가 무엇인지를 식별.
            switch (mToolbar.getTitle().toString()) {
                case mMenuTag:
                    if(mOnBackPressedListener.doBack()) {
                        return;
                    }
                    break;
            }
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d("LOG/MainActivity", "onNavigationItemSelected()");

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        String tag = null;

        switch(item.getItemId()) {
            case R.id.menuHome :
                if(fragmentManager.findFragmentByTag(mMenuHome) == null) {
                    Log.d("LOG/MainActivity", "Fragment Home is selected");
                    fragment = new HomeFragment();
                    tag = mMenuHome;
                }
                break;
            case R.id.menuTag :
                if(fragmentManager.findFragmentByTag(mMenuTag) == null) {
                    Log.d("LOG/MainActivity", "Fragment Tag is selected");
                    fragment = new TagFragment();
                    tag = mMenuTag;
                    mOnBackPressedListener = (TagFragment) fragment;
                }
                break;
            case R.id.menuPrefer :
                if(fragmentManager.findFragmentByTag(mMenuPrefer) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new PreferFragment();
                    tag = mMenuPrefer;
                }
                break;
            case R.id.menuBucket :
                if(fragmentManager.findFragmentByTag(mMenuBucket) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new BucketFragment();
                    tag = mMenuBucket;
                }
                break;
            case R.id.menuPlanner :
                if(fragmentManager.findFragmentByTag(mMenuPlanner) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new PlannerFragment();
                    tag = mMenuPlanner;
                }
                break;
            case R.id.menuMap :
                if(fragmentManager.findFragmentByTag(mMenuMap) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new MapFragment();
                    tag = mMenuMap;
                }
                break;
            default :
                fragment = null;
                tag = null;
                break;
        }

        if(fragment != null && tag != null) {
            for(int i=0; i<fragmentManager.getBackStackEntryCount(); ++i)
                fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.fragmentComponentLayout, fragment, tag).commit();
            mToolbar.setTitle(tag);
        }

        mDrawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus)
            mSearchMenuItem.collapseActionView();
    }
}
