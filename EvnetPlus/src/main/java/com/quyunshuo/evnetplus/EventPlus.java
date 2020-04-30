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
        List<MethodManager> methods = map.get(subscriber);
        if (methods == null) {
            List<MethodManager> methodList = findMethod(subscriber);
            map.put(subscriber, methodList);
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
     * 连接通道
     * 发送事件
     */
    public void post(final Object event) {
        // 得到订阅者集合中的订阅者的集合
        Set<Object> keySet = map.keySet();
        // 遍历所有的订阅者
        for (final Object o : keySet) {
            // 获取订阅者对应的订阅方法
            List<MethodManager> methods = map.get(o);
            for (final MethodManager method : methods) {
                // 匹配订阅方法
                // 得到这个方法的接收参数类型
                Class<?> type = method.getType();
                if (type.isAssignableFrom(event.getClass())) {
                    // 判断订阅方法指定的线程模式
                    switch (method.getThreadModel()) {
                        case POSTING:
                            invoke(method.getMethod(), event, o);
                            break;
                        case MAIN:
                            // 判断当前的线程
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                invoke(method.getMethod(), event, o);
                            } else {
                                // 如果不是主线程就需要切换线程
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(method.getMethod(), event, o);
                                    }
                                });
                            }
                            break;
                        case BACKGROUND:
                            // 判断当前的线程
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                // 如果是主线程就需要切换线程
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(method.getMethod(), event, o);
                                    }
                                });
                            } else {
                                invoke(method.getMethod(), event, o);
                            }
                            break;
                    }
                }
            }
        }
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

    /**
     * 取消注册
     */
    public void unregister(Object object) {
        map.remove(object);
    }
}