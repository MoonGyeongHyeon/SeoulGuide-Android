package test.superdroid.com.seoulguide.Detail;

import android.Manifest;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

import test.superdroid.com.seoulguide.Bucket.BucketInnerDB;
import test.superdroid.com.seoulguide.R;
import test.superdroid.com.seoulguide.Util.MapViewManager;
import test.superdroid.com.seoulguide.Util.Network;
import test.superdroid.com.seoulguide.Util.PermissionChecker;
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
    // 맵 뷰를 담을 레이아웃.
    private FrameLayout mMapLayout;
    // 리뷰를 출력할 뷰.
    private RecyclerView mReviewRecyclerView;
    // 리뷰 출력을 위한 Adapter;
    private ReviewRecyclerViewAdapter mReviewAdapter;
    // 메인 이미지 프로그레스 바
    private ProgressBar mProgressBar;
    // 버킷리스트 추가를 위한 내부 DB
    private BucketInnerDB mBucketInnerDB;
    private MapView mMapView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetailInfo = new DetailInfo();

        mBitmapConverter = new BitmapConverter();

        mBucketInnerDB = BucketInnerDB.getIntance(getContext());

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
        Log.d("LOG/Detail", "onCreateView()");

        View layout = inflater.inflate(R.layout.f_sight_detail, container, false);

        mSubImageLayout = (LinearLayout) layout.findViewById(R.id.detailSubImageLayout);
        mMainImageView = (ImageView) layout.findViewById(R.id.detailMainImageImageView);

        String title = "상세정보 : " + mDetailInfo.getName();
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.mainToolbar);
        toolbar.setTitle(title);

        mMapLayout = (FrameLayout) layout.findViewById(R.id.detailMapLayout);
        mMapView = MapViewManager.getMapView(getActivity(), mMapLayout);

        mReviewRecyclerView = (RecyclerView) layout.findViewById(R.id.detailReviewRecyclerView);
        mReviewRecyclerView.setNestedScrollingEnabled(false);

        RatingBar createReviewRatingBar = (RatingBar) layout.findViewById(R.id.reviewCreateRatingBar);
        // RatingBar의 값을 바꿨을 때(터치했을 때) Dialog가 보여지도록.
        createReviewRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ReviewDialog dialog = new ReviewDialog(getContext(), mDetailInfo.getId(), mReviewAdapter);

                dialog.show();
            }
        });
        mProgressBar = (ProgressBar) layout.findViewById(R.id.detailMainImageLoadingProgressBar);

        ImageButton bucketImageButton = (ImageButton) layout.findViewById(R.id.detailBucketImageButton);
        bucketImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 상세정보를 전부 로드한 상태인지를 확인
                if (mDetailInfo != null && mMainImageView.getDrawable() != null) {
                    // 이미 추가한 여행지인지 확인
                    // 여행지가 존재하지 않을 때 추가
                    if (!mBucketInnerDB.isExistedSight(mDetailInfo.getId())) {
                        ContentValues values = new ContentValues();
                        values.put("_sight_id", mDetailInfo.getId());
                        values.put("_sight_name", mDetailInfo.getName());
                        values.put("_sight_x", mDetailInfo.getLocationX());
                        values.put("_sight_y", mDetailInfo.getLocationY());
                        values.put("_bitmap", getByteArrayFromDrawable(mMainImageView.getDrawable()));

                        mBucketInnerDB.insert(values);

                        Toast.makeText(getContext(), "버킷리스트에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                        Log.d("LOG/Detail", "Bucket data is putted, id : " + mDetailInfo.getId() + " , name : " + mDetailInfo.getName());
                    } else {
                        Toast.makeText(getContext(), "이미 추가한 여행지입니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "잠시 후 다시 시도해주십시오.", Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 내가 요청한 권한에 대한 응답인지 확인
        if(requestCode == PermissionChecker.REQUEST_FINE_LOCATION) {
            if(PermissionChecker.verifyPermission(grantResults)) {
                // 동의.
                Log.d("LOG/Detail", "Permission is granted");
                initMapView(mMapView);
            } else {
                // 거절.
                Log.d("LOG/Detail", "Permission is denied");
                Toast.makeText(getContext(), "위치 정보를 얻을 수 없어 지도를 이용할 수 없습니다.", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void loadData() {
        if(Network.isNetworkConnected(getContext())) {
            // 여행지의 상세 정보 등을 가지고 오는 객체.
            DetailInfoManager detailInfoManager = new DetailInfoManager();

            String infoPhpName = "/new/GetDetailData.php";
            detailInfoManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SharedData.SERVER_IP + infoPhpName, String.valueOf(mDetailInfo.getId()));

        } else {
            Toast.makeText(getActivity(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();
        }
    }

    private void initMapView(MapView mapView) {
        if(Build.VERSION.SDK_INT >= 23) {
            Log.d("LOG/Detail", "SDK Version : " + Build.VERSION.SDK_INT);
            if (!PermissionChecker.checkPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("LOG/Detail", "Permission is nothing and is requested");

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PermissionChecker.REQUEST_FINE_LOCATION);
                return ;
            }
        }

        // 맵 뷰의 중심점을 이동시키고 줌인/줌아웃을 가능하게 한다.
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(mDetailInfo.getLocationX(), mDetailInfo.getLocationY()), 1, true);
        mapView.zoomIn(true);
        mapView.zoomOut(true);

        // 위치를 파악하기 쉽게 하기 위해 마커를 추가한다.
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName(mDetailInfo.getName());
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
        mReviewAdapter = new ReviewRecyclerViewAdapter(R.layout.i_detail_review, reviewList);
        mReviewRecyclerView.setAdapter(mReviewAdapter);
    }

    private void registerProgressBar(int size) {
        // SubImageLayout에 추가할 ViewPager를 생성.
        ViewPager viewPager = (ViewPager) mSubImageLayout.findViewById(R.id.detailSubImageViewPager);

        // ViewPager에 부착할 Adapter 생성.
        // 생성자로 Context와 이미지의 개수를 받는다.
        mAdapter = new SubImageViewPagerAdapter(getContext(), size);
        viewPager.setAdapter(mAdapter);
    }

    public byte[] getByteArrayFromDrawable(Drawable d) {
        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();

        return data;
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
                // index 3 : 여행지의 리뷰 정보들.
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
                        initMapView(mMapView);

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
                    // index 3.
                    else if(i == 3) {
                        JSONArray reviewArray = ja.getJSONArray(i);
                        // 리뷰 정보들을 받아온다.
                        List<Review> reviewList = new ArrayList<>();
                        for(int j=0; j<reviewArray.length(); j++) {
                            JSONObject obj = reviewArray.getJSONObject(j);
                            String writer = obj.getString("review_writer");
                            String info = obj.getString("review_info");
                            String date = obj.getString("review_date");
                            String point = obj.getString("review_point");

                            Review review = new Review(writer, info, date, point);
                            reviewList.add(review);
                        }
                        Log.d("LOG/Detail", "Review data is loaded");
                        initReview(reviewList);
                    }
                }
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
                mProgressBar.setVisibility(View.GONE);

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