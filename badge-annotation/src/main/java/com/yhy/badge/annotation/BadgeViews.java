package com.yhy.badge.annotation;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-03-12 10:40
 * version: 1.0.0
 * desc   : 生成相关BadgeView的注解
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface BadgeViews {

    /**
     * 需要生成BadgeView的控件
     *
     * @return 需要生成BadgeView的控件
     */
    Class<? extends View>[] value() default {};
}
