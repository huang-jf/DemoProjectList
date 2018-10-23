package com.cy_life.aopdemo.jdk;

public class FontProviderFromDisk implements FontProvider {
    @Override
    public String getFont(String name) {
        return name;
    }

    @Override
    public void printName(String name) {
        System.out.println(name);
    }
}
