package test.superdroid.com.seoulguide.Detail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

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

public class SightDetailFragment extends Fragment {

    // 여행지의 상세정보를 가지고 있음.
    private DetailInfo mDetailInfo;
    // 여행지의 이미지 경로를 Bitmap으로 변환하는 객체.
    private BitmapConverter mBitmapConverter;
    // 여행지의 이미지들을 보여줄 레이아웃 객체.
    private LinearLayout mSubImageLayout;
    // 최상단의 메인 이미지를 저장할 객체.
    private ImageView mMainImageView;
    // Sub Image를 담당하는 ViewPager에 부착할 Adapter
    private SubImageViewPagerAdapter mAdapter;
    // 이미지 변환 중인 상태에서 홈 등으로 화면이 전환될 경우 변환 작업을 일시중지시킨다.
    // 다시 상세보기로 돌아올 때 일시중지된 변환 작업을 다시 재개시킨다.
    // 이를 위해 싱크를 맞춰줄 변수.
    private boolean isPausedConverting = false;
    // 다음 지도 API를 위한 key.
    private final String mMapKey = "17fd056513f09a6ebe80bdb559e2285d";
    // 맵 뷰를 담을 레이아웃.
    private FrameLayout mMapLayout;
    // 리뷰를 출력할 뷰.
    private RecyclerView mReviewRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetailInfo = new DetailInfo();

        mBitmapConverter = new BitmapConverter();

        // 홈 프래그먼트 등에서 넘어온 여행지 id와 name을 받아온다.
        Bundle bundle = getArguments();
        if(bundle != null) {
            int id = bundle.getInt("sightId");
            mDetailInfo.setId(id);
            String name = bundle.getString("sightName");
            mDetailInfo.setName(name);
            Log.d("LOG/Detail", "Sight id : " + id + ", name : " + name);
        }

        loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.f_sight_detail, container, false);

        mSubImageLayout = (LinearLayout) layout.findViewById(R.id.detailSubImageLayout);
        mMainImageView = (ImageView) layout.findViewById(R.id.detailMainImageImageView);

        String title = "상세정보 : " + mDetailInfo.getName();
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.mainToolbar);
        toolbar.setTitle(title);

        mMapLayout = (FrameLayout) layout.findViewById(R.id.detailMapLayout);
        mReviewRecyclerView = (RecyclerView) layout.findViewById(R.id.detailReviewRecyclerView);

        LinearLayout detailCreateReviewLayout = (LinearLayout) layout.findViewById(R.id.detailCreateReviewLayout);
        detailCreateReviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReviewDialog dialog = new ReviewDialog(v.getContext());

                dialog.show();
            }
        });

        return layout;
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("LOG/Detail", "onResume()");

        // 만약 일시중지된 이미지 변환 작업이 존재할 경우 다시 재개시킴.
        if(mBitmapConverter.getStatus() == AsyncTask.Status.RUNNING
                && isPausedConverting) {
            isPausedConverting = false;
            Log.d("LOG/Detail", "isPausedConverting : " + !isPausedConverting + " -> " + isPausedConverting);
            synchronized (mBitmapConverter) {
                Log.d("LOG/Detail", "BitmapConverter is notified");
                mBitmapConverter.notify();
            }
        }
    }

    @Override
    public void onPause() {
        Log.d("LOG/Detail", "onPause()");
        super.onPause();

        // 만약 이미지 변환 작업 중이라면 변환 작업을 일시중지 시킨다.
        if(mBitmapConverter.getStatus() == AsyncTask.Status.RUNNING) {
            isPausedConverting = true;
            Log.d("LOG/Detail", "isPausedConverting : " + !isPausedConverting + " -> " + isPausedConverting);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LOG/Detail", "onDestroy()");

        // 만약 Converter가 변환을 진행 중인데 상세보기를 종료시킬 경우 변환 작업 역시 중단시킴.
        if(mBitmapConverter != null)
            if(!mBitmapConverter.isCancelled()) {
                Log.d("LOG/Detail", "BitmapConverter is cancelled");
                mBitmapConverter.cancel(true);
            }
    }

    private void loadData() {
        if(Network.isNetworkConnected(getContext())) {
            // 리뷰를 제외한 여행지의 상세 정보를 가지고 오는 객체.
            // 리뷰를 제외한 이유는 처음 홈에서 상세정보 프래그먼트로 넘어올 때 리뷰를 호출하며
            // 사용자가 리뷰를 작성할 때 또다시 리뷰 정보를 불러오기 위해
            // 다른 정보들과 별개로 재호출할 수 있는 구조를 갖기 위함이다.
            DetailInfoManager detailInfoManager = new DetailInfoManager();

            String infoPhpName = "/new/GetDetailDataWithoutReview.php";
            detailInfoManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SharedData.SERVER_IP + infoPhpName, String.valueOf(mDetailInfo.getId()));

            // 리뷰를 가져오는 객체.
            DetailReviewManager detailReviewManager = new DetailReviewManager();

            String reviewPhpName = "/new/GetDetailReviewData.php";
            detailReviewManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SharedData.SERVER_IP + reviewPhpName, String.valueOf(mDetailInfo.getId()));
        } else {
            Toast.makeText(getActivity(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();
        }
    }

    private void initMapView() {
        // 맵 뷰 생성.
        MapView mapView = new MapView(getActivity());
        // API KEY 적용
        mapView.setDaumMapApiKey(mMapKey);
        // 맵뷰 레이아웃에 추가.
        mMapLayout.addView(mapView);
        // 맵 뷰의 중심점을 이동시키고 줌인/줌아웃을 가능하게 한다.
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(mDetailInfo.getLocationX(), mDetailInfo.getLocationY()), 1, true);
        mapView.zoomIn(true);
        mapView.zoomOut(true);

        // 위치를 파악하기 쉽게 하기 위해 마커를 추가한다.
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Sight Marker");
        marker.setTag(0);
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(mDetailInfo.getLocationX(), mDetailInfo.getLocationY()));
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        mapView.addPOIItem(marker);

        // ScrollView 안에 MapView가 있으므로 스크롤 뷰가 중첩된다.
        // MapView가 정상적으로 스크롤될 수 있도록 MapView에 Touch Event를 추가해준다.
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 상위 뷰인 NestedScrollView(Parent)가 Motion Event를 처리하지 못하도록 설정.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        // 다시 원래대로 돌려놓음.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
    }

    // 여행지 이름, 좋아요 수, 상세내용 등의 TextView를 초기화.
    private void initTextView() {
        TextView sightNameTextView = (TextView) getActivity().findViewById(R.id.detailSightNameTextView);
        sightNameTextView.setText(mDetailInfo.getName());
        TextView sightTagTextView = (TextView) getActivity().findViewById(R.id.detailSightTagTextView);
        sightTagTextView.setText(mDetailInfo.getTag());
        TextView sightRatingTextView = (TextView) getActivity().findViewById(R.id.detailSightRatingTextView);
        sightRatingTextView.setText(String.valueOf(mDetailInfo.getRating()));
        TextView sightInfoTextView = (TextView) getActivity().findViewById(R.id.detailSightInfoTextView);
        sightInfoTextView.setText(mDetailInfo.getInfo());
        TextView sightLikeCountTextView = (TextView) getActivity().findViewById(R.id.detailSightLikeCountTextView);
        sightLikeCountTextView.setText(String.valueOf(mDetailInfo.getLikeCount()));
    }

    private void initReview(List<Review> reviewList) {
        ReviewRecyclerViewAdapter adapter = new ReviewRecyclerViewAdapter(R.layout.i_detail_review, reviewList);
        mReviewRecyclerView.setAdapter(adapter);
    }

    private void registerProgressBar(int size) {
        // SubImageLayout에 추가할 ViewPager를 생성.
        ViewPager viewPager = (ViewPager) mSubImageLayout.findViewById(R.id.detailSubImageViewPager);

        // ViewPager에 부착할 Adapter 생성.
        // 생성자로 Context와 이미지의 개수를 받는다.
        mAdapter = new SubImageViewPagerAdapter(getContext(), size);
        viewPager.setAdapter(mAdapter);
    }

    private class DetailInfoManager extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.d("LOG/Detail", "DetailInfoManager doInBackground()");
            StringBuilder jsonHtml = new StringBuilder();
            try{
                String link = params[0];
                String sId = params[1];
                String data = URLEncoder.encode("SIGHT_ID", "UTF-8") + "=" + URLEncoder.encode(sId, "UTF-8");
                // 연결 url 설정
                URL url = new URL(link);
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
            try {
                Log.d("LOG/Detail", "DetailInfoManager onPostExecute()");
                JSONObject root = new JSONObject(result);
                JSONArray ja = root.getJSONArray("result");
                // result로 넘어오는 배열은 아래와 같다.
                // index 0 : 여행지 내용, 좋아요 수, 위치 등의 정보.
                // index 1 : 여행지에 해당하는 태그 이름들.
                // index 2 : 여행지의 이미지가 위치한 경로들.
                for(int i=0; i<ja.length(); i++) {
                    // index 0.
                    if(i==0) {
                        // 여행지 이름의 경우 홈 프래그먼트에서 Bundle을 통해 받아왔기 때문에
                        // 따로 설정해주지 않는다.
                        JSONObject jo = ja.getJSONObject(i);
                        mDetailInfo.setInfo(jo.getString("sight_info"));
                        mDetailInfo.setLikeCount(jo.getInt("sight_recommend_count"));
                        mDetailInfo.setLocationX(jo.getDouble("sight_location_x"));
                        mDetailInfo.setLocationY(jo.getDouble("sight_location_y"));

                        double sumPoint = jo.getDouble("sum_point");
                        int peopleCount = jo.getInt("p_count");

                        // 별점을 구하는 데 만약 총점이 0이거나 평가자가 없을 경우 0.0으로 설정.
                        if (sumPoint == 0 || peopleCount == 0)
                            mDetailInfo.setRating(0.0);
                        else {
                            // 순환소수가 나올 수 있으므로 둘째 자리에서 반올림.
                            String avgStr = String.format(Locale.getDefault() ,"%.1f", sumPoint / peopleCount);
                            mDetailInfo.setRating(Double.valueOf(avgStr));
                        }
                        initMapView();
                        Log.d("LOG/Detail", "Sight name : " + mDetailInfo.getName());
                    }
                    // index 1.
                    else if(i == 1) {
                        JSONArray tagArray = ja.getJSONArray(i);
                        String tag = "";
                        for(int j=0; j<tagArray.length(); j++) {
                            JSONObject obj = tagArray.getJSONObject(j);
                            tag += obj.getString("tag_name");

                            if(j==tagArray.length()-1)
                                break;
                            else if(j%2 == 0)
                                tag += ", ";
                            else
                                tag += "\n";
                        }
                        mDetailInfo.setTag(tag);
                        // index 0과 index 1에서 설정한 정보들을 바탕으로 화면에 출력.
                        initTextView();
                    }
                    //index 2.
                    else if(i == 2) {
                        // 여행지의 여러 이미지들 경로를 가지는 List 생성.
                        List<String> urlList = new ArrayList<>();
                        JSONArray urlArray = ja.getJSONArray(i);
                        for(int j=0; j<urlArray.length(); j++) {
                            JSONObject obj = urlArray.getJSONObject(j);
                            String path = obj.getString("sight_image_filepath");
                            String name = obj.getString("sight_image_filename");
                            urlList.add(SharedData.SERVER_IP + path + name);
                        }

                        // 이미지를 Bitmap으로 변환하는 동안 보여줄 ProgressBar를 설정.
                        registerProgressBar(urlList.size());

                        // 이미지 변환작업 시작.
                        mBitmapConverter = new BitmapConverter();
                        mBitmapConverter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlList);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private class DetailReviewManager extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.d("LOG/Detail", "DetailReviewManager doInBackground()");
            StringBuilder jsonHtml = new StringBuilder();
            try{
                String link = params[0];
                String sId = params[1];
                String data = URLEncoder.encode("SIGHT_ID", "UTF-8") + "=" + URLEncoder.encode(sId, "UTF-8");
                // 연결 url 설정
                URL url = new URL(link);
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
            try {
                Log.d("LOG/Detail", "DetailReviewManager onPostExecute()");
                JSONObject root = new JSONObject(result);
                JSONArray ja = root.getJSONArray("result");
                // 리뷰 정보들을 받아온다.
                List<Review> reviewList = new ArrayList<>();
                for(int i=0; i<ja.length(); i++) {
                    JSONObject obj = ja.getJSONObject(i);
                    String writer = obj.getString("review_writer");
                    String info = obj.getString("review_info");
                    String date = obj.getString("review_date");
                    String point = obj.getString("review_point");

                    Review review = new Review(writer, info, date, point);
                    reviewList.add(review);
                }
                Log.d("LOG/Detail", "Review data is loaded");
                initReview(reviewList);

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
            Log.d("LOG/Detail", "BitmapConverter is started");
            // 이미지의 경로가 저장되어 있는 list.
            List<String> list = (List<String>) params[0];
            Bitmap bitmap;
            URL newurl;
            for(int i=0; i<list.size(); i++) {
                try {
                    // 만약 프래그먼트의 종료 등으로 인해 변환 작업을 중지시켜야 할 경우.
                    if(isCancelled()) {
                        Log.d("LOG/Detail", "BitmapConverter is stopped");
                        return null;
                    }
                    if(isPausedConverting) {
                        Log.d("LOG/Detail", "BitmapConverter is waiting");
                        synchronized (mBitmapConverter) {
                            mBitmapConverter.wait();
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
            Log.d("LOG/Detail", "BitmapConverter onProgressUpdate()");
            // 변환된 Bitmap.
            Bitmap bitmap = (Bitmap) values[0];
            // item의 index.
            int index = (Integer) values[1];

            // MainImager를 설정하는 과정.
            if(index == 0) {
                Log.d("LOG/Detail", "Converting MainImage is completed");
                ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.detailMainImageLoadingProgressBar);
                progressBar.setVisibility(View.GONE);

                mMainImageView.setImageBitmap(bitmap);
                mMainImageView.setVisibility(View.VISIBLE);

            }
            // SubImage를 설정하는 과정.
            // MainImage가 SubImage의 0번 index가 된다.
            Log.d("LOG/Detail", "Converting SubImage " + index + " is completed ");
            // ProgressBar를 변환된 Bitmap으로 치환.
            mAdapter.registerSubImage(bitmap, index);
        }
    }
}