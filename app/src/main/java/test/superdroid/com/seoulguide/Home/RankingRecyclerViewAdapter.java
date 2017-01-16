package test.superdroid.com.seoulguide.Home;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        holder.picture.setImageResource(sightInfo.getPicture());
        holder.rating.setText(String.valueOf(sightInfo.getRating()));
        holder.likeCount.setText(String.valueOf(sightInfo.getLikeCount()));
        holder.name.setText(sightInfo.getName());
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

        public ViewHolder(View itemView) {
            super(itemView);

            rank = (TextView) itemView.findViewById(R.id.homeSightRankTextView);
            picture = (ImageView) itemView.findViewById(R.id.homeSightPictureImageView);
            rating = (TextView) itemView.findViewById(R.id.homeSightRatingTextView);
            likeCount = (TextView) itemView.findViewById(R.id.homeSightLikeCountTextView);
            name = (TextView) itemView.findViewById(R.id.homeSightNameTextView);
        }
    }
}
