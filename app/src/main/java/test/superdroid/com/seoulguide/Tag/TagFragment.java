package test.superdroid.com.seoulguide.Tag;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import test.superdroid.com.seoulguide.R;
import test.superdroid.com.seoulguide.Util.Network;
import test.superdroid.com.seoulguide.Util.OnBackPressedListener;
import test.superdroid.com.seoulguide.Util.SharedData;

public class TagFragment extends Fragment implements OnBackPressedListener {
    // RecyclerView로부터 출력시킬 TagSightInfo List.
    private List<TagSightInfo> mSightInfoList;
    // 이미지의 경로를 가지고 있는 urlList. Bitmap으로 변환하는 과정에서 쓰인다.
    private List<String> mUrlList;
    // DB로부터 데이터를 받아오는 데 받아올 시작 순위. 예를 들어 값이 0일 경우 1위부터 가져오며, 5일 경우 6위부터 가져옴.
    private int mStartNumber;
    // DB로부터 가져올 데이터의 개수.
    private int mDataCount;
    // RecyclerView에 설정할 Adapter 객체.
    private TagRecyclerViewAdapter mAdapter;
    // 서버에 존재하는 이미지의 주소를 Bitmap 객체로 바꿔줄 객체.
    private BitmapConverter mConverter;
    // 여행지 정보를 가져올 객체.
    private SightInfoManager mSightInfoManager;
    // 이미지 변환 중인 상태에서 상세보기로 넘어갈 경우 변환 작업을 일시중지시킨다.
    // 상세보기에서 다시 홈으로 돌아올 때 일시중지된 변환 작업을 다시 재개시킨다.
    // 이를 위해 싱크를 맞춰줄 변수.
    private boolean isPausedConverting = false;
    private CheckBox mMoreCheckBox;
    private RecyclerView mSightRecyclerView;
    private TextView mResultNothingTextView;
    private LinearLayout mMoreLayout;
    private List<String> mMainCategoryTagList;
    private List<String> mSubCategoryTagList;
    private List<CheckBox> mMainCategoryCheckBoxList;
    private List<CheckBox> mSubCategoryCheckBoxList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1위부터 5개의 데이터를 가져오게 변수 초기화.
        mStartNumber = 0;
        mDataCount = 5;

        mSightInfoList = new ArrayList<>();
        mUrlList = new ArrayList<>();

        mConverter = new BitmapConverter();

        mMainCategoryTagList = new ArrayList<>();
        mSubCategoryTagList = new ArrayList<>();
        mMainCategoryCheckBoxList = new ArrayList<>();
        mSubCategoryCheckBoxList = new ArrayList<>();

        loadData(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_tag, container, false);

        mSightRecyclerView = (RecyclerView) layout.findViewById(R.id.tagSightRecyclerView);
        mMoreLayout = (LinearLayout) layout.findViewById(R.id.tagMoreLayout);
        mResultNothingTextView = (TextView) layout.findViewById(R.id.tagResultNothingTextView);

        initCheckBox(layout);

        mAdapter = new TagRecyclerViewAdapter(mSightInfoList, R.layout.i_sight_info, this);
        mSightRecyclerView.setAdapter(mAdapter);

        //최하단 아이템에 도달했을 때, 추가로 여행지 데이터를 받아와 이어서 출력.
        mSightRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                // 스크롤을 드래그할 때 동작하도록.
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                    // 현재 화면에서 보이는 아이템의 가장 마지막 position을 가져옴.
                    int lastPosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                    // 그 position이 최하단에 근접했을 경우.
                    if((lastPosition+2) >= (mSightInfoList.size()-1) && mStartNumber+mDataCount == mSightInfoList.size()) {
                        // 최대 30위까지만 보여주도록 제한.
                        if(mStartNumber+mDataCount < SharedData.MAX_DATA_COUNT) {
                            Log.d("LOG/Tag", "mStartNumber : " + mStartNumber);
                            // 가져올 데이터의 시작 번호를 변경.
                            mStartNumber = mSightInfoList.size();
                            //데이터를 가져옴.
                            loadData(mMainCategoryTagList.isEmpty() && mSubCategoryTagList.isEmpty());
                            Log.d("LOG/Tag", "Last Position : " + lastPosition);
                        }
                    }
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mSightRecyclerView.setLayoutManager(layoutManager);

