package com.quyunshuo.evnetplus;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: QuYunShuo
 * @Time: 2020/4/30
 * @Class: EventPlus
 * @Remark:
 */
public class EventPlus {

    /**
     * 单例
     */
    private static volatile EventPlus defaultInstance;

    /**
     * 容器 装载了所有订阅者及订阅方法
     */
    private Map<Object, List<MethodManager>> map;

    /**
     * 线程服务对象
     */
    private ExecutorService executorService;

    /**
     * 切换到主线程的Handler
     */
    private Handler handler;

    /**
     * 私有构造函数
     */
    private EventPlus() {
        map = new HashMap<>();
        // new 一个线程池
        executorService = Executors.newCachedThreadPool();
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 懒汉式-双重校验锁单例
     *
     * @return 单例
     */
    public static EventPlus getDefault() {
        EventPlus instance = defaultInstance;
        if (instance == null) {
            synchronized (EventPlus.class) {
                instance = EventPlus.defaultInstance;
                if (instance == null) {
                    instance = EventPlus.defaultInstance = new EventPlus();
                }
            }
        }
        return instance;
    }

    /**
     * 注册方法
     * 反射收集订阅者中所有的订阅方法
     *
     * @param subscriber 订阅者
     */
    public void register(Object subscriber) {
        // 先从容器中查询有无此订阅者
        List<MethodManager> methods = map.get(subscriber);
        // 如果没有  就进行下一步操作
        if (methods == null) {
            // 查找该订阅者的订阅方法
            List<MethodManager> methodList = findMethod(subscriber);
            // 将订阅者及订阅方法put进容器内
            map.put(subscriber, methodList);
        }
    }

    /**
     * 取消注册
     *
     * @param object 订阅者
     */
    public void unregister(Object object) {
        map.remove(object);
    }

    /**
     * 连接通道 发送事件
     *
     * @param event 事件
     */
    public void post(Object event) {
        // 得到容器中所有的订阅者
        Set<Object> keySet = map.keySet();
        // 遍历所有的订阅者
        for (Object o : keySet) {
            // 获取订阅者对应的订阅方法
            List<MethodManager> methods = map.get(o);
            if (methods != null) {
                for (MethodManager method : methods) {
                    // 匹配订阅方法
                    // 得到这个方法的接收参数类型
                    Class<?> type = method.getType();
                    // 判断类型是否相同
                    if (type.isAssignableFrom(event.getClass())) {
                        // 判断订阅方法指定的线程模式
                        judgmentThread(method, event, o);
                    }
                }
            }
        }
    }

    /**
     * 判断线程并执行订阅方法
     *
     * @param method     订阅方法的封装类
     * @param event      事件
     * @param subscriber 订阅者
     */
    private void judgmentThread(final MethodManager method, final Object event, final Object subscriber) {
        switch (method.getThreadModel()) {
            case POSTING:
                invoke(method.getMethod(), event, subscriber);
                break;
            case MAIN:
                // 判断当前的线程
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    invoke(method.getMethod(), event, subscriber);
                } else {
                    // 如果不是主线程就需要切换线程
                    handler.post(() -> invoke(method.getMethod(), event, subscriber));
                }
                break;
            case BACKGROUND:
                // 判断当前的线程
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    // 如果是主线程就需要切换线程
                    executorService.execute(() -> invoke(method.getMethod(), event, subscriber));
                } else {
                    invoke(method.getMethod(), event, subscriber);
                }
                break;
        }
    }

    /**
     * 通过反射查找订阅者的订阅方法
     *
     * @param subscriber 订阅者
     * @return 订阅者的订阅方法
     */
    private List<MethodManager> findMethod(Object subscriber) {
        List<MethodManager> methods = new ArrayList<>();
        // 获取类对象
        Class<?> aClass = subscriber.getClass();
        // 获取类对象里面所有的方法
        Method[] declaredMethods = aClass.getDeclaredMethods();
        // 遍历所有的方法
        for (Method method : declaredMethods) {
            // 条件1: 是否带注解
            Subscribe annotation = method.getAnnotation(Subscribe.class);
            if (annotation == null) {
                continue;
            }
            // 条件2: 有且只有一个参数
            // 获取方法的参数列表
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                continue;
            }
            MethodManager methodManager = new MethodManager(method, parameterTypes[0], annotation.threadModel());
            methods.add(methodManager);
        }
        return methods;
    }

    /**
     * 执行订阅方法
     *
     * @param method 订阅方法本身
     * @param event  发布者发布的事件
     * @param object 事件的订阅者
     */
    private void invoke(Method method, Object event, Object object) {
        try {
            method.invoke(object, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}