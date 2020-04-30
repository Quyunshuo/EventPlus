package com.quyunshuo.evnetplus;

/**
 * @Author: QuYunShuo
 * @Time: 2020/4/30
 * @Class: ThreadModel
 * @Remark: 线程模型
 */
public enum ThreadModel {
    // 跟随发送时的线程
    POSTING,
    // 主线程
    MAIN,
    // 子线程
    BACKGROUND
}
