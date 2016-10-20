package com.roundel.fizyka;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by RouNdeL on 2016-10-20.
 */

public class PriorityLayoutHorizontal extends ViewGroup
{

    private ArrayList<Integer> mPriorityHighChildren;

    public PriorityLayoutHorizontal(Context context)
    {
        this(context, null);
    }

    public PriorityLayoutHorizontal(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PriorityLayoutHorizontal(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = null;
        try
        {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.PriorityLayoutHorizontal, defStyleAttr, 0);
            final int id = typedArray.getResourceId(R.styleable.PriorityLayoutHorizontal_priorityHigh, -1);
            if(id != -1)
            {
                final int[] highPriorityChildren = getResources().getIntArray(id);
                setPriorityHigh(highPriorityChildren);
            }
        }
        finally
        {
            if(typedArray != null)
            {
                typedArray.recycle();
            }
        }
    }

    public void setPriorityHigh(int... priorityHigh)
    {
        Integer[] priorityHighInteger = new Integer[priorityHigh.length];
        int i = 0;
        for(int value : priorityHigh)
        {
            priorityHighInteger[i++] = value;
        }
        mPriorityHighChildren = new ArrayList<Integer>(Arrays.asList(priorityHighInteger));
    }

    int mMaxHeight = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthUsed = 0;
        int heightUsed = 0;
        final int childCount = getChildCount();

        for(int childPosition : mPriorityHighChildren)
        {
            final View childView = getChildAt(childPosition);
            if(childView.getVisibility() != View.GONE)
            {
                measureChildWithMargins(childView, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
                widthUsed += getMeasuredWidthWithMargins(childView);
                heightUsed = Math.max(getMeasuredHeightWithMargins(childView), heightUsed);
            }
        }

        for(int childPosition = 0; childPosition < childCount; childPosition++)
        {
            if(! mPriorityHighChildren.contains(childPosition))
            {
                final View childView = getChildAt(childPosition);
                if(childView.getVisibility() != View.GONE)
                {
                    measureChildWithMargins(childView, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
                    widthUsed += getMeasuredWidthWithMargins(childView);
                    heightUsed = Math.max(getMeasuredHeightWithMargins(childView), heightUsed);
                }
            }
        }

        mMaxHeight = heightUsed;

        int heightSize = heightUsed + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        int spaceUsed = paddingLeft;
        for (int childPosition = 0; childPosition < getChildCount(); childPosition++)
        {
            final View childView = getChildAt(childPosition);
            if(childView.getVisibility() != View.GONE)
            {
                final int top = (mMaxHeight / 2) - (childView.getMeasuredHeight() / 2);
                layoutView(childView, spaceUsed, paddingTop + top, childView.getMeasuredWidth(), childView.getMeasuredHeight());
                spaceUsed += getWidthWithMargins(childView);
            }
        }
    }

    private int getWidthWithMargins(View child)
    {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return child.getWidth() + lp.leftMargin + lp.rightMargin;
    }

    private int getHeightWithMargins(View child)
    {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
    }

    private int getMeasuredWidthWithMargins(View child)
    {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
    }

    private int getMeasuredHeightWithMargins(View child)
    {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
    }

    private void layoutView(View view, int left, int top, int width, int height)
    {
        MarginLayoutParams margins = (MarginLayoutParams) view.getLayoutParams();
        final int leftWithMargins = left + margins.leftMargin;
        final int topWithMargins = top + margins.topMargin;

        view.layout(leftWithMargins, topWithMargins, leftWithMargins + width, topWithMargins + height);
        //view.layout(0,0,200,200);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs)
    {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams()
    {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }
}
