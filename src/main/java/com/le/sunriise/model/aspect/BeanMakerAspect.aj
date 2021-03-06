package com.le.sunriise.model.aspect;

import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.log4j.Logger;

public aspect BeanMakerAspect {
    private static final Logger log = Logger.getLogger(BeanMakerAspect.class);
    
    declare parents: com.le.sunriise.model.bean.* implements BeanSupport;
    private transient PropertyChangeSupport BeanSupport.propertyChangeSupport;

    public void BeanSupport.addPropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeSupport == null)
            propertyChangeSupport = new PropertyChangeSupport(this);

        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void BeanSupport.removePropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeSupport != null)
            propertyChangeSupport.removePropertyChangeListener(listener);
    }

    pointcut beanPropertyChange(BeanSupport bean, Object newValue)
    : execution(void BeanSupport+.set*(*)) && args(newValue) && this(bean);

    void around(BeanSupport bean, Object newValue) : beanPropertyChange(bean, newValue) {
        if (bean.propertyChangeSupport == null) {
            proceed(bean, newValue);
        } else {
            String methodName = thisJoinPointStaticPart.getSignature().getName();
            String propertyName = Introspector.decapitalize(methodName.substring(3));
            if (log.isDebugEnabled()) {
                log.debug("> around, methodName=" + methodName + ", propertyName=" + propertyName);
            }
            proceed(bean, newValue);

            bean.propertyChangeSupport.firePropertyChange(propertyName, null, newValue);
        }
    }

}
