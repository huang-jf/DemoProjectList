package com.cy_life.aopdemo.jdk;

import java.lang.reflect.Proxy;

public class ProviderFactory {

    public static FontProvider getFontProvider() {
        Class<FontProvider> targetClass = FontProvider.class;
        return (FontProvider) Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                new Class[]{targetClass},
                new CachedProviderHandler(new FontProviderFromDisk()));
    }
}
