package com.jzby.jzbysounderclient.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by gordan on 2018/3/9.
 */

public class ElementBean implements Serializable
{
    private static final long serialVersionUID = -7146166023408102548L;
    public String action;
    public String type;
    public String slots;
    public String attrType;
    public String attrValue;
    public List<ElementBean> elements;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAttrType() {
        return attrType;
    }

    public void setAttrType(String attrType) {
        this.attrType = attrType;
    }

    public String getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;
    }

    public List<ElementBean> getElements() {
        return elements;
    }

    public void setElements(List<ElementBean> elements) {
        this.elements = elements;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlots() {
        return slots;
    }

    public void setSlots(String slots) {
        this.slots = slots;
    }
}
