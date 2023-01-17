package com.github.hanlyjiang.app.recyclerview.sticky;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

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

        private RecyclerView.Adapter mAdapter;
        private RecyclerView mRecyclerView;
        private RecyclerView.ViewHolder mViewHolder;

        private int mWidthMeasure, mHeightMeasure;

        private int mDrawTransition = 0;
        /**
         * 在 Adapter 中的位置
         */
        private int targetStickyViewIndex = RecyclerView.NO_POSITION;

        private final List<Integer> topSecInfoList = new ArrayList<>(3);

        /**
         * 保存StickyView index at adapter
         *
         * @param recyclerView         RecyclerView
         * @param currentChild         View
         * @param childAdapterPosition childAdapterPosition
         */
        public void saveCurrentStickyView(RecyclerView recyclerView, View currentChild, int childAdapterPosition) {
            setAdapter(recyclerView);
            targetStickyViewIndex = childAdapterPosition;
            mWidthMeasure = currentChild.getWidth();
            mHeightMeasure = currentChild.getHeight();
            if (!topSecInfoList.contains(childAdapterPosition)) {
                topSecInfoList.add(childAdapterPosition);
                createViewHolder();
            }
        }

        private void createViewHolder() {
            if (targetStickyViewIndex == RecyclerView.NO_POSITION || mAdapter == null || mRecyclerView == null) {
                Log.d(TAG, "createViewHolder return");
                return;
            }
            mViewHolder = mAdapter.onCreateViewHolder(mRecyclerView, mAdapter.getItemViewType(targetStickyViewIndex));
            mAdapter.onBindViewHolder(mViewHolder, targetStickyViewIndex);
            mViewHolder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(mWidthMeasure, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(mHeightMeasure, View.MeasureSpec.EXACTLY)
            );
            mViewHolder.itemView.layout(0, 0, mViewHolder.itemView.getMeasuredWidth(), mViewHolder.itemView.getMeasuredHeight());
        }

        public void draw(@NonNull Canvas c, @Nullable Paint paint) {
            if (mViewHolder == null) {
                Log.d(TAG, "mViewHolder is null");
                return;
            }
            c.translate(0, mDrawTransition);
            mViewHolder.itemView.draw(c);
        }

        public void updateTransition(int i) {
            mDrawTransition = i;
        }

        protected StickyViewCache setAdapter(RecyclerView recyclerView) {
            this.mRecyclerView = recyclerView;
            this.mAdapter = recyclerView.getAdapter();
            return this;
        }

        private void restoreTopSecToPrv(int targetPosition) {
            int i = topSecInfoList.indexOf(targetPosition);
            if ((i - 1) >= 0) {
                targetStickyViewIndex = topSecInfoList.get(i - 1);
                createViewHolder();
            }
        }
    }


    public SectionItemDecoration(StickyViewTester mStickyViewTester) {
        this.mStickyViewTester = mStickyViewTester;
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
                    mStickyViewCache.saveCurrentStickyView(parent, currentChild, childAdapterPosition);
                    mStickyViewCache.updateTransition(0);
                } else  {
                    // 设置交接处的Transition
                    updateTransitionIfHave(currentChildTop, stickyViewHeight);
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
        mStickyViewCache.draw(c, null);
    }

}
