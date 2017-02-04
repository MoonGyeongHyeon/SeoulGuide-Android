package test.superdroid.com.seoulguide.Detail;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.EditText;

import test.superdroid.com.seoulguide.R;

public class ReviewDialog extends Dialog {

    public ReviewDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 레이아웃의 최상단에 존재하는 Title(Toolbar 등)을 없애준다.
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.d_detail_review);

        // EditText의 라인 수를 제한.
        final EditText writerEditText = (EditText) findViewById(R.id.reviewWriterEditText);
        writerEditText.addTextChangedListener(new TextWatcher()
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
                if (writerEditText.getLineCount() >= 2)
                {
                    writerEditText.setText(previousString);
                    writerEditText.setSelection(writerEditText.length());
                }
            }
        });

        final EditText infoEditText = (EditText) findViewById(R.id.reviewInfoEditText);
        infoEditText.addTextChangedListener(new TextWatcher()
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
                if (infoEditText.getLineCount() >= 5)
                {
                    infoEditText.setText(previousString);
                    infoEditText.setSelection(infoEditText.length());
                }
            }
        });
    }
}
