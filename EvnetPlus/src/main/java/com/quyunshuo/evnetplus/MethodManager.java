package com.quyunshuo.evnetplus;

import java.lang.reflect.Method;

/**
 * @Author: QuYunShuo
 * @Time:   2020/4/30
 * @Class:  MethodManager
 * @Remark: 订阅方法的封装类
 */
public class MethodManager {

    /**
     * 方法本身
     */
    private Method method;

    /**
     * 这个方法的参数类型
     */
    private Class<?> type;

    /**
     * 这个订阅方法的线程模式
     */
    private ThreadModel threadModel;

    public MethodManager(Method method, Class<?> type, ThreadModel threadModel) {
        this.method = method;
        this.type = type;
        this.threadModel = threadModel;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getType() {
        return type;
    }

    public ThreadModel getThreadModel() {
        return threadModel;
    }
}
