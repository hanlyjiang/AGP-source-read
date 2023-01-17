package com.github.hanlyjiang.app.swipeview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.github.hanlyjiang.app.R.styleable;

/**
 * 左滑菜单实现
 */
public class SwipeMenuLayout extends ViewGroup {
    private static final String TAG = "zxt/SwipeMenuLayout";
    private static final int NUM_1000 = -1000;
    private static final int SLIDING_ANGLE = 30;

    @SuppressLint("StaticFieldLeak")
    private static SwipeMenuLayout mViewCache;
    private static boolean isTouching;

    private int mScaleTouchSlop;
    private int mMaxVelocity;
    private int mPointerId;
    private int mRightMenuWidths;
    private int mLimit;
    private View mContentView;
    private final PointF mLastP;
    private boolean isUnMoved;
    private final PointF mFirstP;
    private boolean isUserSwiped;
    private VelocityTracker mVelocityTracker;
    private boolean isSwipeEnable;
    private boolean isIos;
    private boolean iosInterceptFlag;
    private boolean isLeftSwipe;
    private boolean isQuickClose;
    private ValueAnimator mExpandAnim;
    private ValueAnimator mCloseAnim;
    private int mCloseAnimTime;

    private SmoothOpenListener smoothOpenListener;
    private boolean isExpand;

