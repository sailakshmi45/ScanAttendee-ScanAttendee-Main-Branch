package com.globalnest.classes;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.globalnest.scanattendee.R;

public class TokenTextview extends AppCompatTextView {

    public TokenTextview(Context context) {
        super(context);
    }

    public TokenTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setCompoundDrawablesWithIntrinsicBounds(0, 0, selected ? R.drawable.img_cross : 0, 0);
    }
}
