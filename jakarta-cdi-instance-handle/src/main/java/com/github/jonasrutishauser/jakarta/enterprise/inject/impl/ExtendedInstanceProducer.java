package com.github.jonasrutishauser.jakarta.enterprise.inject.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.github.jonasrutishauser.jakarta.enterprise.inject.ExtendedInstance;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;

class ExtendedInstanceProducer {

    private static final boolean CDI_4 = isCdiVersionAtLeast4();

    private static boolean isCdiVersionAtLeast4() {
        try {
            Instance.class.getMethod("getHandle");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Produces
    static <T> ExtendedInstance<T> createExtendedInstance(InjectionPoint injectionPoint, Instance<Object> instance) {
        throw new IllegalStateException("should not be called");
    }
    
    static <T> ExtendedInstance<T> create(BeanManager beanManager, InjectionPoint injectionPoint, Instance<T> instance) {
        if (CDI_4) {
            return new CDI4ExtendedInstance<>(beanManager, instance);
        } else {
            Type targetType = Object.class;
            if (injectionPoint.getType() instanceof ParameterizedType) {
                targetType = ((ParameterizedType) injectionPoint.getType()).getActualTypeArguments()[0];
            }
            return new CDI3ExtendedInstance<>(beanManager, instance, targetType, injectionPoint.getQualifiers());
        }
    }

    private ExtendedInstanceProducer() {
    }

}
