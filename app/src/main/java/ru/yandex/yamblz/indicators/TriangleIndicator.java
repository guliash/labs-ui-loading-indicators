package ru.yandex.yamblz.indicators;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class TriangleIndicator extends View {

    private static final long DEFAULT_DURATION = 1000;

    private static final float CIRCLE_RADIUS = 0.1f;
    private static final float STROKE_WITDH = 2;

    private static final TimeInterpolator sDefaultInterpolator = new LinearInterpolator();

    private Paint mPaint;
    private int mWidth, mHeight;
    private float mRadius;
    private AnimatorSet mAnimator;
    private float mVal;
    private boolean mRunning;

    public TriangleIndicator(Context context) {
        super(context);
        init();
    }

    public TriangleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(STROKE_WITDH);
        mPaint.setAntiAlias(true);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mRunning) {
            float len = mWidth - 2 * mRadius - 2 * STROKE_WITDH;
            float y = (float)Math.sqrt(3) * len / 2;
            canvas.save();
            canvas.translate(mRadius + STROKE_WITDH, mHeight - mRadius - STROKE_WITDH);
            canvas.drawCircle(mVal, 0, mRadius, mPaint);
            canvas.restore();
            canvas.save();
            canvas.translate(mWidth / 2, mHeight - mRadius - STROKE_WITDH - y);
            canvas.rotate(60);
            canvas.drawCircle(len - mVal, 0, mRadius, mPaint);
            canvas.restore();
            canvas.translate(mWidth / 2, mHeight - mRadius - STROKE_WITDH - y);
            canvas.rotate(-60);
            canvas.drawCircle(-mVal, 0, mRadius, mPaint);
        }

    }

    private void initValues() {
        mRadius = CIRCLE_RADIUS * Math.min(mWidth, mHeight);
    }

    private void startAnimations() {
        mRunning = true;
        mAnimator = new AnimatorSet();
        ValueAnimator anim = ValueAnimator.ofFloat(0, mWidth - 2 * mRadius - 2 * STROKE_WITDH);
        anim.setInterpolator(sDefaultInterpolator);
        anim.setDuration(DEFAULT_DURATION);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mVal = (float)animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.start();
    }

}
