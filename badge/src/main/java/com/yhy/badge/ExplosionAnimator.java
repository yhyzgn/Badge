package com.yhy.badge;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.Random;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-03-12 11:02
 * version: 1.0.0
 * desc   : 爆炸效果动画
 */
@SuppressWarnings("WeakerAccess")
public class ExplosionAnimator extends ValueAnimator {
    public static final int ANIM_DURATION = 300;
    private static final Interpolator DEFAULT_INTERPOLATOR = new AccelerateInterpolator(0.6f);
    private static final float END_VALUE = 1.4f;
    private static final int REFRESH_RATIO = 3;
    private static float X;
    private static float Y;
    private static float V;
    private static float W;

    private Particle[] mParticles;
    private Paint mPaint;
    private DragBadgeView mDragBadgeView;
    private Rect mRect;
    private Rect mInvalidateRect;

    /**
     * 构造函数
     *
     * @param dragBadgeView 可拖拽的徽章控件
     * @param rect          拖动区域
     * @param bitmap        图片
     */
    public ExplosionAnimator(DragBadgeView dragBadgeView, Rect rect, Bitmap bitmap) {
        setFloatValues(0.0f, END_VALUE);
        setDuration(ANIM_DURATION);
        setInterpolator(DEFAULT_INTERPOLATOR);

        X = BadgeViewUtils.dp2px(dragBadgeView.getContext(), 5);
        Y = BadgeViewUtils.dp2px(dragBadgeView.getContext(), 20);
        V = BadgeViewUtils.dp2px(dragBadgeView.getContext(), 2);
        W = BadgeViewUtils.dp2px(dragBadgeView.getContext(), 1);

        mPaint = new Paint();
        mDragBadgeView = dragBadgeView;
        mRect = rect;
        mInvalidateRect = new Rect(mRect.left - mRect.width() * REFRESH_RATIO, mRect.top - mRect.height() * REFRESH_RATIO, mRect.right + mRect.width() * REFRESH_RATIO, mRect.bottom + mRect.height() * REFRESH_RATIO);

        int partLen = 15;
        mParticles = new Particle[partLen * partLen];
        Random random = new Random(System.currentTimeMillis());
        int w = bitmap.getWidth() / (partLen + 2);
        int h = bitmap.getHeight() / (partLen + 2);
        // 生成爆炸碎片点
        for (int i = 0; i < partLen; i++) {
            for (int j = 0; j < partLen; j++) {
                mParticles[(i * partLen) + j] = generateParticle(bitmap.getPixel((j + 1) * w, (i + 1) * h), random);
            }
        }
    }

    /**
     * 生成爆炸碎片点
     *
     * @param color  颜色
     * @param random 随机数
     * @return 一个爆炸点
     */
    private Particle generateParticle(int color, Random random) {
        Particle particle = new Particle();
        particle.color = color;
        particle.radius = V;
        if (random.nextFloat() < 0.2f) {
            particle.baseRadius = V + ((X - V) * random.nextFloat());
        } else {
            particle.baseRadius = W + ((V - W) * random.nextFloat());
        }
        float nextFloat = random.nextFloat();
        particle.top = mRect.height() * ((0.18f * random.nextFloat()) + 0.2f);
        particle.top = nextFloat < 0.2f ? particle.top : particle.top + ((particle.top * 0.2f) * random.nextFloat());
        particle.bottom = (mRect.height() * (random.nextFloat() - 0.5f)) * 1.8f;
        float f = nextFloat < 0.2f ? particle.bottom : nextFloat < 0.8f ? particle.bottom * 0.6f : particle.bottom * 0.3f;
        particle.bottom = f;
        particle.mag = 4.0f * particle.top / particle.bottom;
        particle.neg = (-particle.mag) / particle.bottom;
        f = mRect.centerX() + (Y * (random.nextFloat() - 0.5f));
        particle.baseCx = f + mRect.width() / 2;
        particle.cx = particle.baseCx;
        f = mRect.centerY() + (Y * (random.nextFloat() - 0.5f));
        particle.baseCy = f;
        particle.cy = f;
        particle.life = END_VALUE / 10 * random.nextFloat();
        particle.overflow = 0.4f * random.nextFloat();
        particle.alpha = 1f;
        return particle;
    }

    /**
     * 绘制爆炸点
     *
     * @param canvas 当前画布
     */
    public void draw(Canvas canvas) {
        if (!isStarted()) {
            return;
        }
        for (Particle particle : mParticles) {
            particle.advance((float) getAnimatedValue());
            if (particle.alpha > 0f) {
                mPaint.setColor(particle.color);
                mPaint.setAlpha((int) (Color.alpha(particle.color) * particle.alpha));
                canvas.drawCircle(particle.cx, particle.cy, particle.radius, mPaint);
            }
        }
        postInvalidate();
    }

    /**
     * 开始动画
     */
    @Override
    public void start() {
        super.start();
        postInvalidate();
    }

    /**
     * 只刷新徽章附近的区域
     */
    private void postInvalidate() {
        mDragBadgeView.postInvalidate(mInvalidateRect.left, mInvalidateRect.top, mInvalidateRect.right, mInvalidateRect.bottom);
    }

    /**
     * 爆炸碎片
     */
    private class Particle {
        float alpha;
        int color;
        float cx;
        float cy;
        float radius;
        float baseCx;
        float baseCy;
        float baseRadius;
        float top;
        float bottom;
        float mag;
        float neg;
        float life;
        float overflow;

        /**
         * 实时计算当前碎片的大小和位置
         *
         * @param factor 动画比例
         */
        public void advance(float factor) {
            float f = 0f;
            float normalization = factor / END_VALUE;
            if (normalization < life || normalization > 1f - overflow) {
                alpha = 0f;
                return;
            }
            normalization = (normalization - life) / (1f - life - overflow);
            float f2 = normalization * END_VALUE;
            if (normalization >= 0.7f) {
                f = (normalization - 0.7f) / 0.3f;
            }
            alpha = 1f - f;
            f = bottom * f2;
            cx = baseCx + f;
            cy = (float) (baseCy - this.neg * Math.pow(f, 2.0)) - f * mag;
            radius = V + (baseRadius - V) * f2;
        }
    }
}
