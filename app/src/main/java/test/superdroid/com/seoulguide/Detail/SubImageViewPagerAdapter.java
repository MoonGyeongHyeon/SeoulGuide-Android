package test.superdroid.com.seoulguide.Detail;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import test.superdroid.com.seoulguide.R;

public class SubImageViewPagerAdapter extends PagerAdapter {
    private int mSize;
    private LayoutInflater mInflater;
    // SubImage를 보여줄 레이아웃을 관리하는 배열.
    private View[] mViewList;
    // Bitmap을 임시저장할 배열.
    private Bitmap[] mBitmapList;

    public SubImageViewPagerAdapter(Context context, int size) {
        mInflater = LayoutInflater.from(context);
        mSize = size;

        // mSize의 값만큼 배열을 생성.
        mViewList = new View[mSize];

        mBitmapList = new Bitmap[mSize];
    }

    @Override
    public int getCount() {
        return mSize;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // 만약 position에 해당하는 View가 생성되지 않았다면
        // 생성한 뒤 배열에 저장함.
        if(mViewList[position] == null) {
            Log.d("LOG/Detail", "SubImage page " + position + " is created" );
            View layout = mInflater.inflate(R.layout.i_detail_sub_image, null);

            // 만약 새로 생성하는 page에 임시저장된 Bitmap이 존재할 경우.
            // Bitmap을 설정해준다.
            if(mBitmapList[position] != null) {
                Log.d("LOG/Detail", "Temporary bitmap is got" );
                ImageView subImage = (ImageView) layout.findViewById(R.id.detailSubImageImageView);
                subImage.setImageBitmap(mBitmapList[position]);
                subImage.setVisibility(View.VISIBLE);

                ProgressBar progressBar = (ProgressBar) layout.findViewById(R.id.detailSubImageLoadingProgressBar);
                progressBar.setVisibility(View.GONE);

                // 사용한 Bitmap은 삭제한다.
                mBitmapList[position] = null;
            }
            mViewList[position] = layout;
        }
        // 현재 페이지 + 전/후의 페이지를 관리하는 container에 page가 정상적으로
        // 보여질 수 있도록 addView() 메서드를 호출한다.
        container.addView(mViewList[position]);

        return mViewList[position];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d("LOG/Detail", "SubImage page " + position + " is destroyed" );
        // 보이지 않는 page을 삭제시켜준다.
        container.removeView((View)object);
    }

    // Bitmap을 position에 해당하는 레이아웃의 ImageView에 치환함.
    void registerSubImage(Bitmap bitmap, int position) {
        View layout = mViewList[position];
        // instantiateItem() 메서드를 통해 초기화가 된 레이아웃일 경우.
        // 변환된 Bitmap을 바로 설정해준다.
        if(layout != null) {
            Log.d("LOG/Detail", "SubImage " + position + " is applied" );
            ImageView subImage = (ImageView) layout.findViewById(R.id.detailSubImageImageView);
            subImage.setImageBitmap(bitmap);
            subImage.setVisibility(View.VISIBLE);

            ProgressBar progressBar = (ProgressBar) layout.findViewById(R.id.detailSubImageLoadingProgressBar);
            progressBar.setVisibility(View.GONE);
        }
        // 만약 아직 레이아웃이 초기화되지 않은 경우.
        // Bitmap을 임시로 저장해둔다.
        else {
            Log.d("LOG/Detail", "SubImage " + position + " is temporarily saved" );
            mBitmapList[position] = bitmap;
        }
    }
}
