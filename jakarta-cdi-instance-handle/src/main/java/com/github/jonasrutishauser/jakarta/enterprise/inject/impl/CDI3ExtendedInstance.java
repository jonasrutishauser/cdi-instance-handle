package com.github.jonasrutishauser.jakarta.enterprise.inject.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import com.github.jonasrutishauser.jakarta.enterprise.inject.ExtendedInstance;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.TypeLiteral;

class CDI3ExtendedInstance<T> implements ExtendedInstance<T> {
    
    private final BeanManager beanManager;
    private final Instance<T> instance;
    private final Type targetType;
    private final Set<Annotation> qualifiers;

    public CDI3ExtendedInstance(BeanManager beanManager, Instance<T> instance, Type targetType, Set<Annotation> qualifiers) {
        this.beanManager = beanManager;
        this.targetType = targetType;
        this.instance = instance;
        this.qualifiers = qualifiers;
    }

    public ExtendedInstance<T> select(Annotation... qualifiers) {
        return new CDI3ExtendedInstance<>(beanManager, instance.select(qualifiers), targetType, addedQualifiers(qualifiers));
    }

    public <U extends T> ExtendedInstance<U> select(Class<U> subtype, Annotation... qualifiers) {
        return new CDI3ExtendedInstance<>(beanManager, instance.select(subtype, qualifiers), subtype, addedQualifiers(qualifiers));
    }

    public <U extends T> ExtendedInstance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return new CDI3ExtendedInstance<>(beanManager, instance.select(subtype, qualifiers), subtype.getType(), addedQualifiers(qualifiers));
    }

    @Override
    public Instance<T> getInstance() {
        return instance;
    }

    @Override
    public Handle<T> getPseudoScopeClosingHandle() {
        return new HandleImpl();
    }

    private Set<Annotation> addedQualifiers(Annotation... qualifiers) {
        Set<Annotation> newQualifiers = new HashSet<>(this.qualifiers);
        for (Annotation qualifier : qualifiers) {
            newQualifiers.add(qualifier);
        }
        return newQualifiers;
    }

    private class HandleImpl implements Handle<T> {
        private T bean;

        public T get() {
            if (bean == null) {
                bean = instance.get();
            }
            return bean;
        }

        public Bean<T> getBean() {
            return (Bean<T>) beanManager.resolve(beanManager.getBeans(targetType, qualifiers.toArray(Annotation[]::new)));
        }

        public void destroy() {
            if (bean != null) {
                instance.destroy(bean);
                bean = null;
            }
        }

        public void close() {
            if (!beanManager.isNormalScope(getBean().getScope())) {
                destroy();
            }
        }
    }
}
