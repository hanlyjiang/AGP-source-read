package com.github.hanlyjiang.app.recyclerview.sticky;

import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


/**
 * 支持顶部吸附的ItemDecoration
 * <BR/>
 * 限制：RecyclerView 首次显示时，第一个需要粘性显示的Item必须在屏幕中。
 *
 * @author hhjiang 2023/1/13 14:03
 * @version 1.0
 */
public class SectionItemDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = SectionItemDecoration.class.getSimpleName();

    private final StickyViewTester mStickyViewTester;
    private final StickyViewCache mStickyViewCache = new StickyViewCache();

    /**
     * Constructor
     *
     * @param stickyViewTester StickyViewTester
     */
    public SectionItemDecoration(StickyViewTester stickyViewTester) {
        this.mStickyViewTester = stickyViewTester;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        // 1. 查找并设置 StickyView
        findStickyView(parent);
        // 2. 绘制 StickyView
        drawStickyView(parent, c);
    }

    /**
     * 获取当前正在显示的粘性View对应的 AdapterPosition
     *
     * @return AdapterPosition
     */
    public int getCurrentStickyViewAdapterPosition() {
        return mStickyViewCache.mCurrentStickyViewAdapterPosition;
    }

    /**
     * 查找 StickyView
     *
     * @param parent RecyclerView
     */
    protected void findStickyView(RecyclerView parent) {
        // parent.getChildCount() 获取的是当前可用的View（RecyclerView中只有可见的几个）
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
                    mStickyViewCache.setUseCurrent(parent, currentChild, childAdapterPosition);
                } else {
                    mStickyViewCache.setUsePrevious(childAdapterPosition);
                }
                // 交替区域，设置平移效果
                mStickyViewCache.updateTransition(calculateTransition(currentChildTop, stickyViewHeight));
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
    protected int calculateTransition(int currentChildTop, int stickyViewHeight) {
        if (currentChildTop > 0 && currentChildTop <= stickyViewHeight) {
            return currentChildTop - stickyViewHeight;
        } else {
            return 0;
        }
    }

    /**
     * 绘制
     *
     * @param parent RecyclerView
     * @param c      Canvas
     */
    protected void drawStickyView(RecyclerView parent, @NonNull Canvas c) {
        mStickyViewCache.draw(c);
    }

    /**
     * 缓存工具类
     */
    protected static class StickyViewCache {

        private RecyclerView.Adapter mAdapter;
        private RecyclerView mRecyclerView;
        private RecyclerView.ViewHolder mViewHolder;

        private int mWidthMeasure, mHeightMeasure;

        private int mDrawTransition = 0;
        /**
         * 当前使用的StickyView在 Adapter 中的位置
         */
        private int mCurrentStickyViewAdapterPosition = RecyclerView.NO_POSITION;

        private final List<Integer> mStickyViewAdapterPositionList = new ArrayList<>(3);

        /**
         * 保存StickyView index at adapter
         *
         * @param recyclerView         RecyclerView
         * @param currentChild         View
         * @param childAdapterPosition childAdapterPosition
         */
        public void setUseCurrent(RecyclerView recyclerView, View currentChild, int childAdapterPosition) {
            setAdapter(recyclerView);
            mWidthMeasure = currentChild.getWidth();
            mHeightMeasure = currentChild.getHeight();
            if (!mStickyViewAdapterPositionList.contains(childAdapterPosition)) {
                mStickyViewAdapterPositionList.add(childAdapterPosition);
                createOrUpdateViewHolder(childAdapterPosition);
                Log.d(TAG, "saveCurrentStickyView current=" + mCurrentStickyViewAdapterPosition + ";list=" + mStickyViewAdapterPositionList);
            } else {
                createOrUpdateViewHolder(childAdapterPosition);
            }
        }

        /**
         * 绘制
         *
         * @param c Canvas
         */
        public void draw(@NonNull Canvas c) {
            if (mViewHolder == null) {
                Log.w(TAG, "draw mViewHolder is null");
                return;
            }
            c.translate(0, mDrawTransition);
            mViewHolder.itemView.draw(c);
        }

        /**
         * 更新上下偏移
         *
         * @param transitionY transition Y
         */
        public void updateTransition(int transitionY) {
            mDrawTransition = transitionY;
        }

        public void setUsePrevious(int targetPosition) {
            int i = mStickyViewAdapterPositionList.indexOf(targetPosition);
            if ((i - 1) >= 0) {
                int newPos = mStickyViewAdapterPositionList.get(i - 1);
                if (mCurrentStickyViewAdapterPosition != newPos) {
                    createOrUpdateViewHolder(newPos);
                    Log.d(TAG, "restoreTopSecToPrv current=" + mCurrentStickyViewAdapterPosition + ";list=" + mStickyViewAdapterPositionList);
                }
            }
        }

        private void createOrUpdateViewHolder(int childAdapterPosition) {
            int oldAdapterPosition = mCurrentStickyViewAdapterPosition;
            if (oldAdapterPosition == childAdapterPosition) {
                Log.v(TAG, "createViewHolder targetStickyViewIndex == childAdapterPosition return");
                return;
            }
            mCurrentStickyViewAdapterPosition = childAdapterPosition;
            if (mCurrentStickyViewAdapterPosition == RecyclerView.NO_POSITION || mAdapter == null || mRecyclerView == null) {
                Log.d(TAG, "createViewHolder return");
                return;
            }
            int newItemViewType = mAdapter.getItemViewType(mCurrentStickyViewAdapterPosition);
            if (mViewHolder == null ||
                    (oldAdapterPosition != RecyclerView.NO_POSITION && newItemViewType != mAdapter.getItemViewType(oldAdapterPosition))
            ) {
                mViewHolder = mAdapter.onCreateViewHolder(mRecyclerView, newItemViewType);
            }
            // Execute data binding
            mAdapter.onBindViewHolder(mViewHolder, mCurrentStickyViewAdapterPosition);
            mViewHolder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(mWidthMeasure, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(mHeightMeasure, View.MeasureSpec.EXACTLY)
            );
            mViewHolder.itemView.layout(0, 0, mViewHolder.itemView.getMeasuredWidth(), mViewHolder.itemView.getMeasuredHeight());
        }

        private void setAdapter(RecyclerView recyclerView) {
            this.mRecyclerView = recyclerView;
            this.mAdapter = recyclerView.getAdapter();
            // TODO: 从 Adapter 中获取 StickyView 列表
        }
    }

}
