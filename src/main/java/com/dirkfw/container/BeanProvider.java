package com.dirkfw.container;

import jakarta.servlet.ServletContext;

public abstract class BeanProvider {

    protected ServletContext servletContext;
    public BeanProvider(ServletContext context){
        servletContext=context;
    }
    public abstract <T> T getBean(Class<T> type);

    public abstract boolean containsBean(Class<?> type);

    public abstract void processOnScan(Class<?> type);

}
