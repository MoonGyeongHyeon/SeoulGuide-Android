package test.superdroid.com.seoulguide.Prefer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import test.superdroid.com.seoulguide.Util.SharedData;

public class SightListFragment extends Fragment {
    // RecyclerView로부터 출력시킬 PreferSightInfo List.
    private List<PreferSightInfo> mSightInfoList;
    // 이미지의 경로를 가지고 있는 urlList. Bitmap으로 변환하는 과정에서 쓰인다.
    private List<String> mUrlList;
    // DB로부터 데이터를 받아오는 데 받아올 시작 순위. 예를 들어 값이 0일 경우 1위부터 가져오며, 5일 경우 6위부터 가져옴.
    private int mStartNumber;
    // DB로부터 가져올 데이터의 개수.
    private int mDataCount;
    // RecyclerView에 설정할 Adapter 객체.
    private PreferRecyclerViewAdapter mAdapter;
    // 서버에 존재하는 이미지의 주소를 Bitmap 객체로 바꿔줄 객체.
    private BitmapConverter mConverter;
    // 여행지 정보를 가져올 객체.
    private SightInfoManager mSightInfoManager;
    // 이미지 변환 중인 상태에서 상세보기로 넘어갈 경우 변환 작업을 일시중지시킨다.
    // 상세보기에서 다시 홈으로 돌아올 때 일시중지된 변환 작업을 다시 재개시킨다.
    // 이를 위해 싱크를 맞춰줄 변수.
    private boolean isPausedConverting = false;
    private String[] mTagIds;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1위부터 5개의 데이터를 가져오게 변수 초기화.
        mStartNumber = 0;
        mDataCount = 5;

        mSightInfoList = new ArrayList<>();
        mUrlList = new ArrayList<>();

        mConverter = new BitmapConverter();

        Bundle bundle = getArguments();
        mTagIds = bundle.getStringArray("tagIds");

