package com.daimajia.numberprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import static com.daimajia.numberprogressbar.NumberProgressBar.ProgressTextVisibility.Invisible;
import static com.daimajia.numberprogressbar.NumberProgressBar.ProgressTextVisibility.Visible;


/**
 * Created by daimajia on 14-4-30.
 */
public class NumberProgressBar_ extends View {

    private int mReachedBarColor = Color.parseColor("#ff0000");
    private int mUnreachedBarColor = Color.parseColor("#00ff00");
    private int mTextBackgroundColor = Color.parseColor("#0000ff");
    private int mTextColor = Color.parseColor("#ff00ff");

    private float mTextSize = sp2px(12);
    private float mReachedBarHeight = dp2px(3);
    private float mUnreachedBarHeight = dp2px(3);
    private float mTextPaddingLeft = dp2px(6);
    private float mTextPaddingRight = dp2px(6);
    private float mTextPaddingTop = dp2px(2);
    private float mTextPaddingBottom = dp2px(2);

    private int mMaxProgress = 100;
    private int mCurrentProgress = 0; //当前进度
    private String mSuffix = "%"; //後綴
    private String mPrefix = ""; //前綴
    private String mCurrentDrawText;

    private Paint mReachedBarPaint;
    private Paint mUnreachedBarPaint;
    private Paint mTextBackgroundPaint;
    private TextPaint mTextPaint;

    private float mDrawTextWidth;
    private float mDrawTextHeight;

    private RectF mUnreachedRectF = new RectF(0, 0, 0, 0);
    private RectF mReachedRectF = new RectF(0, 0, 0, 0);
    private RectF mTextBackgroundRect = new RectF(0, 0, 0, 0);

    private OnProgressBarListener mListener;

    private float mDevideLine; //分界线


    /**
     * For save and restore instance of progressbar.
     */
    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_TEXT_COLOR = "text_color";
    private static final String INSTANCE_TEXT_SIZE = "text_size";
    private static final String INSTANCE_REACHED_BAR_HEIGHT = "reached_bar_height";
    private static final String INSTANCE_REACHED_BAR_COLOR = "reached_bar_color";
    private static final String INSTANCE_UNREACHED_BAR_HEIGHT = "unreached_bar_height";
    private static final String INSTANCE_UNREACHED_BAR_COLOR = "unreached_bar_color";
    private static final String INSTANCE_MAX = "max";
    private static final String INSTANCE_PROGRESS = "progress";
    private static final String INSTANCE_SUFFIX = "suffix";
    private static final String INSTANCE_PREFIX = "prefix";
    private static final String INSTANCE_TEXT_VISIBILITY = "text_visibility";

    private static final int PROGRESS_TEXT_VISIBLE = 0;
    /**
     * Determine if need to draw unreached area.
     */
    private boolean mDrawUnreachedBar = true;
    private boolean mDrawReachedBar = true;
    private boolean mIfDrawText = true;

    public enum ProgressTextVisibility {
        Visible, Invisible
    }

    public NumberProgressBar_(Context context) {
        this(context, null);
    }

    public NumberProgressBar_(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberProgressBar_(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //load styled attributes.
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberProgressBar, defStyleAttr, 0);
        mReachedBarColor = attributes.getColor(R.styleable.NumberProgressBar_progress_reached_color, mReachedBarColor);
        mUnreachedBarColor = attributes.getColor(R.styleable.NumberProgressBar_progress_unreached_color, mUnreachedBarColor);
        mTextColor = attributes.getColor(R.styleable.NumberProgressBar_progress_text_color, mTextColor);
        mTextBackgroundColor = attributes.getColor(R.styleable.NumberProgressBar_progress_text_background_color, mTextBackgroundColor);
        mTextSize = attributes.getDimension(R.styleable.NumberProgressBar_progress_text_size, mTextSize);
        mReachedBarHeight = attributes.getDimension(R.styleable.NumberProgressBar_progress_reached_bar_height, mReachedBarHeight);
        mUnreachedBarHeight = attributes.getDimension(R.styleable.NumberProgressBar_progress_unreached_bar_height, mUnreachedBarHeight);

        int textVisible = attributes.getInt(R.styleable.NumberProgressBar_progress_text_visibility, PROGRESS_TEXT_VISIBLE);
        if (textVisible != PROGRESS_TEXT_VISIBLE) {
            mIfDrawText = false;
        }
        setProgress(attributes.getInt(R.styleable.NumberProgressBar_progress_current, 0));
        setMax(attributes.getInt(R.styleable.NumberProgressBar_progress_max, 100));

        attributes.recycle();
        initializePainters();
    }

