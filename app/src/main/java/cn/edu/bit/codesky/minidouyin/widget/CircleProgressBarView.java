package cn.edu.bit.codesky.minidouyin.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author codesky
 * @date 2019/1/28 9:53
 * @description default
 */
public class CircleProgressBarView extends View {

    private Paint mProgressCirclePaint;
    private ValueAnimator mValueAnimator;
    private float mCurrentProgress;
    private boolean isRecording;

    public CircleProgressBarView(Context context) {
        super(context);
        init(context);
    }

    public CircleProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {

        // 初始化画笔
        mProgressCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressCirclePaint.setStyle(Paint.Style.STROKE);
        mProgressCirclePaint.setColor(Color.parseColor("#1E90FF"));
        mProgressCirclePaint.setStrokeWidth(10);

        mValueAnimator = ValueAnimator.ofFloat(0, 360f);
        mValueAnimator.setDuration(10 * 1000);
        isRecording = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rectf = new RectF(10, 10, getWidth() - 10, getHeight() - 10);
        if (isRecording) {
            canvas.drawArc(rectf, -90, mCurrentProgress, false, mProgressCirclePaint);
        }
    }

    public void startProgressAnimation() {
        isRecording = true;
        mValueAnimator.addUpdateListener(animation -> {
            mCurrentProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mValueAnimator.start();
    }

    public void pauseProgressAnimation() {
        mValueAnimator.pause();
    }

    public void continueProgressAnimation() {
        mValueAnimator.resume();
    }

    public void stopProgressAnimation() {
        isRecording = false;
        mValueAnimator.cancel();
    }

    public void reset() {
        isRecording = false;
        invalidate();
    }

}
