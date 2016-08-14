package ru.yandex.yamblz.indicators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class LineIndicator extends View {

    private static final long DEFAULT_DURATION = 500;

    private static final float CIRCLE_RADIUS = 0.1f;

    private static final int LINE_COLOR = Color.WHITE;

    private static final float LINE_MIN_HEIGHT = 0.3f;

    private static final int LINE_COUNT = 5;

    private static final float LINE_MAX_HEIGHT = 0.7f;

    private static final float ADD_FRACTION = 0.2f;

    private static final float LINE_WIDTH = 0.1f;

    private static final float DIST_BETWEEN = 0.7f * LINE_WIDTH;

    private static final TimeInterpolator sDefaultInterpolator = new LinearInterpolator();

    private ValueAnimator mValueAnimator;
    private FloatEvaluator mEvaluator;

    private int mWidth, mHeight;

    private boolean mRunning, mForward;

    private float mLineWidth, mDistBetween, mCurFraction;

    private Paint mPaint;

    public LineIndicator(Context context) {
        super(context);
        init();
    }

    public LineIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(LINE_COLOR);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRunning) {

            float left = mWidth / 2 - mLineWidth / 2 - mLineWidth * 2 - mDistBetween * 2;
            for (int i = 0; i < LINE_COUNT; i++, left += mLineWidth + mDistBetween) {
                float fraction = mCurFraction;
                if (mForward) {
                    fraction += i * ADD_FRACTION;
                    if (fraction > 1.0f) {
                        fraction = 1 - (fraction % 1.0f);
                    }
                } else {
                    fraction -= i * ADD_FRACTION;
                    if (fraction < 0f) {
                        fraction = -fraction;
                    }
                }
                float lineHeightFraction = mEvaluator.evaluate(sDefaultInterpolator.getInterpolation(fraction),
                        LINE_MIN_HEIGHT, LINE_MAX_HEIGHT);
                float lineHeight = lineHeightFraction * mHeight;
                
                canvas.drawRect(left, mHeight / 2 - lineHeight / 2, left + mLineWidth,
                        mHeight / 2 + lineHeight / 2, mPaint);
                canvas.drawCircle(left + mLineWidth / 2, mHeight / 2 + lineHeight / 2, mLineWidth / 2, mPaint);
                canvas.drawCircle(left + mLineWidth / 2, mHeight / 2 - lineHeight / 2, mLineWidth / 2, mPaint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != 0 && h != 0) {
            mWidth = w;
            mHeight = h;
            initValues();
            startAnimations();
        }
    }

    private void initValues() {
        mLineWidth = mWidth * LINE_WIDTH;
        mDistBetween = mWidth * DIST_BETWEEN;
    }

    private void startAnimations() {
        mRunning = true;
        mForward = true;
        mValueAnimator = ValueAnimator.ofFloat(LINE_MIN_HEIGHT, LINE_MAX_HEIGHT);
        mValueAnimator.setDuration(DEFAULT_DURATION);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mValueAnimator.setInterpolator(sDefaultInterpolator);
        mEvaluator = new FloatEvaluator();

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurFraction = animation.getAnimatedFraction();
                invalidate();
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                mForward = !mForward;
            }
        });

        mValueAnimator.start();
    }
}
