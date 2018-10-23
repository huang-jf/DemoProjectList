package com.cy_life.aopdemo.jdk;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * JDK Dynamic Proxy动态代理
 */
public class CachedProviderHandler implements InvocationHandler {
    private Map<String, Object> cached = new HashMap<String, Object>();
    private Object target;

    public CachedProviderHandler(Object target) {
        this.target = target;
    }

    /**
     * invoke方法可以处理target的所有方法，这里用if判断只处理了getXXX()方法，增加了缓存功能。
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d("DynamicProxy", "使用动态代理了！");
        Class<?>[] types = method.getParameterTypes();
        if (method.getName().matches("get.+") && types.length == 1 && types[0] == String.class) {
            String key = (String) args[0];
            Object value = cached.get(key);
            if (value == null) {
                Log.d("DynamicProxy", "getXXX()方法，新建对象放放入缓存");
                value = method.invoke(target, args);
                cached.put(key, value);
            }
            else {
                Log.d("DynamicProxy", "getXXX()方法，从缓存中获取对象");
            }
            return value;
        }
        return method.invoke(target, args);
    }
}
