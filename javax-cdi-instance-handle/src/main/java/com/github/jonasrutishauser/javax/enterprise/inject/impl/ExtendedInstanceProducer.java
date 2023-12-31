package com.github.jonasrutishauser.javax.enterprise.inject.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import com.github.jonasrutishauser.javax.enterprise.inject.ExtendedInstance;

class ExtendedInstanceProducer {

    @Produces
    static <T> ExtendedInstance<T> createExtendedInstance(InjectionPoint injectionPoint, Instance<Object> instance) {
        throw new IllegalStateException("should not be called");
    }
    
    static <T> ExtendedInstance<T> create(BeanManager beanManager, InjectionPoint injectionPoint, Instance<T> instance) {
        Type targetType = Object.class;
        if (injectionPoint.getType() instanceof ParameterizedType) {
            targetType = ((ParameterizedType) injectionPoint.getType()).getActualTypeArguments()[0];
        }
        return new CDIExtendedInstance<>(beanManager, instance, targetType, injectionPoint.getQualifiers());
    }

    private ExtendedInstanceProducer() {
    }

}
