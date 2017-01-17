package test.superdroid.com.seoulguide.Home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import test.superdroid.com.seoulguide.R;
import test.superdroid.com.seoulguide.Util.Network;
import test.superdroid.com.seoulguide.Util.SharedData;

public class WeekSightFragment extends Fragment {

    // RecyclerView로부터 출력시킬 SightInfo List.
    private List<SightInfo> mList = new ArrayList<>();
    // DB로부터 데이터를 받아오는 데 받아올 시작 순위. 예를 들어 값이 0일 경우 1위부터 가져오며, 5일 경우 6위부터 가져옴.
    private int startNumber = 0;
    // DB로부터 가져올 데이터의 개수.
    private int dataCount = 10;
    // RecyclerView에 설정할 Adapter 객체.
    private RankingRecyclerViewAdapter mAdapter;
    // 서버에서 사용할 PHP 파일의 이름.
    private final String phpName = "/new/Sight1000GetData.php";
    // 서버에 존재하는 이미지의 주소를 Bitmap 객체로 바꿔줄 객체.
    private BitmapConverter mConverter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_week_sight, container, false);

        loadData();

        mAdapter = new RankingRecyclerViewAdapter(mList, R.layout.i_home_ranking);
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.weekRankingRecyclerView);
        recyclerView.setAdapter(mAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
        // 만약 Converter가 변환을 진행 중인데 이 프래그먼트가 종료될 경우 변환 작업을 중단시킴.
        if(mConverter != null)
            if(!mConverter.isCancelled())
                mConverter.cancel(false);
    }

    private void loadData() {
        // 인터넷 연결 확인.
        if(Network.isNetworkConnected(getContext())) {
            // 쿼리를 완성시킴.
            String query = "?start_number=" + String.valueOf(startNumber) + "&data_count=" + String.valueOf(dataCount);
            SightInfoManager sightInfoManager = new SightInfoManager();

            String serverIP = SharedData.getServerIP();
            // DB로부터 데이터를 받아옴.
            sightInfoManager.execute(serverIP + phpName + query);
        } else {
            Toast.makeText(getActivity(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();
        }
    }

    private class SightInfoManager extends AsyncTask<String, Integer,String> {

        @Override
        protected String doInBackground(String... params) {
            StringBuilder jsonHtml = new StringBuilder();
            try{
                // 연결 url 설정
                URL url = new URL(params[0]);
                // 커넥션 객체 생성
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                // 연결되었으면.
                if(conn != null){
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
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
            // DB에 저장된 이미지의 경로를 따로 List에 저장시킴.
            // 이 List의 값을 바탕으로 Bitmap으로 변환시킴.
            List<String> urlList = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(result);
                JSONArray ja = root.getJSONArray("result");
                // DB로부터 받아온 JSON 형식을 자바에서 사용할 수 있도록 변환.
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    int id = jo.getInt("sight_id");
                    String name = jo.getString("sight_name");
                    int recommendCount = jo.getInt("sight_recommend_count");
                    String thumbnail = jo.getString("sight_thumbnail");
                    double sumPoint = jo.getDouble("sum_point");
                    int peopleCount = jo.getInt("p_count");

                    // mList에 추가하기 위해 SightInfo 객체 생성 및 필드 설정.
                    SightInfo sightInfo = new SightInfo();
                    sightInfo.setId(id);
                    sightInfo.setName(name);
                    sightInfo.setRank(i + 1);
                    sightInfo.setLikeCount(recommendCount);
                    // 별점을 구하는 데 만약 총점이 0이거나 평가자가 없을 경우 0.0으로 설정.
                    if (sumPoint == 0 || peopleCount == 0)
                        sightInfo.setRating(0.0);
                    else
                        sightInfo.setRating(sumPoint / peopleCount);

                    mList.add(sightInfo);

                    String serverIP = SharedData.getServerIP();
                    urlList.add(serverIP + thumbnail);
                    Log.d("LOG/HomeWeek", "get SightInfo " + sightInfo.getName());
                }
                // 이미지를 제외한 여행지의 정보를 업데이트함.
                mAdapter.notifyDataSetChanged();
                mConverter = new BitmapConverter();
                // 인수로 이미지의 경로가 저장되어 있는 List를 넘김.
                mConverter.execute(urlList);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class BitmapConverter extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            // 이미지의 경로가 저장되어 있는 list.
            List<String> list = (List<String>) params[0];
            Bitmap bitmap;
            URL newurl;
            for(int i=startNumber; i<mList.size(); i++) {
                // 만약 프래그먼트의 종료 등으로 인해 변환 작업을 중지시켜야 할 경우.
                if(isCancelled())
                    return null;
                try {
                    newurl = new URL(list.get(i));
                    bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                    // 하나의 이미지 변환 작업이 종료될 경우 호출.
                    // 인수로 변환된 Bitmap과 item의 index를 넘겨줌.
                    publishProgress(bitmap,i);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Object... values) {
            // 변환된 Bitmap.
            Bitmap bitmap = (Bitmap) values[0];
            // item의 index.
            int index = (Integer) values[1];

            // index에 해당하는 mList 원소를 가져옴.
            SightInfo sightInfo = mList.get(index);
            // 가져온 원소에 bitmap을 설정함.
            sightInfo.setPicture(bitmap);

            // 화면에 이미지 출력을 위해 업데이트.
            mAdapter.notifyItemChanged(index);
            Log.d("LOG/HomeWeek", "get Bitmap " + sightInfo.getName());
        }
    }
}
