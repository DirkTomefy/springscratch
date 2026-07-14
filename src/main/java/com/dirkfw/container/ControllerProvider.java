package com.dirkfw.container;


import jakarta.servlet.ServletContext;

public abstract class ControllerProvider {

    public ControllerProvider(ServletContext context,Object externalContext){
       
    }
    public abstract <T> T getBean(Class<T> type);

    public abstract boolean containsBean(Class<?> type);

    public abstract void processOnScan(Class<?> type);

}