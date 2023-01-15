package com.github.hanlyjiang.app.recyclerview.sticky;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

        private final List<TopSecInfo> topSecInfoList = new ArrayList<>(2);


        public int getCurrentIndex() {
            if (topSecInfoList.isEmpty()) {
                return RecyclerView.NO_POSITION;
            }
            return topSecInfoList.get(topSecInfoList.size() - 1).index;
        }

        public void saveCurrentStickyView(View currentChild, int childAdapterPosition) {
            if (!topSecInfoList.isEmpty() && topSecInfoList.get(topSecInfoList.size() - 1).index == childAdapterPosition) {
                return;
            }
            boolean drawingCacheEnabled = currentChild.isDrawingCacheEnabled();
            if (!drawingCacheEnabled) {
                currentChild.setDrawingCacheEnabled(true);
            }
            TopSecInfo topSecInfo = new TopSecInfo();
            topSecInfo.bitmap = currentChild.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            topSecInfo.index = childAdapterPosition;
            pushRecord(topSecInfo);

            if (!drawingCacheEnabled) {
                currentChild.setDrawingCacheEnabled(false);
            }
        }

        private void pushRecord(TopSecInfo topSecInfo) {
            if (topSecInfoList.size() > 0) {
                TopSecInfo endItem = topSecInfoList.get(topSecInfoList.size() - 1);
                if (endItem.index == topSecInfo.index) {
                    endItem.bitmap = topSecInfo.bitmap;
                    return;
                }
            }
            topSecInfoList.add(topSecInfo);
            Log.d(TAG, "pushRecord:" + topSecInfo + "\t; " + topSecInfoList);
            if (topSecInfoList.size() == 3) {
                topSecInfoList.remove(0);
            }
        }

        public void draw(Canvas c, Paint paint) {
            TopSecInfo currentTopSec = topSecInfoList.get(topSecInfoList.size() - 1);
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
                return "TopSecInfo{" + " index = " + index + "}";
            }
        }

        private void restoreTopSecToPrv(int targetPosition) {
            if (topSecInfoList.size() == 2 && topSecInfoList.get(1).index == targetPosition) {
                Log.d(TAG, "do real restore before: " + topSecInfoList);
                topSecInfoList.add(0, topSecInfoList.remove(1));
                Log.d(TAG, "do real restore after: " + topSecInfoList);
            }
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

                // 只有两种情况：
                // 1. 使用当前;
                // 2. 使用上一个; (上一个是相对逻辑含义来的）
                if (currentChildTop <= 0) {
                    // 当前遍历的Item需要被选中为当前的StickyView
                    mStickyViewCache.saveCurrentStickyView(currentChild, childAdapterPosition);
                    mStickyViewCache.updateTransition(0);
                } else if (currentChildTop <= stickyViewHeight) {
                    // 设置交接处的Transition
                    updateTransitionIfHave(currentChildTop, stickyViewHeight);
                    mStickyViewCache.restoreTopSecToPrv(childAdapterPosition);
                } else {
                    mStickyViewCache.restoreTopSecToPrv(childAdapterPosition);
                }
                break;
            }
        }
    }

    /**
     * 设置交接处的Transition
     *
     * @param currentChildTop  childTop
     * @param stickyViewHeight View Height
     */
    private void updateTransitionIfHave(int currentChildTop, int stickyViewHeight) {
        // 交替区域，设置平移效果
        if (currentChildTop > 0 && currentChildTop <= stickyViewHeight) {
            // 赋予 transition
            mStickyViewCache.updateTransition(currentChildTop - stickyViewHeight);
        } else {
            mStickyViewCache.updateTransition(0);
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
