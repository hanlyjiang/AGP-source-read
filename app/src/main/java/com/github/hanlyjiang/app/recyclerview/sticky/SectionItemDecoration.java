package com.github.hanlyjiang.app.recyclerview.sticky;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

/**
 * SectionItemDecoration
 *
 * @author hhjiang 2023/1/13 14:03
 * @version 1.0
 */
public class SectionItemDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = SectionItemDecoration.class.getSimpleName();

    private final StickyViewTester mStickyViewTester;
    private final StickyViewCache mStickyViewCache = new StickyViewCache();

    /**
     * 缓存工具类
     */
    private static class StickyViewCache {

        private int mDrawTransition = 0;

        public int getCurrentIndex() {
            return currentTopSec.index;
        }

        /**
         * 当前正在绘制的TOP
         */
        private final TopSecInfo currentTopSec = new TopSecInfo();
        /**
         * 上一个TOP
         */
        private final TopSecInfo prvTopSec = new TopSecInfo();

        public void saveCurrentStickyView(View currentChild, int childAdapterPosition) {
            boolean drawingCacheEnabled = currentChild.isDrawingCacheEnabled();
            if (!drawingCacheEnabled) {
                currentChild.setDrawingCacheEnabled(true);
            }

            currentTopSec.bitmap = currentChild.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            currentTopSec.index = childAdapterPosition;
            recordCurrentSecTopToPrv();

            if (!drawingCacheEnabled) {
                currentChild.setDrawingCacheEnabled(false);
            }
        }

        public void draw(Canvas c, Paint paint) {
            Log.d(TAG, "drawTopSec: top = " + currentTopSec);
            if (currentTopSec.bitmap != null) {
                c.translate(0, mDrawTransition);
                c.drawBitmap(currentTopSec.bitmap, 0, 0, paint);
            }
        }

        public void updateTransition(int i) {
            mDrawTransition = i;
        }

        private static class TopSecInfo {
            public int index = -1;
            public Bitmap bitmap;

            @NotNull
            @Override
            public String toString() {
                return "TopSecInfo{" + " index = " + index + ", bitmap = " + bitmap + '}';
            }
        }

        private void restoreTopSecToPrv() {
            Log.d(TAG, "restoreTopSecToPrv: before = " + currentTopSec);
            currentTopSec.bitmap = prvTopSec.bitmap;
            currentTopSec.index = prvTopSec.index;
            Log.d(TAG, "restoreTopSecToPrv: after = " + currentTopSec);
        }

        private void recordCurrentSecTopToPrv() {
            Log.d(TAG, "recordCurrentSecTopToPrv: before = " + prvTopSec);
            prvTopSec.bitmap = currentTopSec.bitmap;
            prvTopSec.index = currentTopSec.index;
            Log.d(TAG, "recordCurrentSecTopToPrv: after = " + prvTopSec);
        }
    }


    Paint bitmapPaint;

    public SectionItemDecoration(StickyViewTester mStickyViewTester) {
        this.mStickyViewTester = mStickyViewTester;
        bitmapPaint = new Paint();
//        testPaint.setColor(0xFF000000);
        // 透明度设置为 255 才能绘制出完全不透明的Bitmap
        bitmapPaint.setAlpha(255);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
//        parent.getChildCount() 获取的是当前可用的View（RecyclerView中只有可见的几个）
        // 1. 查找 StickyView
        findStickyView(parent);

        // 2. 绘制 StickyView
        drawStickyView(parent, c);
    }

    /**
     * 查找 StickyView
     *
     * @param parent RecyclerView
     */
    protected void findStickyView(RecyclerView parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            // 遍历，获取需要黏住的 View
            View currentChild = parent.getChildAt(i);
            int childAdapterPosition = parent.getChildAdapterPosition(currentChild);
            if (childAdapterPosition == RecyclerView.NO_POSITION) {
                continue;
            }

            if (mStickyViewTester.isStickyView(currentChild, childAdapterPosition)) {
                int currentChildTop = currentChild.getTop();
                int stickyViewHeight = currentChild.getHeight();

                boolean isCurrentItemShouldBeTop = currentChildTop <= stickyViewHeight >> 1;
                boolean isCurrentItemIsUsing = mStickyViewCache.getCurrentIndex() == childAdapterPosition;

                // 交替区域，设置平移效果
                if (currentChildTop > 0 && currentChildTop <= stickyViewHeight) {
                    // 赋予 transition
                    mStickyViewCache.updateTransition(currentChildTop - stickyViewHeight);
                } else {
                    mStickyViewCache.updateTransition(0);
                }

                if (isCurrentItemShouldBeTop && !isCurrentItemIsUsing) {
                    Log.d(TAG, "top <= currentChild.getHeight()： childAdapterPosition: " + childAdapterPosition);
                    mStickyViewCache.saveCurrentStickyView(currentChild, childAdapterPosition);
                } else {
                    // 没有显示完全的情况，取上一个的
                    mStickyViewCache.restoreTopSecToPrv();
                }
                if (isCurrentItemIsUsing) {
                    Log.d(TAG, "currentTopPosition == childAdapterPosition: " + childAdapterPosition);
                    break;
                }
            }
        }
    }

    /**
     * 绘制
     *
     * @param parent RecyclerView
     * @param c      Canvas
     */
    protected void drawStickyView(RecyclerView parent, @NonNull Canvas c) {
        mStickyViewCache.draw(c, bitmapPaint);
    }


}
