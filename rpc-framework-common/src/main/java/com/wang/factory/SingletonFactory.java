package com.wang.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 懒汉式获取单例对象的工厂类
 */
public class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP = new HashMap<>(); //禁止指令重排

    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> c) {
        String key = c.toString();
        Object instance = OBJECT_MAP.get(key);
        if (instance == null) { //双重校验
            synchronized (c) {
                if (instance == null) {
                    try {
                        instance = c.newInstance();
                        OBJECT_MAP.put(key, instance);
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }
        }
        return c.cast(instance); //instance强制转型为c
    }
}
