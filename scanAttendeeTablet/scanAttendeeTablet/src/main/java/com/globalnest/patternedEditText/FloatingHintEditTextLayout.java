package com.globalnest.patternedEditText;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.globalnest.scanattendee.R;


public final class FloatingHintEditTextLayout extends FrameLayout {

    private static final float DEFAULT_PADDING_LEFT_RIGHT_DP = 6f;

    private EditText mEditText;
    private TextView mLabel;

    private final Animation inAnimation;
    private final Animation outAnimation;
    private boolean isAnimating;

    public FloatingHintEditTextLayout(Context context) {
        this(context, null);
    }

    public FloatingHintEditTextLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingHintEditTextLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context
                .obtainStyledAttributes(attrs, R.styleable.FloatingHintEditTextLayout);

        inAnimation = AnimationUtils.loadAnimation(getContext(),
                a.getResourceId(R.styleable.FloatingHintEditTextLayout_floatingHintInAnimation, R.anim.slide_in_top_old));
        inAnimation.setAnimationListener(showLabelAnimationListener);

        outAnimation = AnimationUtils.loadAnimation(getContext(),
                a.getResourceId(R.styleable.FloatingHintEditTextLayout_floatingHintOutAnimation, R.anim.slide_out_bottom));
        outAnimation.setAnimationListener(hideLabelAnimationListener);

        final int sidePadding = a.getDimensionPixelSize(
                R.styleable.FloatingHintEditTextLayout_floatingHintSidePadding,
                dipsToPix(DEFAULT_PADDING_LEFT_RIGHT_DP));

        mLabel = new TextView(context);
        mLabel.setPadding(sidePadding, 0, sidePadding, 0);
        mLabel.setVisibility(INVISIBLE);

        mLabel.setTextAppearance(context,
                a.getResourceId(R.styleable.FloatingHintEditTextLayout_floatingHintTextAppearance,
                        android.R.style.TextAppearance_Small)
        );

        addView(mLabel, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        a.recycle();
    }

    @Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            if (mEditText != null) {
                throw new IllegalArgumentException("We already have an EditText, can only have one");
            }

            final LayoutParams lp = new LayoutParams(params);
            lp.gravity = Gravity.BOTTOM;
            lp.topMargin = (int) mLabel.getTextSize()+6;// for gap
            params = lp;

            setEditText((EditText) child);
        }

        super.addView(child, index, params);
    }

    private void setEditText(EditText editText) {
        mEditText = editText;
        mEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    if (mLabel.getVisibility() == View.VISIBLE || isAnimating) {
                        hideLabel();
                    }
                } else {
                    if (mLabel.getVisibility() != View.VISIBLE & !isAnimating) {
                        showLabel();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
            public void onFocusChange(View view, boolean focused) {
                mLabel.setActivated(focused);
            }
        });

        mLabel.setText(mEditText.getHint());
    }

    /**
     * @return the {@link EditText} text input
     */
    public EditText getEditText() {
        return mEditText;
    }

    /**
     * @return the {@link TextView} label
     */
    public TextView getLabel() {
        return mLabel;
    }

    /**
     * Show the label using an animation
     */
    private void showLabel() {
        mLabel.startAnimation(inAnimation);
    }

    /**
     * Hide the label using an animation
     */
    private void hideLabel() {
        mLabel.startAnimation(outAnimation);
    }

    /**
     * Helper method to convert dips to pixels.
     */
    private int dipsToPix(float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps,
                getResources().getDisplayMetrics());
    }

    /**
     * Animation listener triggered when showing the label
     */
    private Animation.AnimationListener showLabelAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            isAnimating = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mLabel.setVisibility(View.VISIBLE);
            isAnimating = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };

    /**
     * Animation listener triggered when hiding the label
     */
    private Animation.AnimationListener hideLabelAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            isAnimating = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mLabel.setVisibility(View.GONE);
            isAnimating = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };
}