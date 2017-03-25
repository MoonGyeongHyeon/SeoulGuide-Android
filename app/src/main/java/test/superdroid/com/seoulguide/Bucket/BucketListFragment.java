package test.superdroid.com.seoulguide.Bucket;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.List;

import test.superdroid.com.seoulguide.R;
import test.superdroid.com.seoulguide.Util.MapViewManager;
import test.superdroid.com.seoulguide.Util.PermissionChecker;
import test.superdroid.com.seoulguide.Util.SharedData;

public class BucketListFragment extends Fragment {

    private List<BucketSight> mBucketSightList;
    private BucketInnerDB mBucketInnerDB;
    private FrameLayout mMapLayout;
    private MapView mMapView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LOG/Bucket", "onCreate()");

        mBucketInnerDB = BucketInnerDB.getIntance(getContext());

        mBucketSightList = mBucketInnerDB.selectSightList();
        Log.d("LOG/Bucket", "Bucket size : " + mBucketSightList.size());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("LOG/Bucket", "onCreateView()");

        View layout = inflater.inflate(R.layout.f_bucket_sight_list, container, false);

        mMapLayout = (FrameLayout) layout.findViewById(R.id.bucketMapLayout);
        mMapView = MapViewManager.getMapView(getActivity(), mMapLayout);
        initMapView(mMapView);

        BucketRecyclerViewAdapter adapter = new BucketRecyclerViewAdapter(R.layout.i_bucket, mBucketSightList, this, mMapLayout);

        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.bucketRecyclerView);
        recyclerView.setAdapter(adapter);

        GridLayoutManager manager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(manager);

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("LOG/Bucket", "onDestroyView()");

    }

    private void initMapView(MapView mapView) {
        if (Build.VERSION.SDK_INT >= 23) {
            Log.d("LOG/Bucket", "SDK Version : " + Build.VERSION.SDK_INT);
            if (!PermissionChecker.checkPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("LOG/Bucket", "Permission is nothing and is requested");

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PermissionChecker.REQUEST_FINE_LOCATION);
                return;
            }
        }

        // 맵 뷰의 중심점을 이동시키고 줌인/줌아웃을 가능하게 한다.
        mapView.setZoomLevel(5, true);
        mapView.zoomIn(true);
        mapView.zoomOut(true);

        // 버킷리스트 여행지들의 좌표를 평균내어 지도의 중심점을 잡음
        double centerLocationX=0.0, centerLocationY=0.0;

        for (int i = 0; i < mBucketSightList.size(); i++) {
            BucketSight bucketSight = mBucketSightList.get(i);

            // 위치를 파악하기 쉽게 하기 위해 마커를 추가한다.
            MapPOIItem marker = new MapPOIItem();
            marker.setItemName(bucketSight.getSightName());
            marker.setTag(0);
            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(bucketSight.getLocationX(), bucketSight.getLocationY()));
            marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
            mapView.addPOIItem(marker);

            centerLocationX += bucketSight.getLocationX();
            centerLocationY += bucketSight.getLocationY();
        }

        centerLocationX /= mBucketSightList.size();
        centerLocationY /= mBucketSightList.size();

        // 중심점 설정
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(centerLocationX, centerLocationY), true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 내가 요청한 권한에 대한 응답인지 확인
        if (requestCode == PermissionChecker.REQUEST_FINE_LOCATION) {
            if (PermissionChecker.verifyPermission(grantResults)) {
                // 동의.
                Log.d("LOG/Bucket", "Permission is granted");
                initMapView(mMapView);
            } else {
                // 거절.
                Log.d("LOG/Bucket", "Permission is denied");
                Toast.makeText(getContext(), "위치 정보를 얻을 수 없어 지도를 이용할 수 없습니다.", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("LOG/Bucket", "onResume()");

        // 뒤로가기 등으로 다시 버킷리스트 프래그먼트에 돌아왔을 때 툴바 타이틀을 복구시킨다.
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.mainToolbar);
        toolbar.setTitle("버킷리스트");
    }
}
