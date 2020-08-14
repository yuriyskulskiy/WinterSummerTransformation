package com.example.animated.article.medium;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.animated.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnimatedLayoutFixed extends ConstraintLayout {

    // idle layout state
    private static final int SUMMER_STATE = 1;
    private static final int WINTER_STATE = 2;

    // animation state constants
    private static final int FROM_LEFT_TO_RIGHT = 1;
    private static final int FROM_RIGHT_TO_LEFT = 2;
    private static int IDLE_ANIMATION_STATE = 3;

    private int mCurrentAnimation = IDLE_ANIMATION_STATE;
    //current layout state
    private int mIdleState = SUMMER_STATE;

    // child views
    private ImageView mBackgroundImg;
    private TextView mTitleTV;
    private CircleImageView mProfileCircleIV;


    //main parameter which defines the ui state of the views during transformation
    // and the width of the Path for clipping
    private float mOffsetValue = -1;

    //path for clipping. Is used to make only specified area to be drawn and visible
    private Path mPath;
    private float mPreviousTouchX;

    public AnimatedLayoutFixed(Context context) {
        super(context);
        init();
    }

    public AnimatedLayoutFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedLayoutFixed(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPath = new Path();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
//        if (canvas instanceof AnimatedLayoutTest.ScreenShotCanvas) {
////            if (isChildExcluded(child)) {
////                return false;
////            }
//            return super.drawChild(canvas, child, drawingTime);
//        }

        if (mCurrentAnimation != IDLE_ANIMATION_STATE) {
//            if (isChildExcluded(child)) {
//                return super.drawChild(canvas, child, drawingTime);
//            }
            Log.e("WTF", "clipping");
            canvas.save();
            clipToRectangle(canvas, child);
            boolean result = super.drawChild(canvas, child, drawingTime);
            canvas.restore();
            return result;

        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }

    private void clipToRectangle(Canvas canvas, View child) {
        mPath.reset();
//        Log.e("clipP", "clipToRectangle mAnim= "+mCurrentAnimation);
        if (mCurrentAnimation == FROM_LEFT_TO_RIGHT) {
            mPath.addRect(
                    0,
                    0,
                    mOffsetValue,
                    getHeight(),
                    Path.Direction.CW);
            Log.e("clipP", mPath.toString());
            canvas.clipPath(mPath);
        } else if (mCurrentAnimation == FROM_RIGHT_TO_LEFT) {
            mPath.addRect(
                    mOffsetValue,
                    0,
                    getWidth(),
                    getHeight(),
                    Path.Direction.CW);
            Log.e("clipP", mPath.toString());
            canvas.clipPath(mPath);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initIdleState(w);
    }

    //We can't use findViewById() from init() method because children are null yet
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBackgroundImg = findViewById(R.id.background_image);
        mTitleTV = findViewById(R.id.title_tv);
        mProfileCircleIV = findViewById(R.id.profile_image);
    }


    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:

                float dx = event.getX() - mPreviousTouchX;
                mPreviousTouchX = event.getX();

                // check whether it was scrolled
                if (dx == 0) {
                    return false;
                }

                //check whether scrolling is  possible,
                // if offset is 0 - scroll from left to right is impossible
                // same when offset = fetWidth() scrolling to the right is impossible
                if (!isScrollingPossible(dx)) {
                    return false;
                }

                //safe scrolling if delta scroll is bigger then possible scroll distance  and clamp to the possible distance
                float newOffset;
                if (dx > 0) {
                    newOffset = (mOffsetValue + dx) > getWidth() ? getWidth() : mOffsetValue + dx;
                } else {
                    //scroll to the left
                    newOffset = (mOffsetValue + dx) < 0 ? 0 : mOffsetValue + dx;
                }

                // detect the moment when user actually starts to scroll
                // and do some preparation
                if (mCurrentAnimation == IDLE_ANIMATION_STATE) {
                    // we are about to start scrolling - need do some preparation action
                    mCurrentAnimation = (dx > 0)
                            ? FROM_LEFT_TO_RIGHT : FROM_RIGHT_TO_LEFT;
                    Log.e("ANIM", " animation = " + mCurrentAnimation);
//                    ormScreenShot(perf);
//                    offsetValue should be 0 or getWidth in this case
                    if (mOffsetValue == getWidth()) {
                        applyWinter();
                    } else {
                        applySummer();
                    }
                }
                updateOffset(newOffset);
                invalidate();
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                mCurrentAnimation = IDLE_ANIMATION_STATE;
                mPreviousTouchX = -1;
                if (mOffsetValue < getWidth() / 2f) {
                    mIdleState = WINTER_STATE;
                    updateOffset(0);
                    applyWinter();
                } else {
                    mIdleState = SUMMER_STATE;
                    updateOffset(getWidth());
                    applySummer();
                }
                invalidate();
                return true;
            default:
                throw new IllegalStateException("Unexpected value: " + event.getActionMasked());
        }
    }

    private boolean isScrollingPossible(float dx) {
        if (dx == 0) {
            //no scrolling
            throw new IllegalArgumentException("dx cant not be 0 in this block");
        }
        return (!(dx > 0) || canScrollToRight(dx))
                && (!(dx < 0) || canScrollToLeft(dx));
    }

    private boolean canScrollToRight(float dx) {
        return mOffsetValue < getWidth();
    }

    private boolean canScrollToLeft(float dx) {
        return mOffsetValue > 0;
    }

    // depending on idle state - init offsetValue variable with 0
    // or layout width value
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

        // fix bug: text view with wrap_content layout param does not resize its width and
        // last word of new text will not displayed if new string is longer
        mTitleTV.post(new Runnable() {
            @Override
            public void run() {
                mTitleTV.requestLayout();
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
                mTitleTV.requestLayout();
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
    //endregion

}
