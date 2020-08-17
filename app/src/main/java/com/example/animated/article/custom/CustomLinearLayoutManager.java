package com.example.animated.article.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animated.R;

import static com.example.animated.article.custom.RecViewAdapter.INSERT_POSITION;

public class CustomLinearLayoutManager extends LinearLayoutManager {
    public CustomLinearLayoutManager(Context context) {
        super(context);
    }

    public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
                                  RecyclerView.State state) {
        ConstraintLayout parentLayout = findAnimatedLayout();


        AnimatedLayout animatedLayout = null;
        if (parentLayout != null) {
            animatedLayout = parentLayout.findViewById(R.id.transformItem);
            // todo add parallax in next article
        }

        if (needToRunAnimation(dy, animatedLayout)) {
            animatedLayout.animateBy(dy);
            return dy;
        } else {
            return super.scrollVerticallyBy(dy, recycler, state);

        }
    }

    private ConstraintLayout findAnimatedLayout() {
        ConstraintLayout animatedView = (ConstraintLayout) findViewByPosition(INSERT_POSITION);
        return animatedView;
    }

    private boolean needToRunAnimation(float dy, AnimatedLayout animatedLayout) {

        if (animatedLayout == null) {
            // view is out of the screen - position is not laid out
            return false;
        }

        //todo merge nested if
        if (dy > 0) {
            if (animatedLayout.isForwardAnimationPossible()
                    && isItemAboveCenterY((View) animatedLayout.getParent())) {
                return true;
            }
        }

        if (dy < 0) {
            if (animatedLayout.isReverseAnimationPossible()
                    && !isItemAboveCenterY((View) animatedLayout.getParent())) {
                return true;
            }
        }
        return false;
    }

    private boolean isItemAboveCenterY(View animatedItem) {
        float centerItemY = computeItemCenterY(animatedItem);
        float centerRecyclerViewY = computeRecyclerViewCenterY();
        return centerItemY < centerRecyclerViewY;
    }

    private int computeItemCenterY(View animatedView) {
        return (animatedView.getBottom() + animatedView.getTop()) / 2;
    }

    private int computeRecyclerViewCenterY() {
        return getHeight() / 2;
    }

    boolean canScrollVerticallyFlag = true;

    @Override
    public boolean canScrollVertically() {
        if (canScrollVerticallyFlag) {
            return super.canScrollVertically();
        } else {
            return false;
        }
    }
}
