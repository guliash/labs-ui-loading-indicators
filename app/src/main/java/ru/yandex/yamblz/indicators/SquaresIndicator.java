package ru.yandex.yamblz.indicators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

public class SquaresIndicator extends View {

    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final long DEFAULT_DURATION = 2000;
    private static final long SCALE_DURATION = 400;
    private static final long ROTATE_DURATION = 500;
    private static final float SQUARE_SMALL = 0.1f;
    private static final float SQUARE_BIG = 0.2f;

    private Paint mPaint;
    private int mWidth, mHeight;
    private boolean mRunning;
    private float mSquareSmall, mSquareBig, mSquareDiag;
    private float mScaled, mRotate;
    private long mVertDuration, mHorDuration;
    private float mX1, mY1, mX2, mY2;

    private static final Interpolator sDefaultInterpolator = new LinearInterpolator();

    private AnimatorSet mAnimator;

    public SquaresIndicator(Context context) {
        super(context);

        init();
    }



    public SquaresIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mRunning) {
            canvas.save();
            canvas.translate(mX1, mY1);
            canvas.rotate(mRotate);
            canvas.drawRect(-mScaled / 2, -mScaled / 2, mScaled / 2, mScaled / 2, mPaint);
            canvas.restore();
            canvas.translate(mX2, mY2);
            canvas.rotate(mRotate);
            canvas.drawRect(-mScaled / 2, -mScaled / 2, mScaled / 2, mScaled / 2, mPaint);
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
        mSquareBig = SQUARE_BIG * mWidth;
        mSquareSmall = SQUARE_SMALL * mHeight;
        mSquareDiag = (float)Math.sqrt(2) * mSquareBig;

        final float vertSide = mHeight - mSquareDiag;
        final float horSide = mWidth - mSquareDiag;

        final float total = vertSide * 2 + horSide * 2;

        mVertDuration = (long)(vertSide / total * DEFAULT_DURATION);
        mHorDuration = (long)(horSide / total * DEFAULT_DURATION);

    }

    private void startAnimations() {
        mRunning = true;

        mAnimator = new AnimatorSet();

        AnimatorSet animatorX = new AnimatorSet();
        AnimatorSet animatorY = new AnimatorSet();
        animatorX.playSequentially((List<Animator>)createXAnimations());
        animatorY.playSequentially((List<Animator>)createYAnimations());
        mAnimator.playTogether(animatorX, animatorY, createScaleAnimator(), createRotateAnimator());

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator.start();
            }
        });

        mAnimator.start();
    }

    private List<? extends Animator> createXAnimations() {
        List<ValueAnimator> animators = new ArrayList<>();;
        animators.add(ValueAnimator.ofFloat(mSquareDiag / 2, mWidth - mSquareDiag / 2).setDuration(mHorDuration));
        animators.add(ValueAnimator.ofFloat(mWidth - mSquareDiag / 2, mWidth - mSquareDiag / 2).setDuration(mVertDuration));
        animators.add(ValueAnimator.ofFloat(mWidth - mSquareDiag / 2, mSquareDiag / 2).setDuration(mHorDuration));
        animators.add(ValueAnimator.ofFloat(mSquareDiag / 2, mSquareDiag / 2).setDuration(mVertDuration));
        for(ValueAnimator valueAnimator : animators) {
            valueAnimator.addUpdateListener(updateListenerX);
            valueAnimator.setInterpolator(sDefaultInterpolator);
        }

        return animators;
    }

    private List<? extends Animator> createYAnimations() {
        List<ValueAnimator> animators = new ArrayList<>();
        animators.add(ValueAnimator.ofFloat(mSquareDiag / 2, mSquareDiag / 2).setDuration(mHorDuration));
        animators.add(ValueAnimator.ofFloat(mSquareDiag / 2, mHeight - mSquareDiag / 2).setDuration(mVertDuration));
        animators.add(ValueAnimator.ofFloat(mHeight - mSquareDiag / 2, mHeight - mSquareDiag / 2).setDuration(mHorDuration));
        animators.add(ValueAnimator.ofFloat(mHeight - mSquareDiag / 2, mSquareDiag / 2).setDuration(mVertDuration));
        for(ValueAnimator valueAnimator : animators) {
            valueAnimator.addUpdateListener(updateListenerY);
            valueAnimator.setInterpolator(sDefaultInterpolator);
        }

        return animators;
    }

    private Animator createScaleAnimator() {
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(mSquareBig, mSquareSmall);
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScaled = (float)animation.getAnimatedValue();
            }
        });
        scaleAnimator.setInterpolator(sDefaultInterpolator);
        scaleAnimator.setRepeatCount((int)(DEFAULT_DURATION / SCALE_DURATION) - 1);
        scaleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleAnimator.setDuration(SCALE_DURATION);
        return scaleAnimator;
    }

    private Animator createRotateAnimator() {
        ValueAnimator rotateAnimator = ValueAnimator.ofFloat(0f, 360f);
        rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRotate = (float)animation.getAnimatedValue();
            }
        });
        rotateAnimator.setInterpolator(sDefaultInterpolator);
        rotateAnimator.setRepeatCount((int)(DEFAULT_DURATION / ROTATE_DURATION) - 1);
        rotateAnimator.setRepeatMode(ValueAnimator.RESTART);
        rotateAnimator.setDuration(ROTATE_DURATION);
        return rotateAnimator;
    }

    private ValueAnimator.AnimatorUpdateListener updateListenerX = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mX1 = (float)animation.getAnimatedValue();
            mX2 = mWidth - mX1;
            invalidate();
        }
    };

    private ValueAnimator.AnimatorUpdateListener updateListenerY = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mY1 = (float)animation.getAnimatedValue();
            mY2 = mHeight - mY1;
        }
    };
}