    public SwipeMenuLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mLastP = new PointF();
        this.isUnMoved = true;
        this.mFirstP = new PointF();
        this.mCloseAnimTime = 300;
        this.init(context, attrs, defStyleAttr);
    }

    public boolean isSwipeEnable() {
        return this.isSwipeEnable;
    }

    public void setSwipeEnable(boolean swipeEnable) {
        this.isSwipeEnable = swipeEnable;
    }

    public boolean isIos() {
        return this.isIos;
    }

    /**
     * 设置一个展开时，另外一个是否能够滑动展开
     *
     * @param ios true - 不允许；false - 运行
     * @return SwipeMenuLayout
     */
    public SwipeMenuLayout setIos(boolean ios) {
        this.isIos = ios;
        return this;
    }

    public boolean isLeftSwipe() {
        return this.isLeftSwipe;
    }

    public SwipeMenuLayout setLeftSwipe(boolean leftSwipe) {
        this.isLeftSwipe = leftSwipe;
        return this;
    }

    public void setIsQuickClose(boolean isQuickClose) {
        this.isQuickClose = isQuickClose;
    }

    public static SwipeMenuLayout getViewCache() {
        return mViewCache;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        this.isSwipeEnable = true;
        this.isIos = true;
        this.isLeftSwipe = true;
        this.isQuickClose = false;
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, styleable.SwipeMenuLayout, defStyleAttr, 0);
        int count = ta.getIndexCount();

        for (int i = 0; i < count; ++i) {
            int attr = ta.getIndex(i);
            if (attr == styleable.SwipeMenuLayout_swipeEnable) {
                this.isSwipeEnable = ta.getBoolean(attr, true);
            } else if (attr == styleable.SwipeMenuLayout_ios) {
                this.isIos = ta.getBoolean(attr, true);
            } else if (attr == styleable.SwipeMenuLayout_leftSwipe) {
                this.isLeftSwipe = ta.getBoolean(attr, true);
            } else if (attr == styleable.SwipeMenuLayout_isQuickClose) {
                this.isQuickClose = ta.getBoolean(attr, false);
            }
        }

        ta.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.setClickable(true);
        this.mRightMenuWidths = 0;
        int mHeight = 0;
        int contentWidth = 0;
        int childCount = this.getChildCount();
        boolean MEASUREMATCHPARENTCHILDREN = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        boolean isNeedMeasureChildHeight = false;

        for (int i = 0; i < childCount; ++i) {
            View childView = this.getChildAt(i);
            childView.setClickable(true);
            if (childView.getVisibility() != View.GONE) {
                this.measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                MarginLayoutParams LP = (MarginLayoutParams) childView.getLayoutParams();
                mHeight = Math.max(mHeight, childView.getMeasuredHeight());
                if (MEASUREMATCHPARENTCHILDREN && LP.height == -1) {
                    isNeedMeasureChildHeight = true;
                }

                if (i > 0) {
                    this.mRightMenuWidths += childView.getMeasuredWidth();
                } else {
                    this.mContentView = childView;
                    contentWidth = childView.getMeasuredWidth();
                }
            }
        }

        this.setMeasuredDimension(this.getPaddingLeft() + this.getPaddingRight() + contentWidth,
                mHeight + this.getPaddingTop() + this.getPaddingBottom());
        this.mLimit = this.mRightMenuWidths * 4 / 10;
        if (isNeedMeasureChildHeight) {
            this.forceUniformHeight(childCount, widthMeasureSpec);
        }

    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(this.getContext(), attrs);
    }

    private void forceUniformHeight(int count, int widthMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(this.getMeasuredHeight(), MeasureSpec.EXACTLY);

        for (int i = 0; i < count; ++i) {
            View CHILD = this.getChildAt(i);
            if (CHILD.getVisibility() != View.GONE) {
                MarginLayoutParams lp = (MarginLayoutParams) CHILD.getLayoutParams();
                if (lp.height == -1) {
                    int oldWidth = lp.width;
                    lp.width = CHILD.getMeasuredWidth();
                    this.measureChildWithMargins(CHILD, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    lp.width = oldWidth;
                }
            }
        }

    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = this.getChildCount();
        int left = this.getPaddingLeft();
        int right = this.getPaddingRight();

        for (int i = 0; i < childCount; ++i) {
            View childView = this.getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                if (i == 0) {
                    childView.layout(left, this.getPaddingTop(), left + childView.getMeasuredWidth(),
                            this.getPaddingTop() + childView.getMeasuredHeight());
                    left += childView.getMeasuredWidth();
                } else if (this.isLeftSwipe) {
                    childView.layout(left, this.getPaddingTop(), left + childView.getMeasuredWidth(),
                            this.getPaddingTop() + childView.getMeasuredHeight());
                    left += childView.getMeasuredWidth();
                } else {
                    childView.layout(right - childView.getMeasuredWidth(), this.getPaddingTop(), right,
                            this.getPaddingTop() + childView.getMeasuredHeight());
                    right -= childView.getMeasuredWidth();
                }
            }
        }

    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.isSwipeEnable) {
            this.acquireVelocityTracker(ev);
            VelocityTracker VERTRACKER = this.mVelocityTracker;
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    this.isUserSwiped = false;
                    this.isUnMoved = true;
                    this.iosInterceptFlag = false;
                    if (isTouching) {
                        return false;
                    }

                    isTouching = true;
                    this.mLastP.set(ev.getRawX(), ev.getRawY());
                    this.mFirstP.set(ev.getRawX(), ev.getRawY());
                    if (mViewCache != null) {
                        if (mViewCache != this) {
                            mViewCache.smoothClose();
                            this.iosInterceptFlag = this.isIos;
                        }

                        this.getParent().requestDisallowInterceptTouchEvent(true);
                    }

                    this.mPointerId = ev.getPointerId(0);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    this.isUserSwiped = Math.abs(ev.getRawX() - this.mFirstP.x) > (float) this.mScaleTouchSlop;

                    if (!this.iosInterceptFlag && this.isUserSwiped) {
                        VERTRACKER.computeCurrentVelocity(1000, (float) this.mMaxVelocity);
                        float VELOCITY = VERTRACKER.getXVelocity(this.mPointerId);
                        if (Math.abs(VELOCITY) > 1000.0F) {
                            if (VELOCITY < -1000.0F) {
                                if (this.isLeftSwipe) {
                                    this.smoothExpand();
                                } else {
                                    this.smoothClose();
                                }
                            } else if (this.isLeftSwipe) {
                                this.smoothClose();
                            } else {
                                this.smoothExpand();
                            }
                        } else if (Math.abs(this.getScrollX()) > this.mLimit) {
                            this.smoothExpand();
                        } else {
                            this.smoothClose();
                        }
                    } else if (Math.abs(ev.getRawX() - this.mFirstP.x) > 0.0F) {
                        this.smoothClose();
                    }

                    this.releaseVelocityTracker();
                    isTouching = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!this.iosInterceptFlag) {
                        float gap = this.mLastP.x - ev.getRawX();
                        if (Math.abs(gap) > 10.0F || Math.abs(this.getScrollX()) > 10) {
                            this.getParent().requestDisallowInterceptTouchEvent(true);
                        }

                        if (Math.abs(gap) > (float) this.mScaleTouchSlop) {
                            this.isUnMoved = false;
                        }

                        if (this.judgeScrollDirection(this.mFirstP, this.mLastP)) {
                            this.scrollBy((int) gap, 0);
                            if (this.isLeftSwipe) {
                                if (this.getScrollX() < 0) {
                                    this.scrollTo(0, 0);
                                }

                                if (this.getScrollX() > this.mRightMenuWidths) {
                                    this.scrollTo(this.mRightMenuWidths, 0);
                                }
                            } else {
                                if (this.getScrollX() < -this.mRightMenuWidths) {
                                    this.scrollTo(-this.mRightMenuWidths, 0);
                                }

                                if (this.getScrollX() > 0) {
                                    this.scrollTo(0, 0);
                                }
                            }
                        }

                        this.mLastP.set(ev.getRawX(), ev.getRawY());
                    }
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.isSwipeEnable) {
            switch (ev.getAction()) {
                case 1:
                    if (this.isLeftSwipe) {
                        if (this.getScrollX() > this.mScaleTouchSlop && ev.getX() < (float) (this.getWidth() - this.getScrollX())) {
                            if (this.isUnMoved) {
                                this.smoothClose();
                            }

                            return true;
                        }
                    } else if (-this.getScrollX() > this.mScaleTouchSlop && ev.getX() > (float) (-this.getScrollX())) {
                        if (this.isUnMoved) {
                            this.smoothClose();
                        }

                        return true;
                    }

                    if (this.isUserSwiped) {
                        return true;
                    }
                    break;
                case 2:
                    if (Math.abs(ev.getRawX() - this.mFirstP.x) > (float) this.mScaleTouchSlop) {
                        return true;
                    }
            }

            if (this.iosInterceptFlag) {
                return true;
            }
        }

        return super.onInterceptTouchEvent(ev);
    }

    public void smoothExpand() {
        mViewCache = this;
        if (null != this.mContentView) {
            this.mContentView.setLongClickable(false);
        }

        this.cancelAnim();
        this.mExpandAnim = ValueAnimator.ofInt(this.getScrollX(), this.isLeftSwipe ? this.mRightMenuWidths : -this.mRightMenuWidths);
        this.mExpandAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animation != null && animation.getAnimatedValue() != null) {
                    SwipeMenuLayout.this.scrollTo((Integer) animation.getAnimatedValue(), 0);
                }

            }
        });
        this.mExpandAnim.setInterpolator(new OvershootInterpolator());
        this.mExpandAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                SwipeMenuLayout.this.isExpand = true;
            }
        });
        this.mExpandAnim.setDuration(300L).start();
        if (this.smoothOpenListener != null) {
            this.smoothOpenListener.smoothOpen();
        }

    }

    private void cancelAnim() {
        if (this.mCloseAnim != null && this.mCloseAnim.isRunning()) {
            this.mCloseAnim.cancel();
        }

        if (this.mExpandAnim != null && this.mExpandAnim.isRunning()) {
            this.mExpandAnim.cancel();
        }

    }

    public void smoothClose() {
        mViewCache = null;
        if (null != this.mContentView) {
            this.mContentView.setLongClickable(true);
        }

        this.cancelAnim();
        this.mCloseAnim = ValueAnimator.ofInt(this.getScrollX(), 0);
        this.mCloseAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animation != null && animation.getAnimatedValue() != null) {
                    SwipeMenuLayout.this.scrollTo((Integer) animation.getAnimatedValue(), 0);
                }

            }
        });
        this.mCloseAnim.setInterpolator(new AccelerateInterpolator());
        this.mCloseAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                SwipeMenuLayout.this.isExpand = false;
                SwipeMenuLayout.this.mCloseAnimTime = 300;
            }
        });
        this.mCloseAnim.setDuration((long) this.mCloseAnimTime).start();
        if (this.smoothOpenListener != null) {
            this.smoothOpenListener.smoothClose();
        }

    }

    public void setCloseAnimTime(int closeAnimTime) {
        this.mCloseAnimTime = closeAnimTime;
    }

    private void acquireVelocityTracker(MotionEvent event) {
        if (null == this.mVelocityTracker) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }

        this.mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (null != this.mVelocityTracker) {
            this.mVelocityTracker.clear();
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }

    }

    protected void onDetachedFromWindow() {
        if (this == mViewCache) {
            if (this.isQuickClose) {
                mViewCache.quickClose();
            } else {
                mViewCache.smoothClose();
            }

            mViewCache = null;
        }

        super.onDetachedFromWindow();
    }

    public boolean performLongClick() {
        return Math.abs(this.getScrollX()) <= this.mScaleTouchSlop && super.performLongClick();
    }

    public void quickClose() {
        if (this == mViewCache) {
            this.cancelAnim();
            mViewCache.scrollTo(0, 0);
            mViewCache = null;
        }

    }

    private boolean judgeScrollDirection(PointF firstP, PointF lastP) {
        boolean ret = false;
        float moveX = Math.abs(lastP.x - firstP.x);
        float moveY = Math.abs(lastP.y - firstP.y);
        double tan = Math.atan2((double) moveY, (double) moveX);
        double angleA = 180.0D * tan / 3.141592653589793D;
        if (angleA != 0.0D && angleA < 30.0D) {
            ret = true;
        }

        return ret;
    }

    public void setSmoothOpenListener(SmoothOpenListener smoothOpenListener) {
        this.smoothOpenListener = smoothOpenListener;
    }

    public interface SmoothOpenListener {
        void smoothOpen();

        void smoothClose();
    }
}
