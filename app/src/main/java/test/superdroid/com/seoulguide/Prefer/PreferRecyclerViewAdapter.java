package test.superdroid.com.seoulguide.Prefer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import test.superdroid.com.seoulguide.Detail.SightDetailFragment;
import test.superdroid.com.seoulguide.R;

public class PreferRecyclerViewAdapter extends RecyclerView.Adapter<PreferRecyclerViewAdapter.ViewHolder> {

    private List<PreferSightInfo> mList;
    private int mItemLayout;
    private Fragment mFragment;

    public PreferRecyclerViewAdapter(List<PreferSightInfo> mList, int mItemLayout, Fragment mFragment) {
        this.mList = mList;
        this.mItemLayout = mItemLayout;
        this.mFragment = mFragment;
    }

    @Override
    public PreferRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mItemLayout, parent, false);
        return new PreferRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PreferRecyclerViewAdapter.ViewHolder holder, int position) {
        final PreferSightInfo preferSightInfo = mList.get(position);
        holder.mRank.setText(String.valueOf(preferSightInfo.getRank()));
        holder.mRating.setText(String.valueOf(preferSightInfo.getRating()));
        holder.mLikeCount.setText(String.valueOf(preferSightInfo.getLikeCount()));
        holder.mName.setText(preferSightInfo.getName());

        // Bitmap이 준비된 이미지만 출력시키고 준비가 되지 않았을 경우 ProgressBar를 보여줌.
        // 이 처리를 해주지 않을 경우, 재활용 이미지가 그대로 쓰이기 때문에 엉뚱한 이미지가 보여짐.
        if (preferSightInfo.getPicture() != null) {
            holder.mPicture.setImageBitmap(preferSightInfo.getPicture());
            holder.mPicture.setVisibility(View.VISIBLE);
            holder.mImageLoading.setVisibility(View.GONE);
        } else {
            holder.mPicture.setVisibility(View.GONE);
            holder.mImageLoading.setVisibility(View.VISIBLE);
        }

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CardView를 클릭했을 때 이동시킬 Fragment 생성.
                SightDetailFragment sightDetailFragment = new SightDetailFragment();
                Bundle bundle = new Bundle();
                // Bundle에 상세정보를 볼 여행지의 아이디를 설정.
                bundle.putInt("sightId", preferSightInfo.getId());
                // 툴바 타이틀에 여행지 이름을 출력시킬 수 있도록 하기 위해 여행지의 이름을 전달.
                bundle.putString("sightName", preferSightInfo.getName());
                sightDetailFragment.setArguments(bundle);

                mFragment.getActivity().getSupportFragmentManager()
                        .beginTransaction().replace(R.id.fragmentComponentLayout, sightDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mPicture;
        TextView mRank;
        TextView mRating;
        TextView mLikeCount;
        TextView mName;
        ProgressBar mImageLoading;
        CardView mCardView;

        public ViewHolder(View itemView) {
            super(itemView);

            mRank = (TextView) itemView.findViewById(R.id.sightRankTextView);
            mPicture = (ImageView) itemView.findViewById(R.id.sightPictureImageView);
            mRating = (TextView) itemView.findViewById(R.id.sightRatingTextView);
            mLikeCount = (TextView) itemView.findViewById(R.id.sightLikeCountTextView);
            mName = (TextView) itemView.findViewById(R.id.sightNameTextView);
            mImageLoading = (ProgressBar) itemView.findViewById(R.id.sightImageLoadingProgressBar);
            mCardView = (CardView) itemView.findViewById(R.id.rankingCardView);
        }
    }
}