        Button tagOKButton = (Button) layout.findViewById(R.id.tagOKButton);
        tagOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initField();

                for(int i=0;i<mMainCategoryCheckBoxList.size(); i++) {
                    CheckBox checkbox = mMainCategoryCheckBoxList.get(i);
                    if(checkbox.isChecked()) {
                        mMainCategoryTagList.add(checkbox.getText().toString());
                        checkbox.setChecked(false);
                        Log.d("LOG/Tag", "Main tag : " + checkbox.getText().toString());
                    }
                }
                for(int i=0;i<mSubCategoryCheckBoxList.size(); i++) {
                    CheckBox checkbox = mSubCategoryCheckBoxList.get(i);
                    if(checkbox.isChecked()) {
                        mSubCategoryTagList.add(checkbox.getText().toString());
                        checkbox.setChecked(false);
                        Log.d("LOG/Tag", "Sub tag : " + checkbox.getText().toString());
                    }
                }
                mMoreCheckBox.setChecked(false);
                loadData(mMainCategoryTagList.isEmpty() && mSubCategoryTagList.isEmpty());
            }
        });

        return layout;
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("LOG/Tag", "onResume()");

        // 뒤로가기 등으로 다시 태그 프래그먼트에 돌아왔을 때 툴바 타이틀을 복구시킨다.
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.mainToolbar);
        toolbar.setTitle("태그별 보기");

        // 만약 일시중지된 이미지 변환 작업이 존재할 경우 다시 재개시킴.
        if(mConverter.getStatus() == AsyncTask.Status.RUNNING
                && isPausedConverting) {
            isPausedConverting = false;
            Log.d("LOG/Tag", "isPausedConverting : " + !isPausedConverting + " -> " + isPausedConverting);
            synchronized (mConverter) {
                Log.d("LOG/Tag", "BitmapConverter is notified");
                mConverter.notify();
            }
        }
    }

    @Override
    public void onPause() {
        Log.d("LOG/Tag", "onPause()");
        super.onPause();

        // 만약 이미지 변환 작업 중이라면 변환 작업을 일시중지 시킨다.
        if(mConverter.getStatus() == AsyncTask.Status.RUNNING) {
            isPausedConverting = true;
            Log.d("LOG/Tag", "isPausedConverting : " + !isPausedConverting + " -> " + isPausedConverting);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LOG/Tag", "onDestroy()");

        // 만약 Converter가 변환을 진행 중인데 홈을 종료시킬 경우 변환 작업 역시 중단시킴.
        if(mConverter != null)
            if(!mConverter.isCancelled()) {
                Log.d("LOG/Tag", "BitmapConverter is cancelled");
                mConverter.cancel(true);
            }

        // SightInfoManager가 진행 중일 경우 중단시킴.
        if(mSightInfoManager != null)
            if(!mSightInfoManager.isCancelled()) {
                Log.d("LOG/Tag", "SIghtInfoManager is cancelled");
                mSightInfoManager.cancel(true);
            }
    }

    // 무언가를 처리했으면 true를, 아닐 경우 false를 반환.
    @Override
    public boolean doBack() {
        Log.d("LOG/Tag", "Back key is pressed");
        if(mMoreLayout.getVisibility() == View.VISIBLE) {
            mMoreCheckBox.setChecked(false);
            return true;
        }
        return false;
    }

    private void loadData(boolean isTagEmpty) {
        Log.d("LOG/Tag", "loadData()");

        // 인터넷 연결 확인.
        if(Network.isNetworkConnected(getContext())) {
            // 서버에서 사용할 PHP 파일의 이름.
            String phpName;
            String mainQuery = null;
            String subQuery = null;
            if(isTagEmpty || mMainCategoryTagList.contains("전체"))
                phpName = "/new/GetSightData.php";
            else {
                phpName = "/new/GetSightDataByTag.php";

                if(!mMainCategoryTagList.isEmpty()) {
                    mainQuery = mMainCategoryTagList.get(0);
                    for (int i = 1; i < mMainCategoryTagList.size(); i++) {
                        mainQuery += "," + mMainCategoryTagList.get(i);
                    }
                }

                if(!mSubCategoryTagList.isEmpty()) {
                    subQuery = mSubCategoryTagList.get(0);
                    for (int i = 1; i < mSubCategoryTagList.size(); i++) {
                        subQuery += "," + mSubCategoryTagList.get(i);
                    }
                }
            }

            Log.d("LOG/Tag", phpName);

            mSightInfoManager = new SightInfoManager();
            // DB로부터 데이터를 받아옴.
            mSightInfoManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,SharedData.SERVER_IP + phpName,
                    String.valueOf(mStartNumber),
                    String.valueOf(mDataCount),
                    mainQuery,
                    subQuery
            );
        } else {
            Toast.makeText(getActivity(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();
        }
    }

    private void initCheckBox(View layout) {
        // 메인 카테고리
        //**************************************************************
        // 처음부터 보여지는 메인 카테고리를 터치할 경우 나머지 태그 선택부분 레이아웃이 자동으로 확장되도록.
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!mMoreCheckBox.isChecked()) {
                    mMoreCheckBox.setChecked(true);
                    mMoreLayout.setVisibility(View.VISIBLE);
                    mSightRecyclerView.setVisibility(View.GONE);
                    mResultNothingTextView.setVisibility(View.GONE);
                }
            }
        };
        CheckBox checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryAllCheckBox);
        checkBox.setButtonDrawable(new StateListDrawable());
        checkBox.setOnCheckedChangeListener(listener);
        mMainCategoryCheckBoxList.add(checkBox);

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox1);
        checkBox.setButtonDrawable(new StateListDrawable());
        checkBox.setOnCheckedChangeListener(listener);
        mMainCategoryCheckBoxList.add(checkBox);

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox2);
        checkBox.setButtonDrawable(new StateListDrawable());
        checkBox.setOnCheckedChangeListener(listener);
        mMainCategoryCheckBoxList.add(checkBox);

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox3);
        checkBox.setButtonDrawable(new StateListDrawable());
        checkBox.setOnCheckedChangeListener(listener);
        mMainCategoryCheckBoxList.add(checkBox);

        mMoreCheckBox = (CheckBox) layout.findViewById(R.id.tagMoreCheckBox);
        mMoreCheckBox.setButtonDrawable(new StateListDrawable());
        mMoreCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mMoreLayout.setVisibility(View.VISIBLE);
                    mSightRecyclerView.setVisibility(View.GONE);
                    mResultNothingTextView.setVisibility(View.GONE);
                } else {
                    mMoreLayout.setVisibility(View.GONE);
                    if(mSightInfoList.isEmpty() && mSightInfoManager.getStatus() == AsyncTask.Status.FINISHED)
                        mResultNothingTextView.setVisibility(View.VISIBLE);
                    else
                        mSightRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox4);
        checkBox.setButtonDrawable(new StateListDrawable());
        mMainCategoryCheckBoxList.add(checkBox);

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox5);
        checkBox.setButtonDrawable(new StateListDrawable());
        mMainCategoryCheckBoxList.add(checkBox);

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox6);
        checkBox.setButtonDrawable(new StateListDrawable());
        mMainCategoryCheckBoxList.add(checkBox);

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox7);
        checkBox.setButtonDrawable(new StateListDrawable());
        mMainCategoryCheckBoxList.add(checkBox);

        checkBox = (CheckBox) layout.findViewById(R.id.tagCategoryCheckBox8);
        checkBox.setButtonDrawable(new StateListDrawable());
        mMainCategoryCheckBoxList.add(checkBox);
        //**************************************************************

        // 서브 카테고리
        //**************************************************************
        // 지역별
        checkBox = (CheckBox) layout.findViewById(R.id.tagLocationCheckBox1);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagLocationCheckBox2);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagLocationCheckBox3);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagLocationCheckBox4);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagLocationCheckBox5);
         mSubCategoryCheckBoxList.add(checkBox);

        // 분위기
        checkBox = (CheckBox) layout.findViewById(R.id.tagAtmosphereCheckBox1);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagAtmosphereCheckBox2);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagAtmosphereCheckBox3);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagAtmosphereCheckBox4);
         mSubCategoryCheckBoxList.add(checkBox);

        // 테마
        checkBox = (CheckBox) layout.findViewById(R.id.tagThemeCheckBox1);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagThemeCheckBox2);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagThemeCheckBox3);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagThemeCheckBox4);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagThemeCheckBox5);
         mSubCategoryCheckBoxList.add(checkBox);

        // 일행
        checkBox = (CheckBox) layout.findViewById(R.id.tagGroupCheckBox1);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagGroupCheckBox2);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagGroupCheckBox3);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagGroupCheckBox4);
         mSubCategoryCheckBoxList.add(checkBox);

        // 연령
        checkBox = (CheckBox) layout.findViewById(R.id.tagAgeCheckBox1);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagAgeCheckBox2);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagAgeCheckBox3);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagAgeCheckBox4);
         mSubCategoryCheckBoxList.add(checkBox);
        checkBox = (CheckBox) layout.findViewById(R.id.tagAgeCheckBox5);
         mSubCategoryCheckBoxList.add(checkBox);
        //**************************************************************
    }

    // 태그를 선택하고 확인 버튼을 눌렀을 때
    // 기존에 가지고 있던 정보들을 모두 초기화.
    public void initField() {
        mStartNumber = 0;

        mSightInfoList.clear();
        mUrlList.clear();

        mAdapter = new TagRecyclerViewAdapter(mSightInfoList, R.layout.i_sight_info, this);
        mSightRecyclerView.setAdapter(mAdapter);

        mSightInfoManager.cancel(false);
        mSightInfoManager = new SightInfoManager();

        mConverter.cancel(false);
        mConverter = new BitmapConverter();

        mMainCategoryTagList.clear();
        mSubCategoryTagList.clear();
    }

    private class SightInfoManager extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.d("LOG/Tag", "SightInfoManager doInBackground()");
            StringBuilder jsonHtml = new StringBuilder();
            try{
                // 연결 url 설정
                URL url = new URL(params[0]);
                String startNumber = params[1];
                String dataCount = params[2];
                String mainQuery = params[3];
                String subQuery = params[4];
                String data = URLEncoder.encode("start_number", "UTF-8") + "=" + URLEncoder.encode(startNumber, "UTF-8");
                data += "&" + URLEncoder.encode("data_count", "UTF-8") + "=" + URLEncoder.encode(dataCount, "UTF-8");
                if(mainQuery != null) {
                    Log.d("LOG/Tag", "Query : " + mainQuery);
                    data += "&" + URLEncoder.encode("main_query", "UTF-8") + "=" + URLEncoder.encode(mainQuery, "UTF-8");
                }
                if(subQuery != null) {
                    Log.d("LOG/Tag", "Query : " + subQuery);
                    data += "&" + URLEncoder.encode("sub_query", "UTF-8") + "=" + URLEncoder.encode(subQuery, "UTF-8");
                }

                // 커넥션 객체 생성
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                // 연결되었으면.
                if(conn != null){
                    // POST 방식을 위한 데이터 셋
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);

                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    // 연결되었음 코드가 리턴되면.
                    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        for(;;){
                            // 웹상에 보여지는 텍스트를 라인단위로 읽어 저장.
                            String line = br.readLine();
                            if(line == null) break;
                            // 저장된 텍스트 라인을 jsonHtml에 붙여넣음
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch(Exception ex){
                ex.printStackTrace();
            }
            return jsonHtml.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("LOG/Tag", "SightInfoManager onPostExecute()");
            // DB에 저장된 이미지의 경로를 따로 List에 저장시킴.
            // 이 List의 값을 바탕으로 Bitmap으로 변환시킴.
            try {
                JSONObject root = new JSONObject(result);
                JSONArray ja = root.getJSONArray("result");
                // DB로부터 받아온 JSON 형식을 자바에서 사용할 수 있도록 변환.
                for (int i = 0; i < ja.length(); i++) {
                    if(isCancelled()) {
                        mSightInfoList.clear();
                        return;
                    }
                    JSONObject jo = ja.getJSONObject(i);
                    int id = jo.getInt("sight_id");
                    String name = jo.getString("sight_name");
                    int recommendCount = jo.getInt("sight_recommend_count");
                    String thumbnail = jo.getString("sight_thumbnail");
                    double sumPoint = jo.getDouble("sum_point");
                    int peopleCount = jo.getInt("p_count");

                    // mSightInfoList에 추가하기 위해 tagSightInfo 객체 생성 및 필드 설정.
                    TagSightInfo tagSightInfo = new TagSightInfo();
                    tagSightInfo.setId(id);
                    tagSightInfo.setName(name);
                    // 데이터가 추가 로드될 때 순위를 표시하기 위해 mStartNumber를 더해줌.
                    tagSightInfo.setRank(i + mStartNumber + 1);
                    tagSightInfo.setLikeCount(recommendCount);
                    // 별점을 구하는 데 만약 총점이 0이거나 평가자가 없을 경우 0.0으로 설정.
                    if (sumPoint == 0 || peopleCount == 0)
                        tagSightInfo.setRating(0.0);
                    else {
                        // 순환소수가 나올 수 있으므로 둘째 자리에서 반올림.
                        String avgStr = String.format(Locale.getDefault() ,"%.1f", sumPoint / peopleCount);
                        tagSightInfo.setRating(Double.valueOf(avgStr));
                    }

                    mSightInfoList.add(tagSightInfo);

                    mUrlList.add(SharedData.SERVER_IP + thumbnail);
                    Log.d("LOG/Tag", "get TagSightInfo : " + tagSightInfo.getName());
                }
                // 이미지를 제외한 여행지의 정보를 업데이트함.
                mAdapter.notifyDataSetChanged();

                // 만약 SightInfoManager가 중단됐을 경우 Converter도 호출되지 않아야 함.
                if(!isCancelled()) {
                    // 여행지가 하나도 없을 경우 BitmapConverter를 호출할 필요가 없다.
                    Log.d("LOG/Tag", "BitmapConverter is called");
                    Log.d("LOG/Tag", "BitmapConverter status : " + mConverter.getStatus());
                    if (mConverter.getStatus() == Status.FINISHED) {
                        // 만약 BitmapConverter가 FINISHED 상태이면 재생성시킴.
                        Log.d("LOG/Tag", "BitmapConverter is finished and recreated");
                        mConverter = new BitmapConverter();
                    }
                    // 인수로 이미지의 경로가 저장되어 있는 List를 넘김.
                    mConverter.executeOnExecutor(THREAD_POOL_EXECUTOR, mUrlList);

                    if(mSightInfoList.isEmpty()) {
                        mResultNothingTextView.setVisibility(View.VISIBLE);
                        mSightRecyclerView.setVisibility(View.GONE);
                    }

                } else {
                    Log.d("LOG/Tag", "BitmapConverter is cancelled");
                }

            } catch (JSONException e) {
                // 태그 결과가 없을 경우 예외가 발생한다.
                e.printStackTrace();
                Log.d("LOG/Tag", "Sight is nothing");
                mResultNothingTextView.setVisibility(View.VISIBLE);
                mSightRecyclerView.setVisibility(View.GONE);

            } catch (Exception e) {
                Log.d("LOG/Tag", "B");
                e.printStackTrace();
            }
        }
    }

    private class BitmapConverter extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            Log.d("LOG/Tag", "BitmapConverter is started");
            // 이미지의 경로가 저장되어 있는 list.
            List<String> list = (List<String>) params[0];
            Bitmap bitmap;
            URL newurl;
            for(int i=mStartNumber; i<mSightInfoList.size(); i++) {
                try {
                    // 만약 프래그먼트의 종료 등으로 인해 변환 작업을 중지시켜야 할 경우.
                    if(isCancelled()) {
                        Log.d("LOG/Tag", "BitmapConverter is stopped");
                        return null;
                    }
                    if(isPausedConverting) {
                        Log.d("LOG/Tag", "BitmapConverter is waiting");
                        synchronized (mConverter) {
                            mConverter.wait();
                        }
                    }
                    newurl = new URL(list.get(i));
                    bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                    // 하나의 이미지 변환 작업이 종료될 경우 호출.
                    // 인수로 변환된 Bitmap과 item의 index를 넘겨줌.
                    publishProgress(bitmap,i);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Object... values) {
            Log.d("LOG/Tag", "BitmapConverter onProgressUpdate()");
            // 변환된 Bitmap.
            Bitmap bitmap = (Bitmap) values[0];
            // item의 index.
            int index = (Integer) values[1];

            if(!mSightInfoList.isEmpty()) {
                // index에 해당하는 mSightInfoList 원소를 가져옴.
                TagSightInfo tagSightInfo = mSightInfoList.get(index);
                // 가져온 원소에 bitmap을 설정함.
                tagSightInfo.setPicture(bitmap);

                // 화면에 이미지 출력을 위해 업데이트.
                mAdapter.notifyItemChanged(index);
                Log.d("LOG/Tag", "get Bitmap : " + tagSightInfo.getName());
            }
        }
    }
}
