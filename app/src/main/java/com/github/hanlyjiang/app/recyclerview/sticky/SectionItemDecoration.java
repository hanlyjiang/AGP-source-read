package com.github.hanlyjiang.app.recyclerview.sticky;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * SectionItemDecoration
 *
 * @author hhjiang 2023/1/13 14:03
 * @version 1.0
 */
public class SectionItemDecoration extends RecyclerView.ItemDecoration {


    public interface GroupListener {

        boolean isGroup(int adapterPosition);

        void setContent(ViewGroup contentView, int position);
    }

    private static final String TAG = SectionItemDecoration.class.getSimpleName();

    private final GroupListener groupListener;

    /**
     * 当前正在绘制的TOP
     */
    private final TopSecInfo currentTopSec = new TopSecInfo();
    /**
     * 上一个TOP
     */
    private final TopSecInfo prvTopSec = new TopSecInfo();

    private static class TopSecInfo {
        public int index = -1;
        public Bitmap bitmap;

        @Override
        public String toString() {
            return "TopSecInfo{" +
                    " index = " + index +
                    ", bitmap = " + bitmap +
                    '}';
        }
    }

    Paint bitmapPaint;

    public SectionItemDecoration(GroupListener groupListener) {
        this.groupListener = groupListener;
        bitmapPaint = new Paint();
//        testPaint.setColor(0xFF000000);
        // 透明度设置为 255 才能绘制出完全不透明的Bitmap
        bitmapPaint.setAlpha(255);
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
//        Log.d(TAG, "getItemOffsets：" + state.toString());
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
//        Log.d(TAG, "onDraw：" + state.toString());
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//        super.onDrawOver(c, parent, state);
//        parent.getChildCount() 获取的是当前可用的View（RecyclerView中只有可见的几个）
        for (int i = 0; i < parent.getChildCount(); i++) {
            // 遍历，获取需要黏住的 View
            View currentChild = parent.getChildAt(i);
            int childAdapterPosition = parent.getChildAdapterPosition(currentChild);
            if (childAdapterPosition != RecyclerView.NO_POSITION && groupListener.isGroup(childAdapterPosition)) {
                int top = currentChild.getTop();
                boolean isCurrentItemShouldBeTop = top <= currentChild.getHeight()/2;
                boolean isCurrentItemIsUsing = currentTopSec.index == childAdapterPosition;
                // 没有完全显示出来 || 刚好显示完全
//                Log.d(TAG, "top = " + top + "," + currentChild.getHeight());
                if (isCurrentItemShouldBeTop && !isCurrentItemIsUsing) {
                    Log.d(TAG, "top <= currentChild.getHeight()： childAdapterPosition: " + childAdapterPosition);
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
                } else {
                    // 没有显示完全的情况，取上一个的
                    restoreTopSecToPrv();
                }
                if (isCurrentItemIsUsing) {
                    Log.d(TAG, "currentTopPosition == childAdapterPosition: " + childAdapterPosition);
                    break;
                }
            }
        }

        drawTopSec(c);
    }

    private void drawTopSec(@NonNull Canvas c) {
        Log.d(TAG, "drawTopSec: top = " + currentTopSec);
        if (currentTopSec.bitmap != null) {
            c.drawBitmap(currentTopSec.bitmap, 0, 0, bitmapPaint);
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


//    @Override
//    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//        super.onDrawOver(c, parent, state);
//        Log.d(TAG, "onDrawOver：" + state.toString());
////        parent.getChildCount() 获取的是当前可用的View（RecyclerView中只有可见的几个）
//        int left = parent.getPaddingLeft();
//        for (int i = 0; i < parent.getChildCount(); i++) {
//            // 遍历，获取需要黏住的 View
//            View currentChild = parent.getChildAt(i);
//            int childAdapterPosition = parent.getChildAdapterPosition(currentChild);
//            if (childAdapterPosition != RecyclerView.NO_POSITION && groupListener.isGroup(childAdapterPosition)) {
//                // parent.getChildAt(i).top是Item内容的高度，不包含Decoration的高度；sectionLayout.measuredHeight是Decoration的高度
//                int top = currentChild.getTop();
//                if (top > sectionLayout.getMeasuredHeight() && top < sectionLayout.getMeasuredHeight() * 2) {
//                    firstTop = currentChild.getTop() - sectionLayout.getMeasuredHeight() * 2;
//                    if (lastBitmap != null) {
//                        c.drawBitmap(lastBitmap, left, firstTop, null);
//                    }
//                    // 发现是交换的过程，绘制完交换后的Decoration后，不再绘制top位置是0的Decoration
//                    return;
//                } else {
//                    firstTop = 0;
//                }
//            } else {
//                firstTop = 0;
//            }
//        }
////        //绘制top位置是0的Decoration
////        if (lastBitmap != null) {
////            c.drawBitmap(lastBitmap, left, firstTop, null);
////        }
//    }

}