        loadDataByIds(mTagIds);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_prefer_sight_list, container, false);

        mAdapter = new PreferRecyclerViewAdapter(mSightInfoList, R.layout.i_sight_info, this);

        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.preferSightListRecyclerView);
        recyclerView.setAdapter(mAdapter);

        //최하단 아이템에 도달했을 때, 추가로 여행지 데이터를 받아와 이어서 출력.
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            loadDataByIds(mTagIds);
                            Log.d("LOG/Tag", "Last Position : " + lastPosition);
                        }
                    }
                }
            }
        });
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("LOG/Prefer", "onResume()");

        // 뒤로가기 등으로 다시 사용자 성향 프래그먼트에 돌아왔을 때 툴바 타이틀을 복구시킨다.
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.mainToolbar);
        toolbar.setTitle("사용자 성향");

        // 만약 일시중지된 이미지 변환 작업이 존재할 경우 다시 재개시킴.
        if(mConverter.getStatus() == AsyncTask.Status.RUNNING
                && isPausedConverting) {
            isPausedConverting = false;
            Log.d("LOG/Prefer", "isPausedConverting : " + !isPausedConverting + " -> " + isPausedConverting);
            synchronized (mConverter) {
                Log.d("LOG/Prefer", "BitmapConverter is notified");
                mConverter.notify();
            }
        }
    }

    @Override
    public void onPause() {
        Log.d("LOG/Prefer", "onPause()");
        super.onPause();

        // 만약 이미지 변환 작업 중이라면 변환 작업을 일시중지 시킨다.
        if(mConverter.getStatus() == AsyncTask.Status.RUNNING) {
            isPausedConverting = true;
            Log.d("LOG/Prefer", "isPausedConverting : " + !isPausedConverting + " -> " + isPausedConverting);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LOG/Prefer", "onDestroy()");

        // 만약 Converter가 변환을 진행 중인데 홈을 종료시킬 경우 변환 작업 역시 중단시킴.
        if(mConverter != null)
            if(!mConverter.isCancelled()) {
                Log.d("LOG/Prefer", "BitmapConverter is cancelled");
                mConverter.cancel(true);
            }

        // SightInfoManager가 진행 중일 경우 중단시킴.
        if(mSightInfoManager != null)
            if(!mSightInfoManager.isCancelled()) {
                Log.d("LOG/Prefer", "SIghtInfoManager is cancelled");
                mSightInfoManager.cancel(true);
            }
    }

    private void loadDataByIds(String[] ids) {
        Log.d("LOG/Prefer", "loadData()");

        // 인터넷 연결 확인.
        if(Network.isNetworkConnected(getContext())) {
            // 서버에서 사용할 PHP 파일의 이름.
            String phpName = "/new/GetSightDataByIds.php";
            String tagIds = "";

            // 쿼리를 완성시킴.
            String query = "?start_number=" + String.valueOf(mStartNumber) + "&data_count=" + String.valueOf(mDataCount);

            for(int i=0; i<ids.length; i++) {
                tagIds += ids[i];
                if( !(i+1 == i) )
                    tagIds += ",";
            }

            mSightInfoManager = new SightInfoManager();
            // DB로부터 데이터를 받아옴.
            mSightInfoManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,SharedData.SERVER_IP + phpName + query,
                    tagIds
            );
        } else {
            Toast.makeText(getActivity(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();
        }
    }

    private class SightInfoManager extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.d("LOG/Prefer", "SightInfoManager doInBackground()");
            StringBuilder jsonHtml = new StringBuilder();
            try{
                // 연결 url 설정
                URL url = new URL(params[0]);
                String tagIds = params[1];
                String data = URLEncoder.encode("tag_ids", "UTF-8") + "=" + URLEncoder.encode(tagIds, "UTF-8");

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
            Log.d("LOG/Prefer", "SightInfoManager onPostExecute()");
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

                    // mSightInfoList에 추가하기 위해 PreferSightInfo 객체 생성 및 필드 설정.
                    PreferSightInfo preferSightInfo = new PreferSightInfo();
                    preferSightInfo.setId(id);
                    preferSightInfo.setName(name);
                    // 데이터가 추가 로드될 때 순위를 표시하기 위해 mStartNumber를 더해줌.
                    preferSightInfo.setRank(i + mStartNumber + 1);
                    preferSightInfo.setLikeCount(recommendCount);
                    // 별점을 구하는 데 만약 총점이 0이거나 평가자가 없을 경우 0.0으로 설정.
                    if (sumPoint == 0 || peopleCount == 0)
                        preferSightInfo.setRating(0.0);
                    else {
                        // 순환소수가 나올 수 있으므로 둘째 자리에서 반올림.
                        String avgStr = String.format(Locale.getDefault() ,"%.1f", sumPoint / peopleCount);
                        preferSightInfo.setRating(Double.valueOf(avgStr));
                    }

                    mSightInfoList.add(preferSightInfo);

                    mUrlList.add(SharedData.SERVER_IP + thumbnail);
                    Log.d("LOG/Prefer", "get PreferSightInfo : " + preferSightInfo.getName());
                }
                // 이미지를 제외한 여행지의 정보를 업데이트함.
                mAdapter.notifyDataSetChanged();

                // 만약 SightInfoManager가 중단됐을 경우 Converter도 호출되지 않아야 함.
                if(!isCancelled()) {
                    // 여행지가 하나도 없을 경우 BitmapConverter를 호출할 필요가 없다.
                    Log.d("LOG/Prefer", "BitmapConverter is called");
                    Log.d("LOG/Prefer", "BitmapConverter status : " + mConverter.getStatus());
                    if (mConverter.getStatus() == Status.FINISHED) {
                        // 만약 BitmapConverter가 FINISHED 상태이면 재생성시킴.
                        Log.d("LOG/Prefer", "BitmapConverter is finished and recreated");
                        mConverter = new BitmapConverter();
                    }
                    // 인수로 이미지의 경로가 저장되어 있는 List를 넘김.
                    mConverter.executeOnExecutor(THREAD_POOL_EXECUTOR, mUrlList);
                } else {
                    Log.d("LOG/Prefer", "BitmapConverter is cancelled");
                }

            } catch (JSONException e) {
                // 태그 결과가 없을 경우 예외가 발생한다.
                e.printStackTrace();
                Log.d("LOG/Prefer", "Sight is nothing");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class BitmapConverter extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            Log.d("LOG/Prefer", "BitmapConverter is started");
            // 이미지의 경로가 저장되어 있는 list.
            List<String> list = (List<String>) params[0];
            Bitmap bitmap;
            URL newurl;
            for(int i=mStartNumber; i<mSightInfoList.size(); i++) {
                try {
                    // 만약 프래그먼트의 종료 등으로 인해 변환 작업을 중지시켜야 할 경우.
                    if(isCancelled()) {
                        Log.d("LOG/Prefer", "BitmapConverter is stopped");
                        return null;
                    }
                    if(isPausedConverting) {
                        Log.d("LOG/Prefer", "BitmapConverter is waiting");
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
            Log.d("LOG/Prefer", "BitmapConverter onProgressUpdate()");
            // 변환된 Bitmap.
            Bitmap bitmap = (Bitmap) values[0];
            // item의 index.
            int index = (Integer) values[1];

            if(!mSightInfoList.isEmpty()) {
                // index에 해당하는 mSightInfoList 원소를 가져옴.
                PreferSightInfo preferSightInfo = mSightInfoList.get(index);
                // 가져온 원소에 bitmap을 설정함.
                preferSightInfo.setPicture(bitmap);

                // 화면에 이미지 출력을 위해 업데이트.
                mAdapter.notifyItemChanged(index);
                Log.d("LOG/Prefer", "get Bitmap : " + preferSightInfo.getName());
            }
        }
    }
}
