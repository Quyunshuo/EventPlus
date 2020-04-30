package com.quyunshuo.evnetplus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: QuYunShuo
 * @Time: 2020/4/30
 * @Class: Subscribe
 * @Remark: 订阅方法的注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {
    /**
     * 指定线程
     */
    ThreadModel threadModel() default ThreadModel.POSTING;
}
