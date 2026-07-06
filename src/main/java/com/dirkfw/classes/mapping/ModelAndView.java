package com.dirkfw.classes.mapping;

import java.util.HashMap;

public class ModelAndView {
    String viewName;
    HashMap<String,Object> attributes;

    public ModelAndView(String viewName){
        this.viewName = viewName;
        this.attributes = new HashMap<>();
    }

    public void setAttribute(String name, Object value){
        this.attributes.put(name, value);
    }
    
    public String getViewName() {
        return viewName;
    }
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
    public HashMap<String, Object> getAttributes() {
        return attributes;
    }
    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;
    }
}
