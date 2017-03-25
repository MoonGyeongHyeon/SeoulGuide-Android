package test.superdroid.com.seoulguide.Bucket;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapView;

import java.util.List;

import test.superdroid.com.seoulguide.Detail.SightDetailFragment;
import test.superdroid.com.seoulguide.R;
import test.superdroid.com.seoulguide.Util.MapViewManager;

public class BucketRecyclerViewAdapter extends RecyclerView.Adapter<BucketRecyclerViewAdapter.ViewHolder> {

    private int mItemLayout;
    private List<BucketSight> mBucketSightList;
    private Fragment mFragment;
    private BucketInnerDB mBucketInnerDB;
    private MapView mMapView;
    private FrameLayout mMapLayout;

    public BucketRecyclerViewAdapter(int mItemLayout, List<BucketSight> mBucketSightList, Fragment mFragment, FrameLayout mMapLayout) {
        this.mItemLayout = mItemLayout;
        this.mBucketSightList = mBucketSightList;
        this.mFragment = mFragment;
        this.mMapLayout = mMapLayout;

        mMapView = MapViewManager.getMapView();
        mBucketInnerDB = BucketInnerDB.getIntance(this.mFragment.getContext());
    }

    @Override
    public BucketRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(mItemLayout, parent, false);
        return new BucketRecyclerViewAdapter.ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(BucketRecyclerViewAdapter.ViewHolder holder, int position) {
        final int pos = position;
        final BucketSight bucketSight = mBucketSightList.get(position);
        holder.mNameTextView.setText(bucketSight.getSightName());
        holder.mImageView.setImageBitmap(bucketSight.getSightImage());

        // 이미지를 터치했을 때 상세정보로 넘어가도록 함
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BucketSight bucketSight = mBucketSightList.get(pos);
                Fragment fragment = new SightDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("sightId", bucketSight.getSightId());
                bundle.putString("sightName", bucketSight.getSightName());
                fragment.setArguments(bundle);

                mMapLayout.removeAllViews();

                mFragment.getFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.bucketMainLayout, fragment)
                        .commit();
            }
        });
        // X 버튼 터치시 버킷리스트 삭제
        holder.mRemoveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mFragment.getContext());
                builder.setTitle(bucketSight.getSightName() + "을 버킷리스트에서 삭제하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 내부 DB에서 여행지 정보를 삭제
                                mBucketInnerDB.delete(bucketSight.getSightId());

                                // 버킷리스트 정보를 담고있는 리스트에서 삭제
                                mBucketSightList.remove(pos);
                                // 삭제된 정보를 화면에 갱신
                                notifyDataSetChanged();

                                // 지도에서 마커를 제거
                                // 지도가 가지고 있는 마커 정보를 모두 가져옴
                                MapPOIItem[] items = mMapView.getPOIItems();
                                // 마커 정보 중 삭제한 여행지의 마커를 제거
                                mMapView.removePOIItem(items[pos]);
                             }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mBucketSightList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mNameTextView;
        ImageView mImageView;
        ImageView mRemoveImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.bucketNameTextView);
            mImageView = (ImageView) itemView.findViewById(R.id.bucketImageView);
            mRemoveImageView = (ImageView) itemView.findViewById(R.id.bucketRemoveImageView);

        }
    }
}
