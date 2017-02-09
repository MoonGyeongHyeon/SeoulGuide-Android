package test.superdroid.com.seoulguide.Home;

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

public class RankingRecyclerViewAdapter extends RecyclerView.Adapter<RankingRecyclerViewAdapter.ViewHolder>{

    private List<HomeSightInfo> mList;
    private int mItemLayout;
    private Fragment mFragment;

    public RankingRecyclerViewAdapter(List<HomeSightInfo> list, int itemLayout, Fragment fragment) {
        this.mList = list;
        this.mItemLayout = itemLayout;
        this.mFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mItemLayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final HomeSightInfo homeSightInfo = mList.get(position);
        holder.rank.setText(String.valueOf(homeSightInfo.getRank()));
        holder.rating.setText(String.valueOf(homeSightInfo.getRating()));
        holder.likeCount.setText(String.valueOf(homeSightInfo.getLikeCount()));
        holder.name.setText(homeSightInfo.getName());

        // Bitmap이 준비된 이미지만 출력시키고 준비가 되지 않았을 경우 ProgressBar를 보여줌.
        // 이 처리를 해주지 않을 경우, 재활용 이미지가 그대로 쓰이기 때문에 엉뚱한 이미지가 보여짐.
        if (homeSightInfo.getPicture() != null) {
            holder.picture.setImageBitmap(homeSightInfo.getPicture());
            holder.picture.setVisibility(View.VISIBLE);
            holder.imageLoading.setVisibility(View.GONE);
        } else {
            holder.picture.setVisibility(View.GONE);
            holder.imageLoading.setVisibility(View.VISIBLE);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CardView를 클릭했을 때 이동시킬 Fragment 생성.
                SightDetailFragment sightDetailFragment = new SightDetailFragment();
                Bundle bundle = new Bundle();
                // Bundle에 상세정보를 볼 여행지의 아이디를 설정.
                bundle.putInt("sightId", homeSightInfo.getId());
                // 툴바 타이틀에 여행지 이름을 출력시킬 수 있도록 하기 위해 여행지의 이름을 전달.
                bundle.putString("sightName", homeSightInfo.getName());
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
        TextView rank;
        ImageView picture;
        TextView rating;
        TextView likeCount;
        TextView name;
        ProgressBar imageLoading;
        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);

            rank = (TextView) itemView.findViewById(R.id.sightRankTextView);
            picture = (ImageView) itemView.findViewById(R.id.sightPictureImageView);
            rating = (TextView) itemView.findViewById(R.id.sightRatingTextView);
            likeCount = (TextView) itemView.findViewById(R.id.sightLikeCountTextView);
            name = (TextView) itemView.findViewById(R.id.sightNameTextView);
            imageLoading = (ProgressBar) itemView.findViewById(R.id.sightImageLoadingProgressBar);
            cardView = (CardView) itemView.findViewById(R.id.rankingCardView);
        }
    }
}
