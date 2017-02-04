package test.superdroid.com.seoulguide.Detail;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import test.superdroid.com.seoulguide.R;

public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ViewHolder> {

    private int mItemLayout;
    private List<Review> mReviewList;

    public ReviewRecyclerViewAdapter(int mItemLayout, List<Review> mReviewList) {
        this.mItemLayout = mItemLayout;
        this.mReviewList = mReviewList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(mItemLayout, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Review review = mReviewList.get(position);
        holder.mWriterTextView.setText(review.getWriter());
        holder.mInfoTextView.setText(review.getInfo());
        holder.mPointTextView.setText(review.getPoint());
        holder.mRatingBar.setRating(Float.valueOf(review.getPoint()));
        holder.mDateTextView.setText(review.getDate());
    }

    @Override
    public int getItemCount() {
        return mReviewList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mWriterTextView;
        RatingBar mRatingBar;
        TextView mPointTextView;
        TextView mInfoTextView;
        TextView mDateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mWriterTextView = (TextView) itemView.findViewById(R.id.detailReviewWriterTextView);
            mRatingBar = (RatingBar) itemView.findViewById(R.id.detailReviewRatingBar);
            mPointTextView = (TextView) itemView.findViewById(R.id.detailReviewPointTextView);
            mInfoTextView = (TextView) itemView.findViewById(R.id.detailReviewInfoTextView);
            mDateTextView = (TextView) itemView.findViewById(R.id.detailReviewDateTextView);
        }
    }

    public void addItem(Review review) {
        mReviewList.add(0, review);
    }
}
