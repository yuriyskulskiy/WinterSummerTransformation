package com.example.animated.article;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

import com.example.animated.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnimatedLayoutTest extends ConstraintLayout {

    private float MIN_FLING_START_VELOCITY_DP = 500;
    private float currentMinFlingVelocityPx;
    // item can have only two states
    private static final int SUMMER_STATE = 1;
    private static final int WINTER_STATE = 2;
    private int idleState = SUMMER_STATE;

    // animation state constants
    private static final int FROM_LEFT_TO_RIGHT = 1;
    private static final int FROM_RIGHT_TO_LEFT = 2;
    private static int IDLE_ANIMATION_STATE = 3;
    private int currentAnimation = IDLE_ANIMATION_STATE;

    private Path path; // path for clipping visible part

    // children views
    private ImageView backgroundImage;
    private TextView titleTV;
    private CircleImageView profileCircleView;

    //main parameter which defines the ui state of the views during transformation
    // and clipping path width
    private float offsetValue = -1;

    private float previousTouchX = -1;

    private Bitmap screenShotBitmap;

    public AnimatedLayoutTest(Context context) {
        super(context);
        init();
    }

    public AnimatedLayoutTest(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedLayoutTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        path = new Path();
        Log.e("WTF", "init");

        fling.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                Log.e("WTF", "onAnimationEnd canceled = " + canceled);
                if (!canceled) {
                    if (offsetValue == 0 || offsetValue == getWidth()) {
                        //correct ending
                        currentAnimation = IDLE_ANIMATION_STATE;
                        if (offsetValue == 0) {
                            idleState = WINTER_STATE;
                            applyWinter();
                        } else {
                            idleState = SUMMER_STATE;
                            applySummer();
                            Log.e("WTF", "summer state");
                        }
                    } else {
                        applyIdleState();
                    }
                } else {
                    if (!isTouching) {
                        applyIdleState();
                    }
                    Log.e("WTF", "canceled");
                }
                invalidate();
            }
        });
    }

    private void drawScreenShot(Canvas canvas) {
        canvas.drawBitmap(screenShotBitmap, 0, 0, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setWillNotDraw(false);
        if (canvas instanceof ScreenShotCanvas) {
            super.onDraw(canvas);
            return;
        }
        if (currentAnimation != IDLE_ANIMATION_STATE) {
            drawScreenShot(canvas);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (canvas instanceof ScreenShotCanvas) {
//            if (isChildExcluded(child)) {
//                return false;
//            }
            return super.drawChild(canvas, child, drawingTime);
        }

        if (currentAnimation != IDLE_ANIMATION_STATE) {
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
        if (currentAnimation == FROM_LEFT_TO_RIGHT) {
            path.addRect(
                    0,
                    0,
                    offsetValue,
                    getHeight(),
                    Path.Direction.CW);
            canvas.clipPath(path);

        } else if (currentAnimation == FROM_RIGHT_TO_LEFT) {
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

        offsetValue = (idleState == SUMMER_STATE) ? getWidth() : 0;
        initIdleState(w);
        updateOffset(offsetValue);

        currentMinFlingVelocityPx = convertDpToPixel(MIN_FLING_START_VELOCITY_DP, getContext());
        Log.e("VELOCITY", "currentMinFlingVelocityPx" + currentMinFlingVelocityPx);

        screenShotBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    private void initIdleState(int w) {
        float initOffset = (idleState == SUMMER_STATE) ? w : 0;
        updateOffset(initOffset);
        if (idleState == WINTER_STATE) {
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
        backgroundImage.setImageResource(R.drawable.summer_550_309);
    }

    private void applyWinterImageBackground() {
        backgroundImage.setImageResource(R.drawable.winter_550_309);
    }

    private void applySummerTitleTV() {
        titleTV.setBackgroundResource(R.drawable.summer_text_background);
        titleTV.setTextColor(Color.BLACK);
        titleTV.setText("WINTER IS COMING...");
        titleTV.post(new Runnable() {
            @Override
            public void run() {
                titleTV.requestLayout();
            }
        });
    }

    private void applyWinterTitleTV() {
        titleTV.setBackgroundResource(R.drawable.winter_text_background);
        titleTV.setTextColor(Color.WHITE);
        titleTV.setText("WINTER IS HERE...");
        titleTV.post(new Runnable() {
            @Override
            public void run() {
                titleTV.requestLayout();
            }
        });
    }

    private void applySummerProfile() {
        profileCircleView.setBorderColor(Color.WHITE);
        profileCircleView.setImageResource(R.drawable.john_snow_200_200);
    }

    private void applyWinterProfile() {
        profileCircleView.setBorderColor(Color.BLACK);
        profileCircleView.setImageResource(R.drawable.night_king_200x200);
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
        backgroundImage = findViewById(R.id.background_image);

        titleTV = findViewById(R.id.title_tv);
        profileCircleView = findViewById(R.id.profile_image);
        profileCircleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "CLicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    VelocityTracker velocityTracker;

    boolean isTouching = false;

    public boolean onTouchEvent(MotionEvent event) {
        Log.e("onTouch", "ont touch");
        isTouching = true;
        fling.cancel();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                previousTouchX = event.getX();


                if (velocityTracker == null) {

                    // Retrieve a new VelocityTracker object to watch the velocity
                    // of a motion.
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    // Reset the velocity tracker back to its initial state.
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
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
                if (currentAnimation == IDLE_ANIMATION_STATE) {
                    // we are about to start scrolling - need do some preparation action
                    currentAnimation = (dx > 0) ? FROM_LEFT_TO_RIGHT : FROM_RIGHT_TO_LEFT;

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

                if (needToFling()) {
                    velocityTracker.addMovement(event);
                    velocityTracker.computeCurrentVelocity(1000);
                    float velocityX = velocityTracker.getXVelocity();

                    if (Math.abs(velocityX) < currentMinFlingVelocityPx) {
                        if (offsetValue < getWidth() / 2f) {
                            velocityX = -currentMinFlingVelocityPx;
                        } else {
                            velocityX = +currentMinFlingVelocityPx;
                        }
                    }
                    fling.setStartVelocity(velocityX)
                            .setMinValue(0)
                            .setMaxValue(getWidth())
                            .setFriction(0.1f)
                            .start();
                    previousTouchX = -1;
                } else {
                    applyIdleState();
                    invalidate();
                }


                Log.e("VELOCITY", "VELOCITY speed" + velocityTracker.getXVelocity());
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
//                currentAnimation = IDLE_ANIMATION_STATE;
//                previousTouchX = -1;
//                if (offsetValue < getWidth() / 2f) {
//                    idleState = WINTER_STATE;
//                    updateOffset(0);
//                    applyWinter();
//                } else {
//                    idleState = SUMMER_STATE;
//                    updateOffset(getWidth());
//                    applySummer();
//                }
//                invalidate();
                isTouching = false;
                return true;
            default:
                throw new IllegalStateException("Unexpected value: " + event.getActionMasked());
        }
    }


    private void performScreenShot() {
        Canvas canvas = new ScreenShotCanvas(screenShotBitmap);
        this.draw(canvas);
    }

    private class ScreenShotCanvas extends Canvas {
        public ScreenShotCanvas(Bitmap bitmap) {
            super(bitmap);
        }
        //just a marked type
    }


    private boolean needToFling() {
        return 0 < offsetValue && offsetValue < getWidth();
    }

    void applyIdleState() {
        currentAnimation = IDLE_ANIMATION_STATE;
        previousTouchX = -1;
        if (offsetValue < getWidth() / 2f) {
            idleState = WINTER_STATE;
            updateOffset(0);
            applyWinter();
        } else {
            idleState = SUMMER_STATE;
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

    FlingAnimation fling = new FlingAnimation(this, new FloatPropertyCompat<AnimatedLayoutTest>("offset") {
        @Override
        public float getValue(AnimatedLayoutTest object) {
            return object.getOffset();

        }

        @Override
        public void setValue(AnimatedLayoutTest object, float value) {
            Log.e("fling", "val =" + value);
            object.updateOffset(value);
            object.invalidate();
        }
    });

    private float getOffset() {
        return offsetValue;
    }

//    @Override
//    public void setScrollX(int value) {
//        Log.e("fling", "val =" + value);
//        updateOffset(value);
//        invalidate();
//    }

    // helper  util method
    private static float convertDpToPixel(float valueDp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueDp,
                displayMetrics);
    }
}
