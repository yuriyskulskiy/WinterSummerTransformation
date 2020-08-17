package com.example.animated.article.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.animated.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnimatedLayout extends ConstraintLayout {

    private static final int SUMMER_STATE = 1;
    private static final int WINTER_STATE = 2;

    private static final int FROM_LEFT_TO_RIGHT = 1;
    private static final int FROM_RIGHT_TO_LEFT = 2;
    private static int IDLE_ANIMATION_STATE = 3;

    private ImageView mBackgroundImg;
    private TextView mTitleTV;
    private CircleImageView mProfileCircleIV;
    private CircleImageView mWeatherIcon;

    private float mOffsetValue = -1;
    private int mIdleState = SUMMER_STATE;
    private int mCurrentAnimation = IDLE_ANIMATION_STATE;

    private Path mPath;

    private Bitmap mScreenShotBitmap;

    public AnimatedLayout(Context context) {
        super(context);
        init();
    }

    public AnimatedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initIdleState(w);
        mScreenShotBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBackgroundImg = findViewById(R.id.background_image);
        mTitleTV = findViewById(R.id.title_tv);
        mProfileCircleIV = findViewById(R.id.profile_image);
        mWeatherIcon = findViewById(R.id.weatherIconIV);
    }

    private void init() {
        mPath = new Path();
    }

    private void initIdleState(int w) {
        float initOffset = (mIdleState == SUMMER_STATE) ? w : 0;
        updateOffset(initOffset);
        if (mIdleState == WINTER_STATE) {
            applyWinter();
        } else {
            applySummer();
        }
    }

    private void updateOffset(float newOffsetValue) {
        if (mOffsetValue == newOffsetValue) {
//            nothing to do
            return;
        }
        mOffsetValue = newOffsetValue;
        updateIconView();
    }

    private void updateIconView() {
        float oldTranslatePosition = mWeatherIcon.getTranslationX();
        float newTranslatePosition;
        float newAlpha;

        if (mOffsetValue < getWidth() / 2f) {
            newTranslatePosition = mOffsetValue;
            if (oldTranslatePosition >= 0) {
                applyWinterIcon();
            }
            float alphaPass = getWidth() / 2f;
            newAlpha = 1 - newTranslatePosition / alphaPass;

        } else {
            newTranslatePosition = mOffsetValue - getWidth();
            if (oldTranslatePosition <= 0) {
                applySummerIcon();
            }

            float alphaPass = getWidth() / 2f;
            newAlpha = 1 + newTranslatePosition / alphaPass;
            mWeatherIcon.setAlpha(newAlpha);
        }
        mWeatherIcon.setAlpha(newAlpha);
        mWeatherIcon.setTranslationX(newTranslatePosition);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setWillNotDraw(false);
        if (canvas instanceof ScreenShotCanvas) {
            super.onDraw(canvas);
            return;
        }
        if (mCurrentAnimation != IDLE_ANIMATION_STATE) {
            displayScreenShot(canvas);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (canvas instanceof ScreenShotCanvas) {
            if (isChildExcluded(child)) {
                return false;
            }
            return super.drawChild(canvas, child, drawingTime);
        }
        if (mCurrentAnimation != IDLE_ANIMATION_STATE) {
            if (isChildExcluded(child)) {
                return super.drawChild(canvas, child, drawingTime);
            }
            canvas.save();
            clipToRectangle(canvas, child);
            boolean result = super.drawChild(canvas, child, drawingTime);
            canvas.restore();
            return result;

        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }

    private void performScreenShot() {
        Canvas canvas = new ScreenShotCanvas(mScreenShotBitmap);
        this.draw(canvas);
    }

    private void displayScreenShot(Canvas canvas) {
        canvas.drawBitmap(mScreenShotBitmap, 0, 0, null);
    }

    private void clipToRectangle(Canvas canvas, View child) {
        mPath.reset();
        if (mCurrentAnimation == FROM_LEFT_TO_RIGHT) {
            mPath.addRect(
                    0,
                    0,
                    mOffsetValue,
                    getHeight(),
                    Path.Direction.CW);
            canvas.clipPath(mPath);

        } else if (mCurrentAnimation == FROM_RIGHT_TO_LEFT) {
            mPath.addRect(
                    mOffsetValue,
                    0,
                    getWidth(),
                    getHeight(),
                    Path.Direction.CW);
            canvas.clipPath(mPath);
        }
    }

    private boolean isChildExcluded(View child) {
        return child.getId() == R.id.weatherIconIV;
    }

    //region Methods to change Ui elements attributes for summer or winter style
    private void applySummer() {
        applySummerImageBackground();
        applySummerTitleTV();
        applySummerProfile();
    }

    private void applyWinter() {
        applyWinterImageBackground();
        applyWinterTitleTV();
        applyWinterProfile();
    }

    private void applySummerImageBackground() {
        mBackgroundImg.setImageResource(R.drawable.summer_550_309);
    }

    private void applyWinterImageBackground() {
        mBackgroundImg.setImageResource(R.drawable.winter_550_309);
    }

    private void applySummerTitleTV() {
        mTitleTV.setBackgroundResource(R.drawable.summer_text_background);
        mTitleTV.setTextColor(Color.BLACK);
        mTitleTV.setText("WINTER IS COMING...");

        // fix bug: text view with wrap_content layout param
        // does not resize its width and
        // last word of new text will not displayed if new string is longer
        mTitleTV.post(new Runnable() {
            @Override
            public void run() {
                AnimatedLayout.this.getParent().requestLayout();
            }
        });
    }

    private void applyWinterTitleTV() {
        mTitleTV.setBackgroundResource(R.drawable.winter_text_background);
        mTitleTV.setTextColor(Color.WHITE);
        mTitleTV.setText("WINTER IS HERE...");
        mTitleTV.post(new Runnable() {
            @Override
            public void run() {
                AnimatedLayout.this.getParent().requestLayout();
            }
        });
    }

    private void applySummerProfile() {
        mProfileCircleIV.setBorderColor(Color.WHITE);
        mProfileCircleIV.setImageResource(R.drawable.john_snow_200_200);
    }

    private void applyWinterProfile() {
        mProfileCircleIV.setBorderColor(Color.BLACK);
        mProfileCircleIV.setImageResource(R.drawable.night_king_200x200);
    }

    private void applySummerIcon() {
        mWeatherIcon.setImageResource(R.drawable.ic_sun);
        mWeatherIcon.setCircleBackgroundColorResource(R.color.weather_icon_background_summer);
        mWeatherIcon.setBorderColor(Color.WHITE);
    }

    private void applyWinterIcon() {
        mWeatherIcon.setCircleBackgroundColorResource(R.color.snowflake_background);
        mWeatherIcon.setImageResource(R.drawable.ic_snow);
        mWeatherIcon.setBorderColor(Color.BLACK);
    }
    //endregion

    public void animateBy(int dy) {
        float newOffset;
        if (dy > 0) {
            newOffset = (mOffsetValue - dy) < 0 ? 0 : mOffsetValue - dy;
        } else {
            //scroll to the left
            newOffset = (mOffsetValue - dy) > getWidth() ? getWidth() : mOffsetValue - dy;
        }

        if (mCurrentAnimation == IDLE_ANIMATION_STATE) {
            //start from idle state
            mCurrentAnimation = (dy < 0) ? FROM_LEFT_TO_RIGHT : FROM_RIGHT_TO_LEFT;

            performScreenShot();
            if (mIdleState == SUMMER_STATE) {
                applyWinter();
            } else {
                applySummer();
            }
        }

        if (newOffset == 0 || newOffset == getWidth()) {
            mIdleState = newOffset == 0 ? WINTER_STATE : SUMMER_STATE;
            if (mIdleState == SUMMER_STATE) {
                applySummer();
            } else {
                applyWinter();
            }
            mCurrentAnimation = IDLE_ANIMATION_STATE;
        }
        updateOffset(newOffset);
        invalidate();
    }

    public boolean isForwardAnimationPossible() {
        return mOffsetValue > 0;
    }

    public boolean isReverseAnimationPossible() {
        return mOffsetValue < getWidth();
    }

    public void invalidateText() {
        if (mIdleState == WINTER_STATE) {
            applyWinterTitleTV();
        } else {
            applySummerTitleTV();
        }
    }

    private static class ScreenShotCanvas extends Canvas {
        ScreenShotCanvas(Bitmap bitmap) {
            super(bitmap);
        }
    }
}

