package com.lex.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Alex on 21/12/2016.
 */

public class MyView extends View {

    public MyView(Context context){
        super(context);
    }

    public MyView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public MyView(Context context, AttributeSet attrs, int defaultStyle){
        super(context, attrs, defaultStyle);
    }

    @Override
    public void onDraw (Canvas canvas){

    }

    @Override
    public void onMeasure(int widthMeasureSpec,
                          int heightMeasureSpec){
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int myHeight = heightSpecSize;

        if(heightSpecMode == MeasureSpec.EXACTLY)
            myHeight = heightSpecSize;
        else if(heightSpecMode == MeasureSpec.AT_MOST)
            myHeight = heightMeasureSpec;

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int myWidth = widthSpecSize;

        if(widthMeasureSpec == MeasureSpec.EXACTLY)
            myWidth = widthSpecSize;
        else if(widthSpecMode == MeasureSpec.AT_MOST)
            myWidth = widthMeasureSpec;

        setMeasuredDimension(myWidth, myHeight);
    }

}
