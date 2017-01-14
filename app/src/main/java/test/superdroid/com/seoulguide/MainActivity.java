package test.superdroid.com.seoulguide;

import android.support.annotation.NonNull;
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
    private String MENU_HOME = "Menu Home";
    private String MENU_TAG = "Menu Tag";
    private String MENU_PREFER = "Menu Prefer";
    private String MENU_BUCKET = "Menu Bucket";
    private String MENU_PLANNER = "Menu Planner";
    private String MENU_MAP = "Menu Map";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.mainDrawerLayout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.mainNavigationView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawerOpen, R.string.drawerClose);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().beginTransaction().add(R.id.fragmentComponentLayout, new HomeFragment(), MENU_HOME).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_view, menu);

        mSearchMenuItem = menu.findItem(R.id.searchView);
        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
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

        switch(item.getItemId()) {
            case R.id.menuHome :
                if(fragmentManager.findFragmentByTag(MENU_HOME) == null) {
                    Log.d("LOG/MainActivity", "Fragment Home is selected");
                    fragment = new HomeFragment();
                    tag = MENU_HOME;
                }
                break;
            case R.id.menuTag :
                if(fragmentManager.findFragmentByTag(MENU_TAG) == null) {
                    Log.d("LOG/MainActivity", "Fragment Tag is selected");
                    fragment = new TagFragment();
                    tag = MENU_TAG;
                }
                break;
            case R.id.menuPrefer :
                if(fragmentManager.findFragmentByTag(MENU_PREFER) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new PreferFragment();
                    tag = MENU_PREFER;
                }
                break;
            case R.id.menuBucket :
                if(fragmentManager.findFragmentByTag(MENU_BUCKET) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new BucketFragment();
                    tag = MENU_BUCKET;
                }
                break;
            case R.id.menuPlanner :
                if(fragmentManager.findFragmentByTag(MENU_PLANNER) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new PlannerFragment();
                    tag = MENU_PLANNER;
                }
                break;
            case R.id.menuMap :
                if(fragmentManager.findFragmentByTag(MENU_MAP) == null) {
                    Log.d("LOG/MainActivity", "Fragment Prefer is selected");
                    fragment = new MapFragment();
                    tag = MENU_MAP;
                }
                break;
            default :
                fragment = null;
                tag = null;
                break;
        }

        if(fragment != null && tag != null)
            fragmentManager.beginTransaction().replace(R.id.fragmentComponentLayout, fragment, tag).commit();

        mDrawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus)
            mSearchMenuItem.collapseActionView();
    }
}
