package com.yhy.badge;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-03-12 10:59
 * version: 1.0.0
 * desc   : 徽章消失事件监听器
 */
public interface OnDismissListener {

    /**
     * 徽章消失回调
     *
     * @param badge 当前消失的徽章
     */
    void onDismiss(Badge badge);
}
