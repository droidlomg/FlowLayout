package com.lomg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.dayba.interviewdemo.R;

/**
 * 从左到右，从上到下，平铺每个子View
 *
 * @Author wang.xiaolong5 2018-3-12 14:36:38
 */
public class LomgFlowLayout extends ViewGroup {
    /**
     * 竖直方向上行与行之间的间距
     */
    private float mVerticalSpace = 0;
    /**
     * 水平方向上列与列的间距
     */
    private float mHorizontalSpace = 0;


    public LomgFlowLayout(Context context) {
        super(context);
        init(null, 0);
    }

    public LomgFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LomgFlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LomgFlowLayout, defStyle, 0);
        mVerticalSpace = a.getDimension(
                R.styleable.LomgFlowLayout_verticalSpacing,
                mVerticalSpace);
        mHorizontalSpace = a.getDimension(
                R.styleable.LomgFlowLayout_horizontalSpacing,
                mHorizontalSpace);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int widthTotalUsed = 0;//本行已经用了多少空间
        final int CAN_USE_WIDTH = widthSize - getPaddingLeft() - getPaddingRight();
        int totalHeightNeed = getPaddingTop() + getPaddingBottom();//所需要的全部高度，初始时候是需要把padding上下都加上
        int rowNum = 0;//总行数
        int tempCurrentMaxHeightThisLine = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            //先计算下要多大
            child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED));

            MarginLayoutParams childParam = (MarginLayoutParams) child.getLayoutParams();
            //计算下view需要多大的高度
            int childNeedHeight = child.getMeasuredHeight() + childParam.topMargin + childParam.bottomMargin;

            if (child.getMeasuredWidth() > CAN_USE_WIDTH) {  //如果某一个item比整行还长，就设定他最多一整行
                rowNum++;
                totalHeightNeed += tempCurrentMaxHeightThisLine;//记录下上一行多高
                //重新计算这个view需要多大空间
                child.measure(MeasureSpec.makeMeasureSpec(CAN_USE_WIDTH, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED));
                //更新一下当前这个View占用了多高
                tempCurrentMaxHeightThisLine = child.getMeasuredHeight() + childParam.topMargin + childParam.bottomMargin;
                widthTotalUsed = CAN_USE_WIDTH;
            } else {
                //否查看剩余的地方还够不够
                int leftWidth = CAN_USE_WIDTH - widthTotalUsed;
                //计算这个child总共需要多少空间
                int childWidthTotalNeed = child.getMeasuredWidth() + childParam.leftMargin + childParam.rightMargin;
                //如果当前这个不是本行第一个view，那么这两个view之间需要添加空隙
                if (widthTotalUsed != 0) {
                    childWidthTotalNeed += mHorizontalSpace;
                }
                //如果本行够地方
                if (leftWidth >= childWidthTotalNeed) {
                    tempCurrentMaxHeightThisLine = Math.max(tempCurrentMaxHeightThisLine, childNeedHeight);//记录本行最高高度
                    widthTotalUsed += childWidthTotalNeed;
                } else {     //不够地方 换行
                    rowNum++;
                    widthTotalUsed = childWidthTotalNeed;
                    //高度增加
                    totalHeightNeed += tempCurrentMaxHeightThisLine;
                    tempCurrentMaxHeightThisLine = childNeedHeight;
                }
            }
        }
        //加上最后一行的高度

        totalHeightNeed += tempCurrentMaxHeightThisLine;
        if (rowNum > 0) {
            totalHeightNeed += rowNum * mVerticalSpace;
        }

        setMeasuredDimension(widthSize, totalHeightNeed);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final int CAN_USE_WIDTH = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int widthTotalUsed = 0;//本行已经用了多少空间
        int totalHeightUsed = t + getPaddingTop();//已经使用了的高度
        int tempCurrentMaxHeightThisLine = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            //先计算下要多大
            MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
            int childNeedHeight = child.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;

            //查看剩余的地方还够不够
            int leftWidth = CAN_USE_WIDTH - widthTotalUsed;
            //计算这个child总共需要多少空间  ——自身宽度加上左右边距
            int childTotalNeed = (child.getMeasuredWidth() + ((MarginLayoutParams) child.getLayoutParams()).leftMargin
                    + ((MarginLayoutParams) child.getLayoutParams()).rightMargin);
            if (leftWidth != CAN_USE_WIDTH) {//如果不是第一个item 需要增加一个horizontalSpace
                childTotalNeed += mHorizontalSpace;
            }
            if (leftWidth >= childTotalNeed) {     //够地方
                int extraLength = 0;//记录是否需要多余的空间  ——也就是水平item之间的间隙
                if (leftWidth != CAN_USE_WIDTH) {//如果不是第一个item 需要增加一个horizontalSpace
                    extraLength = (int) mHorizontalSpace;
                }
                child.layout(l + widthTotalUsed + layoutParams.leftMargin + extraLength, totalHeightUsed + layoutParams.topMargin,
                        l + widthTotalUsed + layoutParams.leftMargin + child.getMeasuredWidth() + extraLength,
                        totalHeightUsed + layoutParams.topMargin + child.getMeasuredHeight());

                tempCurrentMaxHeightThisLine = Math.max(tempCurrentMaxHeightThisLine, childNeedHeight);//记录本行最高高度
                widthTotalUsed += childTotalNeed + extraLength;
            } else {     //不够地方 换行
                //已经被使用了的高度增加
                totalHeightUsed += tempCurrentMaxHeightThisLine + mVerticalSpace;
                child.layout(l + layoutParams.leftMargin, totalHeightUsed + layoutParams.topMargin,
                        l + layoutParams.leftMargin + child.getMeasuredWidth(),
                        totalHeightUsed + layoutParams.topMargin + child.getMeasuredHeight());
                widthTotalUsed = childTotalNeed;
                tempCurrentMaxHeightThisLine = childNeedHeight;
            }
        }
    }


    public float getExampleDimension() {
        return mVerticalSpace;
    }


    @Override
    public LayoutParams getLayoutParams() {
        return super.getLayoutParams();
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MyLayoutParams(getContext(), attrs);
    }

    @Override
    public void setLayoutParams(LayoutParams params) {
        super.setLayoutParams(params);
    }

    @Override
    protected void attachLayoutAnimationParameters(View child, LayoutParams params, int index, int count) {
        super.attachLayoutAnimationParameters(child, params, index, count);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MyLayoutParams(-1, -1);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        if (p instanceof MarginLayoutParams) {
            return p;
        } else {
            return new MyLayoutParams(p.width, p.height);
        }
    }

    static class MyLayoutParams extends MarginLayoutParams {

        public MyLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public MyLayoutParams(int width, int height) {
            super(width, height);
        }

        public MyLayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public MyLayoutParams(LayoutParams source) {
            super(source);
        }
    }
}
