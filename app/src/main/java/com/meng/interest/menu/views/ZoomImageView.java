package com.meng.interest.menu.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * @author wangchengmeng
 *         可以缩放的ImageView
 */

public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {
    private boolean isOnce = true;
    private float                mInitScale;//最小的缩放值
    private float                mMidScale;//中等的缩放值
    private float                mMaxScale;//最大的缩放值
    private Matrix               mMatrix;
    private ScaleGestureDetector mScaleGestureDetector;

    /*自由移动的成员变量*/
    private int     mLastPointCount;//记录最后一次多点的个数
    private float   mLastX;
    private float   mLastY;//记录上一次X Y的坐标
    private boolean mIsCanDrag; //根据是否达到阈值 判断是否可以移动
    private int     mDragSlop;

    private boolean isDragVertical;
    private boolean isDragHorizontal;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mMatrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
        //多点触控
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this);
        mDragSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //注册 OnGlobalLayoutListener
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //注销 OnGlobalLayoutListener
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        //因为要在加载完成的时候就获取到图片的宽高 然后让图片的宽高去适应控件的宽高大小
        if (isOnce) {
            //获取控件的宽高
            int width = getWidth();
            int height = getHeight();
            //获取图片
            Drawable drawable = getDrawable();
            if (null == drawable) {
                return;
            }
            //获取到图片的宽高
            int dw = drawable.getIntrinsicWidth();
            int dh = drawable.getIntrinsicHeight();

            //定义一个图片缩放值
            float scale = 1.0f;

            //当图片的宽大于了控件的宽  图片的高小于控件的高
            if (dw > width && dh < height) {
                scale = width * 1.0f / dw;
            }
            //当图片的宽小于了控件的宽  图片的高大于控件的高
            if (dw < width && dh > height) {
                scale = height * 1.0f / dh;
            }

            if ((dw > width && dh > height) || (dw < width && dh < height)) {
                scale = Math.min((width * 1.0f / dw), (height * 1.0f / dh));
            }
            //初始化三个缩放的值
            mInitScale = scale;
            mMidScale = scale * 2;
            mMaxScale = scale * 4;

            //将图片初始化加载到控件的正中心位置
            //获取中心位置
            float dx = getWidth() / 2f - dw / 2f;
            float dy = getHeight() / 2f - dh / 2f;
            //使用矩阵控制图片的平移和缩放
            mMatrix.postTranslate(dx, dy);
            //缩放的时候要指定缩放基准点
            mMatrix.postScale(mInitScale, mInitScale, getWidth() / 2f, getHeight() / 2f);

            setImageMatrix(mMatrix);

            isOnce = false;
        }
    }

    private float getCurrentScale() {
        float[] values = new float[9];
        mMatrix.getValues(values);//矩阵在内存中其实就是一个一维数组
        return values[Matrix.MSCALE_X];
    }

    //使用ScaleGestureListener去实现多点触控
    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        if (null == getDrawable()) {
            return true;
        }

        //缩放中
        //获取当前图片缩放scale
        float scale = getCurrentScale();

        //获取缩放因子
        float scaleFactor = scaleGestureDetector.getScaleFactor();
        //缩放值达到最大和最小的情况
        if ((scale < mMaxScale && scaleFactor > 1.0f) || scale > mInitScale && scaleFactor < 1.0f) {
            if (scale * scaleFactor < mInitScale) {
                scaleFactor = mInitScale / scale;
            } else if (scale * scaleFactor > mMaxScale) {
                scaleFactor = mMaxScale / scale;
            }
        }

        //根据缩放因子去设置图片的缩放  根据多点的中心去缩放
        mMatrix.postScale(scaleFactor, scaleFactor, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());

        //因为缩放的中心点会改变  所以要控制图片的边界处理
        checkoutBounds();

        setImageMatrix(mMatrix);
        return true;
    }

    private void checkoutBounds() {
        //通过矩阵要获取到缩放后图片的大小和坐标
        Drawable drawable = getDrawable();
        if (null != drawable) {
            RectF rectF = getScaleMatrix(drawable);
            //获取控件的宽高
            int width = getWidth();
            int height = getHeight();
            //声明 x y偏移值
            float detalX = 0;
            float detalY = 0;
            //宽度控制
            if (rectF.width() >= width) {
                //图片的宽大于等于了控件的宽，为了让宽留白边，计算出应该左右移动的偏移值
                if (0 < rectF.left) {
                    //左边留空白了 那就应该像左移动
                    detalX = -rectF.left;
                } else if (rectF.right < width) {
                    detalX = width - rectF.right;
                }
            }
            //高度控制
            if (rectF.height() >= height) {
                if (0 < rectF.top) {
                    detalY = -rectF.top;
                } else if (rectF.bottom < height) {
                    detalY = height - rectF.bottom;
                }
            }

            //图片宽和高小于控件宽高的情况，让图片居中显示
            if (rectF.width() < width) {
                //计算偏移值
                detalX = width / 2f - rectF.right + rectF.width() / 2f;
            }

            if (rectF.height() < height) {
                detalY = height / 2f - rectF.bottom + rectF.height() / 2f;
            }
            mMatrix.postTranslate(detalX, detalY);
        }

    }

    //通过矩阵 去获取到缩放后的图片的四个顶点坐标
    public RectF getScaleMatrix(Drawable drawable) {
        Matrix matrix = mMatrix;

        //图片的四个点坐标
        RectF rectF = new RectF(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        matrix.mapRect(rectF);

        return rectF;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        //开始缩放
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        //缩放结束
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //将触摸事件传递给ScaleGesture
        mScaleGestureDetector.onTouchEvent(motionEvent);

        //实现图片的自由移动
        float x = 0;
        float y = 0;//记录中心点位置
        int pointCount = motionEvent.getPointerCount();
        for (int i = 0; i < pointCount; i++) {
            x += motionEvent.getX(i);
            y += motionEvent.getY(i);
        }
        x /= pointCount;
        y /= pointCount;//根据多点的个数和xy坐标 计算出中心坐标

        if (mLastPointCount != pointCount) {
            mIsCanDrag = false;
            mLastX = x;
            mLastY = y;
        }
        mLastPointCount = pointCount;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //偏移值
                float dx = x - mLastX;
                float dy = y - mLastY;
                if (!mIsCanDrag) {
                    mIsCanDrag = isMoveAction(dx, dy);
                } else {
                    if (null != getDrawable()) {
                        RectF rectF = getScaleMatrix(getDrawable());
                        isDragVertical = isDragHorizontal = true;
                        if (rectF.width() < getWidth()) {
                            //如果图片的宽度小于了控件的宽度那就横着就不用移动了
                            dx = 0;
                            isDragHorizontal = false;
                        }
                        if (rectF.height() < getHeight()) {
                            isDragVertical = false;
                            dy = 0;
                        }
                        mMatrix.postTranslate(dx, dy);
                        checkTranslateBounds();
                        setImageMatrix(mMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLastPointCount = 0;
                break;
        }
        return true;
    }

    private void checkTranslateBounds() {
        float dx = 0;
        float dy = 0;
        RectF rectF = getScaleMatrix(getDrawable());

        int width = getWidth();
        int height = getHeight();
        if (rectF.top > 0 && isDragVertical) {
            dy = -rectF.top;
        }
        if (rectF.bottom < height && isDragVertical) {
            dy = height - rectF.bottom;
        }
        if (rectF.left > 0 && isDragHorizontal) {
            dx = -rectF.left;
        }
        if (rectF.right < width && isDragHorizontal) {
            dx = width - rectF.right;
        }
        mMatrix.postTranslate(dx, dy);
    }

    private boolean isMoveAction(double dx, double dy) {
        return Math.sqrt(dx * dx + dy * dy) > mDragSlop;
    }

}
