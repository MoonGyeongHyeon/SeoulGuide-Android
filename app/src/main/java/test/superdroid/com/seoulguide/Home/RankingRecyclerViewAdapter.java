package test.superdroid.com.seoulguide.Home;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import test.superdroid.com.seoulguide.R;

public class RankingRecyclerViewAdapter extends RecyclerView.Adapter<RankingRecyclerViewAdapter.ViewHolder>{

    private List<SightInfo> list;
    private int itemLayout;

    public RankingRecyclerViewAdapter(List<SightInfo> list, int itemLayout) {
        this.list = list;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SightInfo sightInfo = list.get(position);
        holder.rank.setText(String.valueOf(sightInfo.getRank()));
        holder.rating.setText(String.valueOf(sightInfo.getRating()));
        holder.likeCount.setText(String.valueOf(sightInfo.getLikeCount()));
        holder.name.setText(sightInfo.getName());

        // Bitmap이 준비된 이미지만 출력시키고 준비가 되지 않았을 경우 ProgressBar를 보여줌.
        // 이 처리를 해주지 않을 경우, 재활용 이미지가 그대로 쓰이기 때문에 엉뚱한 이미지가 보여짐.
        if (sightInfo.getPicture() != null) {
            holder.picture.setImageBitmap(sightInfo.getPicture());
            holder.picture.setVisibility(View.VISIBLE);
            holder.imageLoading.setVisibility(View.GONE);
        } else {
            holder.picture.setVisibility(View.GONE);
            holder.imageLoading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank;
        ImageView picture;
        TextView rating;
        TextView likeCount;
        TextView name;
        ProgressBar imageLoading;

        public ViewHolder(View itemView) {
            super(itemView);

            rank = (TextView) itemView.findViewById(R.id.homeSightRankTextView);
            picture = (ImageView) itemView.findViewById(R.id.homeSightPictureImageView);
            rating = (TextView) itemView.findViewById(R.id.homeSightRatingTextView);
            likeCount = (TextView) itemView.findViewById(R.id.homeSightLikeCountTextView);
            name = (TextView) itemView.findViewById(R.id.homeSightNameTextView);
            imageLoading = (ProgressBar) itemView.findViewById(R.id.homeImageLoadingProgressBar);
        }
    }
}