    private void initializePainters() {
        mReachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedBarPaint.setColor(mReachedBarColor);

        mUnreachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnreachedBarPaint.setColor(mUnreachedBarColor);

        mTextBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextBackgroundPaint.setColor(mTextBackgroundColor);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
    }

    public int getMax() {
        return mMaxProgress;
    }

    public void setMax(int maxProgress) {
        if (maxProgress > 0) {
            this.mMaxProgress = maxProgress;
            invalidate();
        }
    }

    public int getProgress() {
        return mCurrentProgress;
    }

    public void setProgress(int progress) {
        if (/*progress <= getMax() &&*/ progress >= 0) {
            this.mCurrentProgress = progress;
            invalidate();
        }
    }

    public void setOnProgressBarListener(OnProgressBarListener listener) {
        mListener = listener;
    }

    public float dp2px(float dp) {
        return dp * getResources().getDisplayMetrics().density + 0.5f;
    }

    public float sp2px(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return (int) (mTextSize + getPaddingBottom() + getPaddingTop());
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return Math.max((int) (mTextSize + mTextPaddingTop + mTextPaddingBottom), Math.max((int) mReachedBarHeight, (int) mUnreachedBarHeight));
    }

    public void incrementProgressBy(int by) {
        if (by > 0) {
            setProgress(getProgress() + by);
        }
        if (mListener != null) {
            mListener.onProgressChange(getProgress(), getMax());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //分界线
        mDevideLine = (getWidth() - getPaddingLeft() - getPaddingRight()) / (getMax() * 1.0f) * Math.max(Math.min(getMax(), mCurrentProgress), 0) + getPaddingLeft();
        measureReachedRectF();
        measureUnreachedRectF();
        measureText();
        measureTextBackground();

        drawReachedRectF(canvas);
        drawUnreachedRectF(canvas);
        drawTextBackgroundRectF(canvas);
        drawText(canvas);
    }

    private void measureTextBackground() {
        mTextBackgroundRect.left = mDevideLine - mDrawTextWidth / 2 - (mTextPaddingLeft + mTextPaddingRight) / 2;
        mTextBackgroundRect.right = mDevideLine + mDrawTextWidth / 2 + (mTextPaddingLeft + mTextPaddingRight) / 2;

        //TODO 还要处理在端点处显示不全的问题 这个主要看需求
        if (mTextBackgroundRect.left < getPaddingLeft()) {
            mTextBackgroundRect.right = mTextBackgroundRect.width() + getPaddingLeft();
            mTextBackgroundRect.left = getPaddingLeft();
        }
        if (mTextBackgroundRect.right > getWidth() - getPaddingRight()) {
            mTextBackgroundRect.left = getWidth() - getPaddingRight() - mTextBackgroundRect.width();
            mTextBackgroundRect.right = getWidth() - getPaddingRight();
        }

        mTextBackgroundRect.top = (getHeight() - mDrawTextHeight) / 2 - (mTextPaddingTop + mTextPaddingBottom) / 2;
        mTextBackgroundRect.bottom = (getHeight() + mDrawTextHeight) / 2 + (mTextPaddingTop + mTextPaddingBottom) / 2;
    }

    private void measureText() {
        mCurrentDrawText = mPrefix + String.format("%d", getProgress() * 100 / getMax()) + mSuffix;
        mDrawTextWidth = mTextPaint.measureText(mCurrentDrawText);
        mDrawTextHeight = mTextPaint.getTextSize();
    }

    private void measureUnreachedRectF() {
        mUnreachedRectF.left = mDevideLine;
        mUnreachedRectF.right = getWidth() - getPaddingRight();
        mUnreachedRectF.top = getHeight() / 2.0f - mUnreachedBarHeight / 2.0f;
        mUnreachedRectF.bottom = getHeight() / 2.0f + mUnreachedBarHeight / 2.0f;
    }

    private void measureReachedRectF() {
        mReachedRectF.left = getPaddingLeft();
        mReachedRectF.right = mDevideLine;
        mReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
        mReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;
    }

    private void drawText(Canvas canvas) {
        canvas.drawText(mCurrentDrawText, (mTextBackgroundRect.left + mTextBackgroundRect.right) / 2, (mTextBackgroundRect.top + mTextBackgroundRect.bottom) / 2 + mDrawTextHeight / 3, mTextPaint);
    }

    private void drawTextBackgroundRectF(Canvas canvas) {
        canvas.drawRoundRect(mTextBackgroundRect, mTextBackgroundRect.height() / 2, mTextBackgroundRect.height() / 2, mTextBackgroundPaint);
    }

    private void drawUnreachedRectF(Canvas canvas) {
        canvas.drawRoundRect(mUnreachedRectF, mUnreachedBarHeight / 2, mUnreachedBarHeight / 2, mUnreachedBarPaint);
    }

    private void drawReachedRectF(Canvas canvas) {
        canvas.drawRoundRect(mReachedRectF, mReachedBarHeight / 2, mReachedBarHeight / 2, mReachedBarPaint);
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getTextBackgroundColor() {
        return mTextBackgroundColor;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor());
        bundle.putFloat(INSTANCE_TEXT_SIZE, getProgressTextSize());
        bundle.putFloat(INSTANCE_REACHED_BAR_HEIGHT, getReachedBarHeight());
        bundle.putFloat(INSTANCE_UNREACHED_BAR_HEIGHT, getUnreachedBarHeight());
        bundle.putInt(INSTANCE_REACHED_BAR_COLOR, getReachedBarColor());
        bundle.putInt(INSTANCE_UNREACHED_BAR_COLOR, getUnreachedBarColor());
        bundle.putInt(INSTANCE_MAX, getMax());
        bundle.putInt(INSTANCE_PROGRESS, getProgress());
        bundle.putString(INSTANCE_SUFFIX, getSuffix());
        bundle.putString(INSTANCE_PREFIX, getPrefix());
        bundle.putBoolean(INSTANCE_TEXT_VISIBILITY, getProgressTextVisibility());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mTextColor = bundle.getInt(INSTANCE_TEXT_COLOR);
            mTextSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
            mReachedBarHeight = bundle.getFloat(INSTANCE_REACHED_BAR_HEIGHT);
            mUnreachedBarHeight = bundle.getFloat(INSTANCE_UNREACHED_BAR_HEIGHT);
            mReachedBarColor = bundle.getInt(INSTANCE_REACHED_BAR_COLOR);
            mUnreachedBarColor = bundle.getInt(INSTANCE_UNREACHED_BAR_COLOR);
            initializePainters();
            setMax(bundle.getInt(INSTANCE_MAX));
            setProgress(bundle.getInt(INSTANCE_PROGRESS));
            setPrefix(bundle.getString(INSTANCE_PREFIX));
            setSuffix(bundle.getString(INSTANCE_SUFFIX));
            setProgressTextVisibility(bundle.getBoolean(INSTANCE_TEXT_VISIBILITY) ? Visible : Invisible);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public float getProgressTextSize() {
        return mTextSize;
    }

    public int getUnreachedBarColor() {
        return mUnreachedBarColor;
    }

    public int getReachedBarColor() {
        return mReachedBarColor;
    }

    public void setSuffix(String suffix) {
        if (suffix == null) {
            mSuffix = "";
        } else {
            mSuffix = suffix;
        }
    }

    public void setProgressTextVisibility(NumberProgressBar.ProgressTextVisibility visibility) {
        mIfDrawText = visibility == Visible;
        invalidate();
    }

    public boolean getProgressTextVisibility() {
        return mIfDrawText;
    }

    public String getSuffix() {
        return mSuffix;
    }

    public void setPrefix(String prefix) {
        if (prefix == null)
            mPrefix = "";
        else {
            mPrefix = prefix;
        }
    }

    public String getPrefix() {
        return mPrefix;
    }

    public float getReachedBarHeight() {
        return mReachedBarHeight;
    }

    public float getUnreachedBarHeight() {
        return mUnreachedBarHeight;
    }

    public void setProgressTextSize(float textSize) {
        this.mTextSize = textSize;
        mTextPaint.setTextSize(mTextSize);
        invalidate();
    }

    public void setProgressTextColor(int textColor) {
        this.mTextColor = textColor;
        mTextPaint.setColor(mTextColor);
        invalidate();
    }

}
