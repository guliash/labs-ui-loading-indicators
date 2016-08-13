package ru.yandex.yamblz.indicators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.yamblz.R;

public class CirclesForwardView extends View {

    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final long DEFAULT_DURATION = 300;
    private static final float CIRCLE_FRACTION = 0.1f;

    private Paint mPaint;
    private AnimatorSet mAnimator;
    private int mCircleColor;
    private boolean mRunning = false;
    private float mRadius;
    private long mDiagTime, mSideTime;
    private float mXup, mYup, mXdown, mYdown;
    private float mRotate;
    private int mWidth, mHeight;

    public CirclesForwardView(Context context) {
        super(context);
        init();
    }

    public CirclesForwardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray arr = context.getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.CirclesForwardView,
                0, 0);
        try {
            mCircleColor = arr.getColor(R.styleable.CirclesForwardView_circleColor, DEFAULT_COLOR);
        } finally {
             arr.recycle();
        }
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(mCircleColor);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mRunning) {
            canvas.drawCircle(mXup, mYup, mRadius, mPaint);
            canvas.drawCircle(mXdown, mYdown, mRadius, mPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(w != 0 && h != 0) {
            mWidth = w;
            mHeight = h;
            initValues();
            startAnimations();
        }
    }

    private void initValues() {
        final float diag = (float) Math.hypot(mWidth / 2 - mRadius, mHeight / 2 - mRadius);
        final float side = mWidth - 2 * mRadius;
        final float total = 2 * diag + side;

        mRadius = CIRCLE_FRACTION * Math.min(mWidth, mHeight) / 2;
        mDiagTime = (long) (diag / total * DEFAULT_DURATION);
        mSideTime = (long) (side / total * DEFAULT_DURATION);
    }

    private void startAnimations() {
        mRunning = true;

        AnimatorSet animatorX = new AnimatorSet();
        animatorX.playSequentially((List<Animator>)createXCoordinatesAnimators());

        AnimatorSet animatorY = new AnimatorSet();
        animatorY.playSequentially((List<Animator>)createYCoordinatesAnimators());

        mAnimator = new AnimatorSet();
        mAnimator.playTogether(animatorX, animatorY);
        mAnimator.addListener(mAnimListenerAdapter);
        mAnimator.start();

    }

    private List<? extends Animator> createXCoordinatesAnimators() {
        List<ValueAnimator> animators = new ArrayList<>();
        animators.add(ValueAnimator.ofFloat(mWidth / 2, mRadius).setDuration(mDiagTime));
        animators.add(ValueAnimator.ofFloat(mRadius, mWidth - mRadius).setDuration(mSideTime));
        animators.add(ValueAnimator.ofFloat(mWidth - mRadius, mWidth / 2).setDuration(mDiagTime));
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        for(ValueAnimator valueAnimator : animators) {
            valueAnimator.addUpdateListener(updateListenerX);
            valueAnimator.setInterpolator(linearInterpolator);
        }
        return animators;
    }

    private List<? extends Animator> createYCoordinatesAnimators() {
        List<ValueAnimator> animators = new ArrayList<>();
        animators.add(ValueAnimator.ofFloat(mHeight / 2, mRadius).setDuration(mDiagTime));
        animators.add(ValueAnimator.ofFloat(mRadius, mRadius).setDuration(mSideTime));
        animators.add(ValueAnimator.ofFloat(mRadius, mHeight / 2).setDuration(mDiagTime));
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        for(ValueAnimator valueAnimator : animators) {
            valueAnimator.addUpdateListener(updateListenerY);
            valueAnimator.setInterpolator(linearInterpolator);
        }
        return animators;
    }

    private void stopAnimations() {
        mAnimator.cancel();
    }

    private AnimatorListenerAdapter mAnimListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimator.start();
        }
    };


    private ValueAnimator.AnimatorUpdateListener updateListenerX = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mXup = (float)animation.getAnimatedValue();
            mXdown = mWidth - mXup;
            invalidate();
        }
    };

    private ValueAnimator.AnimatorUpdateListener updateListenerY = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mYup = (float)animation.getAnimatedValue();
            mYdown = mHeight - mYup;
            invalidate();
        }
    };
}
