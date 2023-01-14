package com.github.hanlyjiang.app.recyclerview.sticky;

import android.view.View;

/**
 * 用于判断是否是吸附View
 */
public interface StickyViewTester {

    /**
     * 是否是吸附view
     *
     * @param view            待判断的View
     * @param adapterPosition 待判断的Item 在Adapter中的位置
     * @return true - 是需要吸附的View
     */
    boolean isStickyView(View view, int adapterPosition);
}
