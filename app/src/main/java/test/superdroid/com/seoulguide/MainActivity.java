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
/* develop*/
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnFocusChangeListener {

    private DrawerLayout mDrawerLayout;
    private MenuItem mSearchMenuItem;
    private Toolbar mToolbar;
    private String menuHome = "Menu Home";
    private String menuTag = "Menu Tag";
    private String menuPrefer = "Menu Prefer";
    private String menuBucket = "Menu Bucket";
    private String menuPlanner = "Menu Planner";
    private String menuMap = "Menu Map";


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

        getSupportFragmentManager().beginTransaction().add(R.id.fragmentComponentLayout, new HomeFragment(), menuHome).commit();
        mToolbar.setTitle(getResources().getString(R.string.menuHome));
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
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawers();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d("LOG/MainActivity", "onNavigationItemSelected()");

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        String tag = null;
        String title = null;

        switch(item.getItemId()) {
            case R.id.menuHome :
                if(fragmentManager.findFragmentByTag(menuHome) == null) {
                    Log.d("LOG/MainActivity", "Fragment Home is selected");
                    fragment = new HomeFragment();
                    tag = menuHome;
                    title = getResources().getString(R.string.menuHome);
                }
                break;
            case R.id.menuTag :
                if(fragmentManager.findFragmentByTag(menuTag) == null) {
                    Log.d("LOG/MainActivity", "Fragment Tag is selected");
                    fragment = new TagFragment();
                    tag = menuTag;
                    title = getResources().getString(R.string.menuTag);
                }
                break;
            case R.id.menuPrefer :
                if(fragmentManager.findFragmentByTag(menuPrefer) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new PreferFragment();
                    tag = menuPrefer;
                    title = getResources().getString(R.string.menuPrefer);
                }
                break;
            case R.id.menuBucket :
                if(fragmentManager.findFragmentByTag(menuBucket) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new BucketFragment();
                    tag = menuBucket;
                    title = getResources().getString(R.string.menuBucket);
                }
                break;
            case R.id.menuPlanner :
                if(fragmentManager.findFragmentByTag(menuPlanner) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new PlannerFragment();
                    tag = menuPlanner;
                    title = getResources().getString(R.string.menuPlanner);
                }
                break;
            case R.id.menuMap :
                if(fragmentManager.findFragmentByTag(menuMap) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new MapFragment();
                    tag = menuMap;
                    title = getResources().getString(R.string.menuMap);
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
            mToolbar.setTitle(title);
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
