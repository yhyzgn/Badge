package com.yhy.badge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.TypedValue;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-03-12 11:04
 * version: 1.0.0
 * desc   : 工具类
 */
public class BadgeViewUtils {

    private BadgeViewUtils() {
        throw new UnsupportedOperationException("Can not be instantiate.");
    }

    /**
     * dp转换为px
     *
     * @param context 上下文
     * @param dpValue dp
     * @return px
     */
    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    /**
     * sp转换为px
     *
     * @param context 上下文
     * @param spValue sp
     * @return px
     */
    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 安全创建bitmap
     *
     * @param dragBadgeView 可拖拽的徽章控件
     * @param rect          bitmap区域
     * @param retryCount    重试次数
     * @return 创建的bitmap
     */
    public static Bitmap createBitmapSafely(DragBadgeView dragBadgeView, Rect rect, int retryCount) {
        try {
            dragBadgeView.setDrawingCacheEnabled(true);
            // 只裁剪徽章区域,不然会很卡
            return Bitmap.createBitmap(dragBadgeView.getDrawingCache(), rect.left < 0 ? 0 : rect.left, rect.top < 0 ? 0 : rect.top, rect.width(), rect.height());
        } catch (OutOfMemoryError e) {
            if (retryCount > 0) {
                System.gc();
                return createBitmapSafely(dragBadgeView, rect, retryCount - 1);
            }
            return null;
        }
    }

    /**
     * 两点之间的距离
     *
     * @param p0 点1
     * @param p1 点2
     * @return 距离
     */
    public static float getDistanceBetween2Points(PointF p0, PointF p1) {
        float distance = (float) Math.sqrt(Math.pow(p0.y - p1.y, 2) + Math.pow(p0.x - p1.x, 2));
        return distance;
    }

    /**
     * 获取中点
     *
     * @param p1 点1
     * @param p2 点2
     * @return 中点
     */
    public static PointF getMiddlePoint(PointF p1, PointF p2) {
        return new PointF((p1.x + p2.x) / 2.0f, (p1.y + p2.y) / 2.0f);
    }

    /**
     * 按比例获取两点之间的某个点
     *
     * @param p1      点1
     * @param p2      点2
     * @param percent 比例
     * @return 点
     */
    public static PointF getPointByPercent(PointF p1, PointF p2, float percent) {
        return new PointF(evaluate(percent, p1.x, p2.x), evaluate(percent, p1.y, p2.y));
    }

    /**
     * 按百分比计算起始值与终点值之间某个百分比的值
     *
     * @param fraction   百分比
     * @param startValue 起始值
     * @param endValue   终点值
     * @return 当前值
     */
    public static Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    /**
     * 交点坐标
     *
     * @param pMiddle 圆中点
     * @param radius  圆半径
     * @param lineK   直线斜率
     * @return 交点坐标
     */
    public static PointF[] getIntersectionPoints(PointF pMiddle, float radius, Double lineK) {
        PointF[] points = new PointF[2];

        float radian, xOffset = 0, yOffset = 0;
        if (lineK != null) {
            radian = (float) Math.atan(lineK);
            xOffset = (float) (Math.sin(radian) * radius);
            yOffset = (float) (Math.cos(radian) * radius);
        } else {
            xOffset = radius;
            yOffset = 0;
        }
        points[0] = new PointF(pMiddle.x + xOffset, pMiddle.y - yOffset);
        points[1] = new PointF(pMiddle.x - xOffset, pMiddle.y + yOffset);

        return points;
    }
}
