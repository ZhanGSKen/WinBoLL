/*
 * Copyright 2026 WinBoLL Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package cc.winboll.studio.libappbase.views;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/12 12:29:01
 * @Describe 水平布局的 ListView
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.Scroller;
import cc.winboll.studio.libappbase.LogUtils;

public class HorizontalListView extends ListView {
    public static final String TAG = "HorizontalListView";
    private int verticalOffset = 0;
    private Scroller scroller;
    private int totalWidth;

    public HorizontalListView(Context context) {
        super(context);
        init();
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        scroller = new Scroller(getContext());
        setHorizontalScrollBarEnabled(true);
        setVerticalScrollBarEnabled(false);
    }

    public void setVerticalOffset(int verticalOffset) {
        this.verticalOffset = verticalOffset;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int childCount = getChildCount();
        int left = getPaddingLeft();
        int viewHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        totalWidth = left;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            child.layout(left, verticalOffset, left + width, verticalOffset + height);
            left += width;
        }
        totalWidth = left + getPaddingRight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    public void smoothScrollTo(int x, int y) {
        int dx = x - getScrollX();
        int dy = y - getScrollY();
        scroller.startScroll(getScrollX(), getScrollY(), dx, dy, 300); // 300ms平滑动画
        invalidate();
    }

    @Override
    public int computeHorizontalScrollRange() {
        return totalWidth;
    }

    @Override
    public int computeHorizontalScrollOffset() {
        return getScrollX();
    }

    @Override
    public int computeHorizontalScrollExtent() {
        return getWidth();
    }

    public void scrollToItem(int position) {
        if (position < 0 || position >= getChildCount()) {
            LogUtils.d(TAG, "无效的position: " + position);
            return;
        }

        View targetView = getChildAt(position);
        int targetLeft = targetView.getLeft();
        int scrollX = targetLeft - getPaddingLeft();

        // 修正最大滚动范围计算
        int maxScrollX = totalWidth;
        scrollX = Math.max(0, Math.min(scrollX, maxScrollX));

        // 强制重新布局和绘制
        requestLayout();
        invalidateViews();
        smoothScrollTo(scrollX, 0);
        LogUtils.d(TAG, String.format("滚动到position: %d, scrollX: %d computeHorizontalScrollRange() %d", position, scrollX, computeHorizontalScrollRange()));
    }

    public void resetScrollToStart() {
        // 强制重新布局和绘制
        requestLayout();
        invalidateViews();
        smoothScrollTo(0, 0);
    }
}

