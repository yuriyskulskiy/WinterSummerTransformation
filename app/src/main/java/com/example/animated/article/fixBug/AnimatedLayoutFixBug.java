package com.example.animated.article.fixBug;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.animated.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class AnimatedLayoutFixBug extends ConstraintLayout {

    // item can have only two states
    private static final int SUMMER_STATE = 1;
    private static final int WINTER_STATE = 2;

    // animation state constants
    private static final int FROM_LEFT_TO_RIGHT = 1;
    private static final int FROM_RIGHT_TO_LEFT = 2;
    private static int IDLE_ANIMATION_STATE = 3;

    private int mCurrentAnimation = IDLE_ANIMATION_STATE;
    private int mIdleState = SUMMER_STATE;
    // children views
    private ImageView mBackgroundImg;
    private TextView mTitleTV;
    private CircleImageView mProfileCircleIV;
    private int currentAnimation = IDLE_ANIMATION_STATE;

    private Path path; // path for clipping visible part


    //main parameter which defines the ui state of the views during transformation
    // and clipping path width
    private float offsetValue = -1;

    private float previousTouchX = -1;

    private Bitmap screenShotBitmap;

    public AnimatedLayoutFixBug(Context context) {
        super(context);
        init();
    }

    public AnimatedLayoutFixBug(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedLayoutFixBug(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        path = new Path();
        Log.e("WTF", "init");


    }

    private void drawScreenShot(Canvas canvas) {
//        canvas.drawBitmap(screenShotBitmap, 0, 0, null);
        canvas.drawBitmap(screenShotBitmap, 0, 0, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setWillNotDraw(false);
        if (canvas instanceof AnimatedLayoutFixBug.ScreenShotCanvas) {
            super.onDraw(canvas);
            return;
        }
        if (mCurrentAnimation != IDLE_ANIMATION_STATE) {
            drawScreenShot(canvas);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (canvas instanceof AnimatedLayoutFixBug.ScreenShotCanvas) {
//            if (isChildExcluded(child)) {
//                return false;
//            }
            return super.drawChild(canvas, child, drawingTime);
        }

        if (mCurrentAnimation != IDLE_ANIMATION_STATE) {
//            if (isChildExcluded(child)) {
//                return super.drawChild(canvas, child, drawingTime);
//            }

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
        path.reset();
        if (mCurrentAnimation == FROM_LEFT_TO_RIGHT) {
            path.addRect(
                    0,
                    0,
                    offsetValue,
                    getHeight(),
                    Path.Direction.CW);
            canvas.clipPath(path);

        } else if (mCurrentAnimation == FROM_RIGHT_TO_LEFT) {
            path.addRect(
                    offsetValue,
                    0,
                    getWidth(),
                    getHeight(),
                    Path.Direction.CW);
            canvas.clipPath(path);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        offsetValue = (mIdleState == SUMMER_STATE) ? getWidth() : 0;
        initIdleState(w);
        updateOffset(offsetValue);


        screenShotBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
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

    //region apply summer or inter state
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

    private void updateOffset(float newOffsetValue) {
        if (offsetValue == newOffsetValue) {
//            nothing to do
            return;
        }
        offsetValue = newOffsetValue;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initUiComponents();
    }

    private void initUiComponents() {
        mBackgroundImg = findViewById(R.id.background_image);

        mTitleTV = findViewById(R.id.title_tv);
        mProfileCircleIV = findViewById(R.id.profile_image);
        mProfileCircleIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "CLicked", Toast.LENGTH_SHORT).show();
            }
        });
    }


    boolean isTouching = false;

    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                previousTouchX = event.getX();

                return true;
            case MotionEvent.ACTION_MOVE:

                float dx = event.getX() - previousTouchX;
                previousTouchX = event.getX();

                // check is it actually scroll
                if (dx == 0) {
                    return true;
                }

                //check is scrolling possible
                if (!isScrollingPossible(dx)) {
                    return false;
                }


                //safe scrolling if delta scroll is bigger then possible scroll distnace clamp to the possible distance
                float newOffset;
                if (dx > 0) {
                    newOffset = (offsetValue + dx) > getWidth() ? getWidth() : offsetValue + dx;
                } else {
                    //scroll to the left
                    newOffset = (offsetValue + dx) < 0 ? 0 : offsetValue + dx;
                }

                // detect the moment when user actually starts to scroll and do some preparation
                if (mCurrentAnimation == IDLE_ANIMATION_STATE) {
                    // we are about to start scrolling - need do some preparation action
                    mCurrentAnimation = (dx > 0) ? FROM_LEFT_TO_RIGHT : FROM_RIGHT_TO_LEFT;

                    performScreenShot();
//                    offsetValue should be 0 or getWidth in this case
                    if (offsetValue == getWidth()) {
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
                previousTouchX = -1;
                if (offsetValue < getWidth() / 2f) {
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


    private void performScreenShot() {
        Canvas canvas = new ScreenShotCanvas(screenShotBitmap);
        this.draw(canvas);
    }

    private static class ScreenShotCanvas extends Canvas {
        public ScreenShotCanvas(Bitmap bitmap) {
            super(bitmap);
        }
    }


    private boolean needToFling() {
        return 0 < offsetValue && offsetValue < getWidth();
    }

    void applyIdleState() {
        mCurrentAnimation = IDLE_ANIMATION_STATE;
        previousTouchX = -1;
        if (offsetValue < getWidth() / 2f) {
            mIdleState = WINTER_STATE;
            updateOffset(0);
            applyWinter();
        } else {
            mIdleState = SUMMER_STATE;
            updateOffset(getWidth());
            applySummer();
        }
    }

    private boolean isScrollingPossible(float dx) {
        if (dx == 0) {
            //no scrolling
            throw new IllegalArgumentException("dx cant not be 0 in this block");
        }

        //todo leave it for explanation
//        if ((dx > 0 && !canScrollToRight(dx)) || dx < 0 && !canScrollToLeft(dx)) {
//            return false;
//        } else {
//            return true;
//        }
        return (!(dx > 0) || canScrollToRight(dx))
                && (!(dx < 0) || canScrollToLeft(dx));
    }

    private boolean canScrollToRight(float dx) {
        return offsetValue < getWidth();
    }

    private boolean canScrollToLeft(float dx) {
        return offsetValue > 0;
    }

//    FlingAnimation fling = new FlingAnimation(this, new FloatPropertyCompat<AnimatedLayoutFixBug>("offset") {
//        @Override
//        public float getValue(AnimatedLayoutFixBug object) {
//            return object.getOffset();
//
//        }
//
//        @Override
//        public void setValue(AnimatedLayoutFixBug object, float value) {
//            Log.e("fling", "val =" + value);
//            object.updateOffset(value);
//            object.invalidate();
//        }
//    });

//    private float getOffset() {
//        return offsetValue;
//    }

//    @Override
//    public void setScrollX(int value) {
//        Log.e("fling", "val =" + value);
//        updateOffset(value);
//        invalidate();
//    }

    // helper  util method
//    private static float convertDpToPixel(float valueDp, Context context) {
//        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueDp,
//                displayMetrics);
//    }
}

class asd {
    private Bitmap mScreenShotBitmap;


}