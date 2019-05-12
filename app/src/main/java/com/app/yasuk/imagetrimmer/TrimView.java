package com.app.yasuk.imagetrimmer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

public class TrimView extends View {

    private int imageWidth = 0;
    private int imageHeight = 0;
    private int originX = 0;
    private int originY = 0;

    // Paints
    private Paint paintRect;
    private Paint paintCircle;

    // Touch point
    private float touchX;
    private float touchY;

    // Rect's point
    private Point rectLeftTop;
    private Point rectLeftBottom;
    private Point rectRightTop;
    private Point rectRightBottom;

    // Move mode
    private static final int MOVE_LEFT_TOP = 1;
    private static final int MOVE_LEFT_BOTTOM = 2;
    private static final int MOVE_RIGHT_TOP = 3;
    private static final int MOVE_RIGHT_BOTTOM = 4;

    private int currentMoveMode = 0;

    private final int rectOffset=10;

    public TrimView(Context context) {
        super(context);

        paintRect = new Paint();
        paintRect.setStyle(Paint.Style.STROKE);
        paintRect.setStrokeWidth(7f);
        paintRect.setColor(Color.CYAN);

        paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(5f);
        paintCircle.setColor(Color.CYAN);
    }

    public void setSize(int x, int y, int w, int h) {
        originX = x;
        originY = y;
        imageWidth = w;
        imageHeight = h;

        rectLeftTop = new Point(originX, originY);
        rectLeftBottom = new Point(originX, originY+imageHeight);
        rectRightTop = new Point(originX+imageWidth, originY);
        rectRightBottom = new Point(originX+ imageWidth, originY+imageHeight);
    }

    public int getStartX() {
        return rectLeftTop.x - originX;
    }

    public int getStartY() {
        return rectLeftTop.y - originY;
    }

    public int getRectWidth() {
        return rectRightTop.x - rectLeftTop.x;
    }

    public int getRectHeight() {
        return rectLeftBottom.y - rectLeftTop.y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (currentMoveMode){
            case MOVE_LEFT_TOP:
                rectLeftTop.set((int)touchX, (int)touchY);
                rectLeftBottom.set((int)touchX, rectLeftBottom.y);
                rectRightTop.set(rectRightTop.x, (int)touchY);
                break;
            case MOVE_LEFT_BOTTOM:
                rectLeftBottom.set((int)touchX, (int)touchY);
                rectLeftTop.set((int)touchX, rectLeftTop.y);
                rectRightBottom.set(rectRightBottom.x, (int)touchY);
                break;
            case MOVE_RIGHT_TOP:
                rectRightTop.set((int)touchX, (int)touchY);
                rectLeftTop.set(rectLeftTop.x, (int)touchY);
                rectRightBottom.set((int)touchX, rectRightBottom.y);
                break;
            case MOVE_RIGHT_BOTTOM:
                rectRightBottom.set((int)touchX, (int)touchY);
                rectLeftBottom.set(rectLeftBottom.x, (int)touchY);
                rectRightTop.set((int)touchX, rectRightTop.y);
                break;
            default:
                break;
        }

        // draw rect
        canvas.drawRect(rectLeftTop.x, rectLeftTop.y, rectRightBottom.x, rectRightBottom.y, paintRect);

        // draw circles
        canvas.drawCircle(rectLeftTop.x, rectLeftTop.y, 15f, paintCircle);
        canvas.drawCircle(rectLeftBottom.x, rectLeftBottom.y, 15f, paintCircle);
        canvas.drawCircle(rectRightTop.x, rectRightTop.y, 15f, paintCircle);
        canvas.drawCircle(rectRightBottom.x, rectRightBottom.y, 15f, paintCircle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(event.getX() < originX){
                    touchX = originX;
                } else if(event.getX() > originX+imageWidth){
                    touchX = originX + imageWidth;
                } else {
                    touchX = event.getX();
                }

                if(event.getY() < originY){
                    touchY = originY;
                } else if(event.getY() > originY+imageHeight){
                    touchY = originY + imageHeight;
                } else {
                    touchY = event.getY();
                }

                float toRectLeftTop = culcDistance(touchX, touchY, rectLeftTop.x, rectLeftTop.y);
                float toRectLeftBottom = culcDistance(touchX, touchY, rectLeftBottom.x, rectLeftBottom.y);
                float toRectRightTop = culcDistance(touchX, touchY, rectRightTop.x, rectRightTop.y);
                float toRectRightBottom = culcDistance(touchX, touchY, rectRightBottom.x, rectRightBottom.y);

                float distanceArray[] = new float[]{
                        toRectLeftTop,
                        toRectLeftBottom,
                        toRectRightTop,
                        toRectRightBottom
                };

                float minDist = toRectLeftTop;
                for(float distance : distanceArray){
                    minDist = Math.min(minDist, distance);
                }

                if(minDist==toRectLeftTop){
                    currentMoveMode = MOVE_LEFT_TOP;
                } else if(minDist==toRectLeftBottom){
                    currentMoveMode = MOVE_LEFT_BOTTOM;
                } else if(minDist==toRectRightTop){
                    currentMoveMode = MOVE_RIGHT_TOP;
                } else if(minDist==toRectRightBottom){
                    currentMoveMode = MOVE_RIGHT_BOTTOM;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                updateTouchX(event.getX());
                updateTouchY(event.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        invalidate();
        return true;
    }

    private float culcDistance(float xSrc, float ySrc, float xDst,float yDst) {
        float x = xSrc - xDst;
        float y = ySrc - yDst;
        return (float)Math.sqrt(x * x + y * y);
    }

    private void updateTouchX(float posX){
        switch (currentMoveMode){
            case MOVE_LEFT_TOP:
            case MOVE_LEFT_BOTTOM:
                if(posX < originX){
                    touchX = originX;
                } else if((originX <= posX) && (posX < rectRightTop.x-rectOffset)){
                    touchX = posX;
                } else {
                    touchX = rectRightTop.x-rectOffset;
                }
                break;
            case MOVE_RIGHT_TOP:
            case MOVE_RIGHT_BOTTOM:
                if(originX+imageWidth < posX){
                    touchX = originX + imageWidth;
                } else if((rectLeftTop.x+rectOffset <= posX) && (posX <= originX+imageWidth)){
                    touchX = posX;
                } else {
                    touchX = rectLeftTop.x+rectOffset;
                }
                break;
        }
    }

    private void updateTouchY(float posY){
        switch (currentMoveMode) {
            case MOVE_LEFT_TOP:
            case MOVE_RIGHT_TOP:
                if(posY < originY){
                    touchY = originY;
                } else if((originY <= posY) && (posY < rectLeftBottom.y-rectOffset)){
                    touchY = posY;
                } else {
                    touchY = rectLeftBottom.y-rectOffset;
                }
                break;
            case MOVE_LEFT_BOTTOM:
            case MOVE_RIGHT_BOTTOM:
                if(originY+imageHeight < posY){
                    touchY = originY + imageHeight;
                } else if((rectLeftTop.y+rectOffset <=posY) && (posY < originY+imageHeight)){
                    touchY = posY;
                } else {
                    touchY = rectLeftTop.y+rectOffset;
                }
                break;
        }
    }
}
