package test.superdroid.com.seoulguide.Detail;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;

import test.superdroid.com.seoulguide.R;
import test.superdroid.com.seoulguide.Util.Network;
import test.superdroid.com.seoulguide.Util.SharedData;

public class ReviewDialog extends Dialog {
    // 리뷰를 추가할 여행지 아이디.
    private int sightId;
    // 리뷰 작성자.
    private EditText mWriterEditText;
    // 리뷰 내용.
    private EditText mInfoEditText;
    // 리뷰 별점.
    private RatingBar mRatingBar;
    // 리뷰 정보들을 담을 객체.
    private Review mReview;
    // mReview 객체 정보를 화면에 보여주기 위해 ReviewRecyclerViewAdapter를 참조.
    private ReviewRecyclerViewAdapter mAdapter;

    public ReviewDialog(Context context, int id, ReviewRecyclerViewAdapter adapter) {
        super(context);
        sightId = id;
        mAdapter = adapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 레이아웃의 최상단에 존재하는 Title(Toolbar 등)을 없애준다.
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.d_detail_review);

        mWriterEditText = (EditText) findViewById(R.id.reviewWriterEditText);
        // EditText의 라인 수를 제한.
        mWriterEditText.addTextChangedListener(new TextWatcher()
        {
            String previousString = "";

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                previousString= s.toString();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (mWriterEditText.getLineCount() >= 2)
                {
                    mWriterEditText.setText(previousString);
                    mWriterEditText.setSelection(mWriterEditText.length());
                }
            }
        });

        mInfoEditText = (EditText) findViewById(R.id.reviewInfoEditText);
        // EditText의 라인 수를 제한.
        mInfoEditText.addTextChangedListener(new TextWatcher()
        {
            String previousString = "";

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                previousString= s.toString();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (mInfoEditText.getLineCount() >= 5)
                {
                    mInfoEditText.setText(previousString);
                    mInfoEditText.setSelection(mInfoEditText.length());
                }
            }
        });

        mRatingBar = (RatingBar) findViewById(R.id.reviewRatingBar);

        Button createButton = (Button ) findViewById(R.id.reviewCreateButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 작성자와 내용을 모두 입력했을 경우.
                if(!mWriterEditText.getText().toString().isEmpty()
                        && !mInfoEditText.getText().toString().isEmpty()) {
                    insertReview();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "내용을 확인해주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // 리뷰 정보를 얻어와 DB로 리뷰 정보를 전송.
    private void insertReview() {
        // 인터넷 연결 확인.
        if(Network.isNetworkConnected(getContext())) {
            String insertReviewPhpName = "/new/InsertReview.php";

            // 리뷰 내용 받아옴.
            String writer = mWriterEditText.getText().toString();
            String info = mInfoEditText.getText().toString();
            String sPoint = String.valueOf(mRatingBar.getRating());

            // 추가한 리뷰를 바로 화면에 보여주기 위해 현재 시간을 받아옴.
            Calendar cal = Calendar.getInstance( );
            String currentDate = String.format(Locale.getDefault(),"%04d-%02d-%02d %02d:%02d:%02d",
                    cal.get(Calendar.YEAR),
                    (cal.get(Calendar.MONTH) + 1),
                    cal.get(Calendar.DAY_OF_MONTH),

                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND)
            );
            // 리뷰 객체를 하나 생성.
            // 이 객체를 이용해서 방금 추가한 리뷰를 DB 연동 없이 화면에 보여준다.
            mReview = new Review(
                    writer,
                    info,
                    currentDate,
                    sPoint
            );

            // 리뷰 남기기 실행.
            ReviewCreatingManager reviewCreatingManager = new ReviewCreatingManager();
            reviewCreatingManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    SharedData.SERVER_IP + insertReviewPhpName,
                    String.valueOf(sightId),
                    writer,
                    info,
                    sPoint
            );
        } else {
            Toast.makeText(getContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();
        }
    }

    private class ReviewCreatingManager extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.d("LOG/DetailReview", "ReviewCreating doInBackground()");
            try{
                String link = params[0];
                String sId = params[1];
                String writer = params[2];
                String info = params[3];
                String sPoint = params[4];
                String data = URLEncoder.encode("SIGHT_ID", "UTF-8") + "=" + URLEncoder.encode(sId, "UTF-8");
                data += "&" + URLEncoder.encode("WRITER", "UTF-8") + "=" + URLEncoder.encode(writer, "UTF-8");
                data += "&" + URLEncoder.encode("INFO", "UTF-8") + "=" + URLEncoder.encode(info, "UTF-8");
                data += "&" + URLEncoder.encode("POINT", "UTF-8") + "=" + URLEncoder.encode(sPoint, "UTF-8");
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
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch(Exception ex){
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("LOG/DetailReview", "Review is created and is inserted");
            mAdapter.addItem(mReview);
            mAdapter.notifyDataSetChanged();
        }
    }
}
