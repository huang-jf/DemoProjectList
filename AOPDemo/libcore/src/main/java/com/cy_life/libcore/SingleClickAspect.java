package com.cy_life.libcore;

import android.util.Log;
import android.view.View;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Calendar;

/**
 * 防止View被连续点击,间隔时间600ms
 */
@Aspect
public class SingleClickAspect {

    static int TIME_TAG = R.id.click_time;
    public static final int MIN_CLICK_DELAY_TIME = 1500;


    /**
     * 申明方法切入点
     */
    @Pointcut("execution(@com.cy_life.libaop.SingleClick * *(..))")
    public void methodAnnotated() {
    }


    /**
     * 对指定切入点进行方法替换
     * @param joinPoint
     * @throws Throwable
     */
    @Around("methodAnnotated()")
    public void aroundJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        View view = null;
        for (Object arg : joinPoint.getArgs())
            if (arg instanceof View) view = (View) arg;
        if (view != null) {
            Object tag = view.getTag(TIME_TAG);
            long lastClickTime = ((tag != null) ? (long) tag : 0);
            Log.d("SingleClickAspect", "lastClickTime:" + lastClickTime);
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {//过滤掉600毫秒内的连续点击
                view.setTag(TIME_TAG, currentTime);
                Log.d("SingleClickAspect", "currentTime:" + currentTime);
                joinPoint.proceed();//执行原方法
            }
        }
    }


    /**
     * 前置通知：目标方法执行之前执行以下方法体的内容
     * @param jp
     */
//    @Before("execution(* com.qcc.beans.aop.*.*(..))")
//    public void beforeMethod(JoinPoint jp) {
//        String methodName = jp.getSignature().getName();
//        System.out.println("【前置通知】the method 【" + methodName + "】 begins with " + Arrays.asList(jp.getArgs()));
//    }

    /**
     * 返回通知：目标方法正常执行完毕时执行以下代码
     *
     * @param jp
     * @param result
     */
//    @AfterReturning(value = "execution(* com.qcc.beans.aop.*.*(..))", returning = "result")
//    public void afterReturningMethod(JoinPoint jp, Object result) {
//        String methodName = jp.getSignature().getName();
//        System.out.println("【返回通知】the method 【" + methodName + "】 ends with 【" + result + "】");
//    }

    /**
     * 后置通知：目标方法执行之后执行以下方法体的内容，不管是否发生异常。
     *
     * @param jp
     */
//    @After("execution(* com.qcc.beans.aop.*.*(..))")
//    public void afterMethod(JoinPoint jp) {
//        System.out.println("【后置通知】this is a afterMethod advice...");
//    }

    /**
     * 异常通知：目标方法发生异常的时候执行以下代码
     */
//    @AfterThrowing(value = "execution(* com.qcc.beans.aop.*.*(..))", throwing = "e")
//    public void afterThorwingMethod(JoinPoint jp, NullPointerException e) {
//        String methodName = jp.getSignature().getName();
//        System.out.println("【异常通知】the method 【" + methodName + "】 occurs exception: " + e);
//    }

}
