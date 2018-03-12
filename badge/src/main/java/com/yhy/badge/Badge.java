package com.yhy.badge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-03-12 10:58
 * version: 1.0.0
 * desc   : 徽章控件接口
 */
public interface Badge {

    /**
     * 显示圆点徽章
     */
    void showCirclePointBadge();

    /**
     * 显示文本徽章
     *
     * @param badgeText 显示文本
     */
    void showTextBadge(String badgeText);

    /**
     * 隐藏徽章
     */
    void hiddenBadge();

    /**
     * 显示图像徽章
     *
     * @param bitmap 显示图片
     */
    void showDrawableBadge(Bitmap bitmap);

    /**
     * 调用父类的onTouchEvent方法
     *
     * @param event 当前事件
     * @return 父类onTouchEvent方法返回值
     */
    boolean callSuperOnTouchEvent(MotionEvent event);

    /**
     * 设置徽章消失回调事件
     *
     * @param listener 徽章消失回调事件
     */
    void setOnDismissListener(OnDismissListener listener);

    /**
     * 是否正在显示徽章
     *
     * @return 是否正在显示
     */
    boolean isShowBadge();

    /**
     * 获取当前徽章辅助类
     * <p>
     * 用于设置徽章颜色、字体颜色、大小等功能
     *
     * @return 当前徽章辅助类
     */
    BadgeViewHelper getBadgeViewHelper();

    /**
     * 获取控件宽度
     *
     * @return 控件宽度
     */
    int getWidth();

    /**
     * 获取控件高度
     *
     * @return 控件高度
     */
    int getHeight();

    /**
     * 刷新控件
     */
    void postInvalidate();

    /**
     * 获取父控件
     *
     * @return 父控件
     */
    ViewParent getParent();

    /**
     * 获取控件id
     *
     * @return 控件id
     */
    int getId();

    /**
     * 获取控件全局显示的区域
     *
     * @param r 用来接收范围的矩形
     * @return 是否获取成功
     */
    boolean getGlobalVisibleRect(Rect r);

    /**
     * 获取上下文
     *
     * @return 上下文
     */
    Context getContext();

    /**
     * 获取根控件
     *
     * @return 根控件
     */
    View getRootView();
}
