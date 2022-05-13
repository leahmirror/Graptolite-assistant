package com.example.myapplication.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.example.myapplication.util.Utils;

@SuppressLint("ClickableViewAccessibility")
public class CropImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Bitmap mOrigBitmap = null; // 声明一个原始的位图对象
    private Bitmap mCropBitmap = null; // 声明一个裁剪后的位图对象
    private Rect mRect = new Rect(0, 0, 0, 0); // 矩形边界
    private int mInterval; // 与边缘线的间距阈值
    private float mOriginX, mOriginY; // 按下时候落点的横纵坐标
    private Rect mOriginRect; // 原始的矩形边界
    private float mLastOffsetX, mLastOffsetY; // 首要落点的上次横纵坐标偏差
    private float mLastOffsetXTwo, mLastOffsetYTwo; // 次要落点的上次横纵坐标偏差
    private boolean bReset = false; // 是否重新按下
    private float mScaleRatio = 1.0f; // 缩放比例
    private Bitmap mBitmap; // 声明一个位图对象
    private int mBitmapWidth; // 位图宽度
    private int mBitmapHeight; // 位图高度
    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInterval = Utils.dip2px(context, 10);
    }

    // 设置原始的位图对象
    public void setOrigBitmap(Bitmap orig) {
        mOrigBitmap = orig;
    }

    // 获得裁剪后的位图对象
    public Bitmap getCropBitmap() {
        return mCropBitmap;
    }

    // 设置位图的矩形边界
    public boolean setBitmapRect(Rect rect) {
        if (mOrigBitmap == null) { // 原始位图为空
            return false;
        } else if (rect.left < 0 || rect.left > mOrigBitmap.getWidth()) { // 左侧边界非法
            return false;
        } else if (rect.top < 0 || rect.top > mOrigBitmap.getHeight()) { // 上方边界非法
            return false;
        } else if (rect.right <= 0 || rect.left + rect.right > mOrigBitmap.getWidth()) { // 右侧边界非法
            return false;
        } else if (rect.bottom <= 0 || rect.top + rect.bottom > mOrigBitmap.getHeight()) { // 下方边界非法
            return false;
        }
        mRect = rect;
        // 设置视图的四周间隔
        setPadding(mRect.left, mRect.top, 0, 0);
        // 根据指定的四周边界，裁剪相应尺寸的位图对象
        mCropBitmap = Bitmap.createBitmap(mOrigBitmap,
                mRect.left, mRect.top, mRect.right, mRect.bottom);
        // 设置图像视图的位图内容
        setImageBitmap(mCropBitmap);
        postInvalidate(); // 立即刷新视图
        return true;
    }
    // 获取位图的矩形边界
    public Rect getBitmapRect() {
        return mRect;
    }
    // 在发生触摸事件时触发
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 手指按下
                mOriginX = event.getX();
                mOriginY = event.getY();
                mOriginRect = mRect;
                // 根据落点坐标与矩形边界的相对位置，决定本次拖曳动作的类型
                mDragMode = getDragMode(mOriginX, mOriginY);
                break;
            case MotionEvent.ACTION_MOVE: // 手指移动
                int offsetX = (int) (event.getX() - mOriginX);
                int offsetY = (int) (event.getY() - mOriginY);
                Rect rect = null;
                if (mDragMode == DRAG_NONE) { // 无拖曳动作
                    return true;
                } else if (mDragMode == DRAG_WHOLE) { // 拖动整个矩形边界框
                    rect = new Rect(mOriginRect.left + offsetX, mOriginRect.top + offsetY, mOriginRect.right, mOriginRect.bottom);
                } else if (mDragMode == DRAG_LEFT) { // 拖动矩形边界的左边缘
                    rect = new Rect(mOriginRect.left + offsetX, mOriginRect.top, mOriginRect.right - offsetX, mOriginRect.bottom);
                } else if (mDragMode == DRAG_RIGHT) { // 拖动矩形边界的右边缘
                    rect = new Rect(mOriginRect.left, mOriginRect.top, mOriginRect.right + offsetX, mOriginRect.bottom);
                } else if (mDragMode == DRAG_TOP) { // 拖动矩形边界的上边缘
                    rect = new Rect(mOriginRect.left, mOriginRect.top + offsetY, mOriginRect.right, mOriginRect.bottom - offsetY);
                } else if (mDragMode == DRAG_BOTTOM) { // 拖动矩形边界的下边缘
                    rect = new Rect(mOriginRect.left, mOriginRect.top, mOriginRect.right, mOriginRect.bottom + offsetY);
                } else if (mDragMode == DRAG_LEFT_TOP) { // 拖动矩形边界的左上角
                    rect = new Rect(mOriginRect.left + offsetX, mOriginRect.top + offsetY, mOriginRect.right - offsetX, mOriginRect.bottom - offsetY);
                } else if (mDragMode == DRAG_RIGHT_TOP) { // 拖动矩形边界的右上角
                    rect = new Rect(mOriginRect.left, mOriginRect.top + offsetY, mOriginRect.right + offsetX, mOriginRect.bottom - offsetY);
                } else if (mDragMode == DRAG_LEFT_BOTTOM) { // 拖动矩形边界的左下角
                    rect = new Rect(mOriginRect.left + offsetX, mOriginRect.top, mOriginRect.right - offsetX, mOriginRect.bottom + offsetY);
                } else if (mDragMode == DRAG_RIGHT_BOTTOM) { // 拖动矩形边界的右下角
                    rect = new Rect(mOriginRect.left, mOriginRect.top, mOriginRect.right + offsetX, mOriginRect.bottom + offsetY);
                }else if (mDragMode == IMAGE_SCALE_OR_ROTATE) { // 缩放图像
                    if (mListener != null) {
                        // 当前两个触摸点之间的距离
                        float nowWholeDistance = distance(event.getX(), event.getY(),
                                event.getX(1), event.getY(1));
                        // 上次两个触摸点之间的距离
                        float preWholeDistance = distance(mLastOffsetX, mLastOffsetY,
                                mLastOffsetXTwo, mLastOffsetYTwo);
                        // 主要点在前后两次落点之间的距离
                        float primaryDistance = distance(event.getX(), event.getY(),
                                mLastOffsetX, mLastOffsetY);
                        // 次要点在前后两次落点之间的距离
                        float secondaryDistance = distance(event.getX(1), event.getY(1),
                                mLastOffsetXTwo, mLastOffsetYTwo);
                        if (Math.abs(nowWholeDistance - preWholeDistance) >
                                (float) Math.sqrt(2) / 2.0f * (primaryDistance + secondaryDistance)) {
                            // 倾向于在原始线段的相同方向上移动，则判作缩放图像
                            // 触发图像变更监听器的缩放图像动作
                            mListener.onImageScale(nowWholeDistance / preWholeDistance);
                        }
                    }
                }
                setBitmapRect(rect); // 设置位图的矩形边界
                break;
            case MotionEvent.ACTION_UP: // 手指松开
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // 次要点按下
                // 多点触摸可能是缩放或者旋转
                mDragMode = IMAGE_SCALE_OR_ROTATE;
                bReset = true;
                break;
            case MotionEvent.ACTION_POINTER_UP: // 次要点松开
                mDragMode = DRAG_NONE;
                break;
            default:
                break;
        }
        mLastOffsetX = event.getX();
        mLastOffsetY = event.getY();
        if (event.getPointerCount() >= 2) { // 存在多点触摸
            mLastOffsetXTwo = event.getX(1);
            mLastOffsetYTwo = event.getY(1);
        }
        return true;
    }

    private int DRAG_NONE = 0; // 无拖曳动作
    private int DRAG_WHOLE = 1; // 拖动整个矩形边界框
    private int DRAG_LEFT = 2; // 拖动矩形边界的左边缘
    private int DRAG_RIGHT = 3; // 拖动矩形边界的右边缘
    private int DRAG_TOP = 4; // 拖动矩形边界的上边缘
    private int DRAG_BOTTOM = 5; // 拖动矩形边界的下边缘
    private int DRAG_LEFT_TOP = 6; // 拖动矩形边界的左上角
    private int DRAG_RIGHT_TOP = 7; // 拖动矩形边界的右上角
    private int DRAG_LEFT_BOTTOM = 8; // 拖动矩形边界的左下角
    private int DRAG_RIGHT_BOTTOM = 9; // 拖动矩形边界的右下角
    private int mDragMode = DRAG_NONE; // 拖曳动作的类型
    private int IMAGE_SCALE_OR_ROTATE = 11; // 缩放或者旋转图像
    // 计算两个坐标点之间的距离
    private float distance(float x1, float y1, float x2, float y2) {
        float offsetX = x2 - x1;
        float offsetY = y2 - y1;
        return (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
    }
    // 根据落点坐标与矩形边界的相对位置，决定本次拖曳动作的类型
    private int getDragMode(float f, float g) {
        int left = mRect.left;
        int top = mRect.top;
        int right = mRect.left + mRect.right;
        int bottom = mRect.top + mRect.bottom;
        if (Math.abs(f - left) <= mInterval && Math.abs(g - top) <= mInterval) {
            return DRAG_LEFT_TOP; // 拖动矩形边界的左上角
        } else if (Math.abs(f - right) <= mInterval && Math.abs(g - top) <= mInterval) {
            return DRAG_RIGHT_TOP; // 拖动矩形边界的右上角
        } else if (Math.abs(f - left) <= mInterval && Math.abs(g - bottom) <= mInterval) {
            return DRAG_LEFT_BOTTOM; // 拖动矩形边界的左下角
        } else if (Math.abs(f - right) <= mInterval && Math.abs(g - bottom) <= mInterval) {
            return DRAG_RIGHT_BOTTOM; // 拖动矩形边界的右下角
        } else if (Math.abs(f - left) <= mInterval && g > top + mInterval && g < bottom - mInterval) {
            return DRAG_LEFT; // 拖动矩形边界的左边缘
        } else if (Math.abs(f - right) <= mInterval && g > top + mInterval && g < bottom - mInterval) {
            return DRAG_RIGHT; // 拖动矩形边界的右边缘
        } else if (Math.abs(g - top) <= mInterval && f > left + mInterval && f < right - mInterval) {
            return DRAG_TOP; // 拖动矩形边界的上边缘
        } else if (Math.abs(g - bottom) <= mInterval && f > left + mInterval && f < right - mInterval) {
            return DRAG_BOTTOM; // 拖动矩形边界的下边缘
        } else if (f > left + mInterval && f < right - mInterval
                && g > top + mInterval && g < bottom - mInterval) {
            return DRAG_WHOLE; // 拖动整个矩形边界框
        } else {
            return DRAG_NONE; // 无拖曳动作
        }
    }
    private ImageChangetListener mListener; // 声明一个图像变更的监听器对象
    // 设置图像变更监听器
    public void setImageChangetListener(ImageChangetListener listener) {
        mListener = listener;
    }
    public void setScaleRatio(float ratio, boolean isReset) {
        if (isReset) {
            mScaleRatio = ratio;
        } else {
            mScaleRatio = mScaleRatio * ratio;
        }
        invalidate(); // 立即刷新视图
    }
    // 定义一个图像变更的监听器接口
    public interface ImageChangetListener {
        void onImageScale(float ratio); // 缩放图像
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap == null) {
            return;
        }
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int new_width = (int) (mBitmapWidth * mScaleRatio);
        int new_height = (int) (mBitmapHeight * mScaleRatio);
        // 生成缩放后的位图对象
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(mBitmap, new_width, new_height, false);

    }
}
