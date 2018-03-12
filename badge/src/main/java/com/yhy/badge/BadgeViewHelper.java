package com.yhy.badge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-03-12 11:00
 * version: 1.0.0
 * desc   : 徽章控件助手类
 */
public class BadgeViewHelper {
    private Bitmap mBitmap;
    private Badge mBadge;
    private Paint mBadgePaint;
    /**
     * 徽章背景色
     */
    private int mBadgeBgColor;
    /**
     * 徽章文本的颜色
     */
    private int mBadgeTextColor;
    /**
     * 徽章文本字体大小
     */
    private int mBadgeTextSize;
    /**
     * 徽章背景与宿主控件上下边缘间距离
     */
    private int mBadgeVerticalMargin;
    /**
     * 徽章背景与宿主控件左右边缘间距离
     */
    private int mBadgeHorizontalMargin;
    /***
     * 徽章文本边缘与徽章背景边缘间的距离
     */
    private int mBadgePadding;
    /**
     * 徽章文本
     */
    private String mBadgeText;
    /**
     * 徽章文本所占区域大小
     */
    private Rect mBadgeNumberRect;
    /**
     * 是否显示Badge
     */
    private boolean mIsShowBadge;
    /**
     * 徽章在宿主控件中的位置
     */
    private BadgeGravity mBadgeGravity;
    /**
     * 整个徽章所占区域
     */
    private RectF mBadgeRectF;
    /**
     * 是否可拖动
     */
    private boolean mDragEnable;
    /**
     * 拖拽徽章超出轨迹范围后，再次放回到轨迹范围时，是否恢复轨迹
     */
    private boolean mResumeTravel;
    /***
     * 徽章描边宽度
     */
    private int mBadgeBorderWidth;
    /***
     * 徽章描边颜色
     */
    private int mBadgeBorderColor;
    /**
     * 触发开始拖拽徽章事件的扩展触摸距离
     */
    private int mDragExtra;
    /**
     * 整个徽章加上其触发开始拖拽区域所占区域
     */
    private RectF mBadgeDragExtraRectF;
    /**
     * 拖动时的徽章控件
     */
    private DragBadgeView mDropBadgeView;
    /**
     * 是否正在拖动
     */
    private boolean mIsDraging;
    /**
     * 拖动大于BadgeViewHelper.mMoveHiddenThreshold后抬起手指徽章消失的代理
     */
    private OnDismissListener mListener;
    private boolean mIsShowDrawable = false;

    /**
     * 构造方法
     *
     * @param badge               当前徽章控件
     * @param context             上下文
     * @param attrs               属性集
     * @param defaultBadgeGravity 默认对齐方式
     */
    public BadgeViewHelper(Badge badge, Context context, AttributeSet attrs, BadgeGravity defaultBadgeGravity) {
        mBadge = badge;
        initDefaultAttrs(context, defaultBadgeGravity);
        initCustomAttrs(context, attrs);
        afterInitDefaultAndCustomAttrs();
        mDropBadgeView = new DragBadgeView(context, this);
    }

    /**
     * 初始化属性集
     *
     * @param context             上下文
     * @param defaultBadgeGravity 默认对齐方式
     */
    private void initDefaultAttrs(Context context, BadgeGravity defaultBadgeGravity) {
        mBadgeNumberRect = new Rect();
        mBadgeRectF = new RectF();
        mBadgeBgColor = Color.RED;
        mBadgeTextColor = Color.WHITE;
        mBadgeTextSize = BadgeViewUtils.sp2px(context, 10);

        mBadgePaint = new Paint();
        mBadgePaint.setAntiAlias(true);
        mBadgePaint.setStyle(Paint.Style.FILL);
        // 设置mBadgeText居中，保证mBadgeText长度为1时，文本也能居中
        mBadgePaint.setTextAlign(Paint.Align.CENTER);

        mBadgePadding = BadgeViewUtils.dp2px(context, 4);
        mBadgeVerticalMargin = BadgeViewUtils.dp2px(context, 4);
        mBadgeHorizontalMargin = BadgeViewUtils.dp2px(context, 4);

        mBadgeGravity = defaultBadgeGravity;
        mIsShowBadge = false;

        mBadgeText = null;

        mBitmap = null;

        mIsDraging = false;

        mDragEnable = false;

        mBadgeBorderColor = Color.WHITE;

        mDragExtra = BadgeViewUtils.dp2px(context, 4);
        mBadgeDragExtraRectF = new RectF();
    }

