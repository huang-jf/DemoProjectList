package com.example.hjf.hookdemo;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HookFuncByDynamicProxy {


    /**
     * hook 拦截应用内通知
     * sService 是个静态成员变量，而且只会初始化一次。只要把 sService 替换成自定义的不就行了
     *
     * @param context
     */
    public static void hookNotificationManager(Context context) {
        try {

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // 得到系统的 sService
            Method getService = NotificationManager.class.getDeclaredMethod("getService");
            getService.setAccessible(true);
            final Object sService = getService.invoke(notificationManager);

            Class iNotifyManager = Class.forName("android.app.INotificationManager");
            // 动态代理 INotificationManager
            Object proxyNotifyMgr = Proxy.newProxyInstance(context.getClass().getClassLoader(), new Class[]{iNotifyManager}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (args != null && args.length > 0) {
                        for (Object arg : args) {
                        }
                    }
                    // 操作交由 sService 处理，不拦截通知
//                    return method.invoke(sService, args);
                    // 拦截通知，什么都不做
                    return null;
                }
            });
            // 替换 mService
            Field mServiceField = NotificationManager.class.getField("mService");
            mServiceField.setAccessible(true);
            mServiceField.set(notificationManager, proxyNotifyMgr);

        } catch (Exception e) {
        }
    }

    /**
     * 拦截Activity启动时
     * http://weishu.me/2016/01/28/understand-plugin-framework-proxy-hook/
     */
    public static void hookActivityOnStart() {
        try {

            // 1. 获取当前的 ActivityThread 对象
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            //  静态全局方法，传入null
            currentActivityThreadMethod.setAccessible(true);
            Object activityThread = currentActivityThreadMethod.invoke(null);

            // 2. 获取 Instrumentation 对象
            Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation instrumentation = (Instrumentation) mInstrumentationField.get(activityThread);

            // 因为 jdk 动态代理只支持 Interface，而 Instrumentation 是 Class，需要手动写静态代理，然后替换整个对象
            MyInstrumentation myInstrumentation = new MyInstrumentation(instrumentation);

            // 替换
            mInstrumentationField.set(activityThread, myInstrumentation);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试修改情况
     * 因为国内Rom定制原因，可能不能成功hook
     * 验证逻辑就行，不做适配
     */
    public static void testHookActivityOnStart(){
        try {

            // 1. 获取当前的 ActivityThread 对象
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            //  静态全局方法，传入null
            currentActivityThreadMethod.setAccessible(true);
            Object activityThread = currentActivityThreadMethod.invoke(null);

            // 2. 获取 Instrumentation 对象
            Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Object o = mInstrumentationField.get(activityThread);
            if (o instanceof MyInstrumentation){
                Log.d("hjf", "修改成功");

            }
        }catch (Exception e){

        }
    }


    /**
     * 因为 jdk 动态代理只支持 Interface，而 Instrumentation 是 Class，需要手动写静态代理，然后替换整个对象
     */
    private static class MyInstrumentation extends Instrumentation {
        private Instrumentation base;

        public MyInstrumentation(Instrumentation base) {
            this.base = base;
        }

        public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {
            try {


                Log.d("hjf", "\n执行了startActivity, 参数如下: \n" + "who = [" + who + "], " +
                        "\ncontextThread = [" + contextThread + "], \ntoken = [" + token + "], " +
                        "\ntarget = [" + target + "], \nintent = [" + intent +
                        "], \nrequestCode = [" + requestCode + "], \noptions = [" + options + "]");

                Method execStartActivityMethod = Instrumentation.class.getDeclaredMethod("execStartActivity",
                        Context.class, IBinder.class, Activity.class, Intent.class, Integer.class, Bundle.class);
                execStartActivityMethod.setAccessible(true);
                return (ActivityResult) execStartActivityMethod.invoke(base, who, contextThread, token, target, intent, requestCode, options);
            } catch (Exception e) {
                // 某该死的rom修改了  需要手动适配
            }
            return null;
        }
    }
}