    /**
     * 初始化自定义属性集
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    private void initCustomAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Badge);
        final int N = typedArray.getIndexCount();
        for (int i = 0; i < N; i++) {
            initCustomAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();
    }

    /**
     * 解析自定义属性
     *
     * @param attr       属性id
     * @param typedArray 属性集
     */
    private void initCustomAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.Badge_bdg_bg_color) {
            mBadgeBgColor = typedArray.getColor(attr, mBadgeBgColor);
        } else if (attr == R.styleable.Badge_bdg_text_color) {
            mBadgeTextColor = typedArray.getColor(attr, mBadgeTextColor);
        } else if (attr == R.styleable.Badge_bdg_text_size) {
            mBadgeTextSize = typedArray.getDimensionPixelSize(attr, mBadgeTextSize);
        } else if (attr == R.styleable.Badge_bdg_vertical_margin) {
            mBadgeVerticalMargin = typedArray.getDimensionPixelSize(attr, mBadgeVerticalMargin);
        } else if (attr == R.styleable.Badge_bdg_horizontal_margin) {
            mBadgeHorizontalMargin = typedArray.getDimensionPixelSize(attr, mBadgeHorizontalMargin);
        } else if (attr == R.styleable.Badge_bdg_padding) {
            mBadgePadding = typedArray.getDimensionPixelSize(attr, mBadgePadding);
        } else if (attr == R.styleable.Badge_bdg_gravity) {
            int ordinal = typedArray.getInt(attr, mBadgeGravity.ordinal());
            mBadgeGravity = BadgeGravity.values()[ordinal];
        } else if (attr == R.styleable.Badge_bdg_drag_enable) {
            mDragEnable = typedArray.getBoolean(attr, mDragEnable);
        } else if (attr == R.styleable.Badge_bdg_resume_travel) {
            mResumeTravel = typedArray.getBoolean(attr, mResumeTravel);
        } else if (attr == R.styleable.Badge_bdg_border_width) {
            mBadgeBorderWidth = typedArray.getDimensionPixelSize(attr, mBadgeBorderWidth);
        } else if (attr == R.styleable.Badge_bdg_border_color) {
            mBadgeBorderColor = typedArray.getColor(attr, mBadgeBorderColor);
        } else if (attr == R.styleable.Badge_bdg_drag_extra) {
            mDragExtra = typedArray.getDimensionPixelSize(attr, mDragExtra);
        }
    }

    /**
     * 初始化后设置属性
     */
    private void afterInitDefaultAndCustomAttrs() {
        mBadgePaint.setTextSize(mBadgeTextSize);
    }

    /**
     * 设置徽章背景颜色
     *
     * @param badgeBgColor 背景颜色
     */
    public void setBadgeBgColorInt(int badgeBgColor) {
        mBadgeBgColor = badgeBgColor;
        mBadge.postInvalidate();
    }

    /**
     * 设置徽章字体颜色
     *
     * @param badgeTextColor 字体颜色
     */
    public void setBadgeTextColorInt(int badgeTextColor) {
        mBadgeTextColor = badgeTextColor;
        mBadge.postInvalidate();
    }

    /**
     * 设置徽章字体大小
     *
     * @param badgeTextSize 字体大小
     */
    public void setBadgeTextSizeSp(int badgeTextSize) {
        if (badgeTextSize >= 0) {
            mBadgeTextSize = BadgeViewUtils.sp2px(mBadge.getContext(), badgeTextSize);
            mBadgePaint.setTextSize(mBadgeTextSize);
            mBadge.postInvalidate();
        }
    }

    /**
     * 设置徽章垂直方向外边距
     *
     * @param badgeVerticalMargin 外边距
     */
    public void setBadgeVerticalMarginDp(int badgeVerticalMargin) {
        if (badgeVerticalMargin >= 0) {
            mBadgeVerticalMargin = BadgeViewUtils.dp2px(mBadge.getContext(), badgeVerticalMargin);
            mBadge.postInvalidate();
        }
    }

    /**
     * 设置徽章水平方向外边距
     *
     * @param badgeHorizontalMargin 外边距
     */
    public void setBadgeHorizontalMarginDp(int badgeHorizontalMargin) {
        if (badgeHorizontalMargin >= 0) {
            mBadgeHorizontalMargin = BadgeViewUtils.dp2px(mBadge.getContext(), badgeHorizontalMargin);
            mBadge.postInvalidate();
        }
    }

    /**
     * 设置徽章内边距
     *
     * @param badgePadding 内边距
     */
    public void setBadgePaddingDp(int badgePadding) {
        if (badgePadding >= 0) {
            mBadgePadding = BadgeViewUtils.dp2px(mBadge.getContext(), badgePadding);
            mBadge.postInvalidate();
        }
    }

    /**
     * 设置对齐方式
     *
     * @param badgeGravity 对齐方式
     */
    public void setBadgeGravity(BadgeGravity badgeGravity) {
        if (badgeGravity != null) {
            mBadgeGravity = badgeGravity;
            mBadge.postInvalidate();
        }
    }

    /**
     * 设置是否可拖动
     *
     * @param dragEnable 是否可拖动
     */
    public void setDragEnable(boolean dragEnable) {
        mDragEnable = dragEnable;
        mBadge.postInvalidate();
    }

    /**
     * 设置是否可恢复轨迹
     *
     * @param resumeTravel 是否可恢复轨迹
     */
    public void setResumeTravel(boolean resumeTravel) {
        mResumeTravel = resumeTravel;
        mBadge.postInvalidate();
    }

    /**
     * 设置边框宽度
     *
     * @param badgeBorderWidthDp 边框宽度
     */
    public void setBadgeBorderWidthDp(int badgeBorderWidthDp) {
        if (badgeBorderWidthDp >= 0) {
            mBadgeBorderWidth = BadgeViewUtils.dp2px(mBadge.getContext(), badgeBorderWidthDp);
            mBadge.postInvalidate();
        }
    }

    /**
     * 设置边框颜色
     *
     * @param badgeBorderColor 边框颜色
     */
    public void setBadgeBorderColorInt(int badgeBorderColor) {
        mBadgeBorderColor = badgeBorderColor;
        mBadge.postInvalidate();
    }

    /**
     * 触摸事件处理
     *
     * @param event 当前事件
     * @return 事件处理结果
     */
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mBadgeDragExtraRectF.left = mBadgeRectF.left - mDragExtra;
                mBadgeDragExtraRectF.top = mBadgeRectF.top - mDragExtra;
                mBadgeDragExtraRectF.right = mBadgeRectF.right + mDragExtra;
                mBadgeDragExtraRectF.bottom = mBadgeRectF.bottom + mDragExtra;

                if ((mBadgeBorderWidth == 0 || mIsShowDrawable) && mDragEnable && mIsShowBadge && mBadgeDragExtraRectF.contains(event.getX(), event.getY())) {
                    mIsDraging = true;
                    mBadge.getParent().requestDisallowInterceptTouchEvent(true);

                    Rect badgeableRect = new Rect();
                    mBadge.getGlobalVisibleRect(badgeableRect);
                    mDropBadgeView.setStickCenter(badgeableRect.left + mBadgeRectF.left + mBadgeRectF.width() / 2, badgeableRect.top + mBadgeRectF.top + mBadgeRectF.height() / 2);

                    mDropBadgeView.onTouchEvent(event);
                    mBadge.postInvalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsDraging) {
                    mDropBadgeView.onTouchEvent(event);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsDraging) {
                    mDropBadgeView.onTouchEvent(event);
                    mIsDraging = false;
                    return true;
                }
                break;
            default:
                break;
        }
        return mBadge.callSuperOnTouchEvent(event);
    }

    /**
     * 动画结束并消失
     */
    public void endDragWithDismiss() {
        hiddenBadge();
        if (mListener != null) {
            mListener.onDismiss(mBadge);
        }
    }

    /**
     * 动画结束未消失
     */
    public void endDragWithoutDismiss() {
        mBadge.postInvalidate();
    }

    /**
     * 绘制徽章
     *
     * @param canvas 当前画布
     */
    public void drawBadge(Canvas canvas) {
        if (mIsShowBadge && !mIsDraging) {
            if (mIsShowDrawable) {
                drawDrawableBadge(canvas);
            } else {
                drawTextBadge(canvas);
            }
        }
    }

    /**
     * 绘制图像徽章
     *
     * @param canvas 当前画布
     */
    private void drawDrawableBadge(Canvas canvas) {
        mBadgeRectF.left = mBadge.getWidth() - mBadgeHorizontalMargin - mBitmap.getWidth();
        mBadgeRectF.top = mBadgeVerticalMargin;
        switch (mBadgeGravity) {
            case RightTop:
                mBadgeRectF.top = mBadgeVerticalMargin;
                break;
            case RightCenter:
                mBadgeRectF.top = (mBadge.getHeight() - mBitmap.getHeight()) / 2;
                break;
            case RightBottom:
                mBadgeRectF.top = mBadge.getHeight() - mBitmap.getHeight() - mBadgeVerticalMargin;
                break;
            default:
                break;
        }
        canvas.drawBitmap(mBitmap, mBadgeRectF.left, mBadgeRectF.top, mBadgePaint);
        mBadgeRectF.right = mBadgeRectF.left + mBitmap.getWidth();
        mBadgeRectF.bottom = mBadgeRectF.top + mBitmap.getHeight();
    }

    /**
     * 绘制文字徽章
     *
     * @param canvas 当前画布
     */
    private void drawTextBadge(Canvas canvas) {
        String badgeText = "";
        if (!TextUtils.isEmpty(mBadgeText)) {
            badgeText = mBadgeText;
        }
        // 获取文本宽所占宽高
        mBadgePaint.getTextBounds(badgeText, 0, badgeText.length(), mBadgeNumberRect);
        // 计算徽章背景的宽高
        int badgeHeight = mBadgeNumberRect.height() + mBadgePadding * 2;
        int badgeWidth;
        // 当mBadgeText的长度为1或0时，计算出来的高度会比宽度大，此时设置宽度等于高度
        if (badgeText.length() == 1 || badgeText.length() == 0) {
            badgeWidth = badgeHeight;
        } else {
            badgeWidth = mBadgeNumberRect.width() + mBadgePadding * 2;
        }

        // 计算徽章背景上下的值
        mBadgeRectF.top = mBadgeVerticalMargin;
        mBadgeRectF.bottom = mBadge.getHeight() - mBadgeVerticalMargin;
        switch (mBadgeGravity) {
            case RightTop:
                mBadgeRectF.bottom = mBadgeRectF.top + badgeHeight;
                break;
            case RightCenter:
                mBadgeRectF.top = (mBadge.getHeight() - badgeHeight) / 2;
                mBadgeRectF.bottom = mBadgeRectF.top + badgeHeight;
                break;
            case RightBottom:
                mBadgeRectF.top = mBadgeRectF.bottom - badgeHeight;
                break;
            default:
                break;
        }

        // 计算徽章背景左右的值
        mBadgeRectF.right = mBadge.getWidth() - mBadgeHorizontalMargin;
        mBadgeRectF.left = mBadgeRectF.right - badgeWidth;

        if (mBadgeBorderWidth > 0) {
            // 设置徽章边框景色
            mBadgePaint.setColor(mBadgeBorderColor);
            // 绘制徽章边框背景
            canvas.drawRoundRect(mBadgeRectF, badgeHeight / 2, badgeHeight / 2, mBadgePaint);

            // 设置徽章背景色
            mBadgePaint.setColor(mBadgeBgColor);
            // 绘制徽章背景
            canvas.drawRoundRect(new RectF(mBadgeRectF.left + mBadgeBorderWidth, mBadgeRectF.top + mBadgeBorderWidth, mBadgeRectF.right - mBadgeBorderWidth, mBadgeRectF.bottom - mBadgeBorderWidth), (badgeHeight - 2 * mBadgeBorderWidth) / 2, (badgeHeight - 2 * mBadgeBorderWidth) / 2, mBadgePaint);
        } else {
            // 设置徽章背景色
            mBadgePaint.setColor(mBadgeBgColor);
            // 绘制徽章背景
            canvas.drawRoundRect(mBadgeRectF, badgeHeight / 2, badgeHeight / 2, mBadgePaint);
        }

        if (!TextUtils.isEmpty(mBadgeText)) {
            // 设置徽章文本颜色
            mBadgePaint.setColor(mBadgeTextColor);
            // initDefaultAttrs方法中设置了mBadgeText居中，此处的x为徽章背景的中心点y
            float x = mBadgeRectF.left + badgeWidth / 2;
            // 注意：绘制文本时的y是指文本底部，而不是文本的中间
            float y = mBadgeRectF.bottom - mBadgePadding;
            // 绘制徽章文本
            canvas.drawText(badgeText, x, y, mBadgePaint);
        }
    }

    /**
     * 显示圆点徽章
     */
    public void showCirclePointBadge() {
        showTextBadge(null);
    }

    /**
     * 显示文本徽章
     *
     * @param badgeText 文本
     */
    public void showTextBadge(String badgeText) {
        mIsShowDrawable = false;
        mBadgeText = badgeText;
        mIsShowBadge = true;
        mBadge.postInvalidate();
    }

    /**
     * 隐藏徽章
     */
    public void hiddenBadge() {
        mIsShowBadge = false;
        mBadge.postInvalidate();
    }

    /**
     * 徽章是否正在显示
     *
     * @return 是否正在显示
     */
    public boolean isShowBadge() {
        return mIsShowBadge;
    }

    /**
     * 显示图片徽章
     *
     * @param bitmap 图片
     */
    public void showDrawable(Bitmap bitmap) {
        mBitmap = bitmap;
        mIsShowDrawable = true;
        mIsShowBadge = true;
        mBadge.postInvalidate();
    }

    /**
     * 是否是图片徽章
     *
     * @return 是否是图片徽章
     */
    public boolean isShowDrawable() {
        return mIsShowDrawable;
    }

    /**
     * 获取徽章区域
     *
     * @return 徽章区域
     */
    public RectF getBadgeRectF() {
        return mBadgeRectF;
    }

    /**
     * 获取徽章内边距
     *
     * @return 内边距
     */
    public int getBadgePadding() {
        return mBadgePadding;
    }

    /**
     * 获取徽章文本
     *
     * @return 文本
     */
    public String getBadgeText() {
        return mBadgeText;
    }

    /**
     * 获取徽章背景颜色
     *
     * @return 背景颜色
     */
    public int getBadgeBgColor() {
        return mBadgeBgColor;
    }

    /**
     * 获取徽章字体颜色
     *
     * @return 字体颜色
     */
    public int getBadgeTextColor() {
        return mBadgeTextColor;
    }

    /**
     * 获取徽章字体大小
     *
     * @return 字体大小
     */
    public int getBadgeTextSize() {
        return mBadgeTextSize;
    }

    /**
     * 获取徽章图片
     *
     * @return 图片
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * 设置徽章消失监听器
     *
     * @param listener 监听器
     */
    public void setOnDismissListener(OnDismissListener listener) {
        mListener = listener;
    }

    /**
     * 获取徽章根控件
     *
     * @return 根控件
     */
    public View getRootView() {
        return mBadge.getRootView();
    }

    /**
     * 是否支持轨迹恢复
     *
     * @return 是否支持轨迹恢复
     */
    public boolean isResumeTravel() {
        return mResumeTravel;
    }

    /**
     * 对其方式枚举
     */
    public enum BadgeGravity {
        RightTop,
        RightCenter,
        RightBottom
    }
}
