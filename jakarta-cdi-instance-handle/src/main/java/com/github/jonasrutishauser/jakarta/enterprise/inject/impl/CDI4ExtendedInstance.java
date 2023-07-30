package com.github.jonasrutishauser.jakarta.enterprise.inject.impl;

import java.lang.annotation.Annotation;

import com.github.jonasrutishauser.jakarta.enterprise.inject.ExtendedInstance;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.TypeLiteral;

class CDI4ExtendedInstance<T> implements ExtendedInstance<T> {
    
    private final BeanManager beanManager;
    private final Instance<T> instance;

    public CDI4ExtendedInstance(BeanManager beanManager, Instance<T> instance) {
        this.beanManager = beanManager;
        this.instance = instance;
    }

    public ExtendedInstance<T> select(Annotation... qualifiers) {
        return new CDI4ExtendedInstance<>(beanManager, instance.select(qualifiers));
    }

    public <U extends T> ExtendedInstance<U> select(Class<U> subtype, Annotation... qualifiers) {
        return new CDI4ExtendedInstance<>(beanManager, instance.select(subtype, qualifiers));
    }

    public <U extends T> ExtendedInstance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return new CDI4ExtendedInstance<>(beanManager, instance.select(subtype, qualifiers));
    }

    @Override
    public Instance<T> getInstance() {
        return instance;
    }

    @Override
    public Handle<T> getPseudoScopeClosingHandle() {
        return new HandleImpl<>(beanManager, instance.getHandle());
    }

    private static class HandleImpl<T> implements Handle<T> {
        private final BeanManager beanManager;
        private final jakarta.enterprise.inject.Instance.Handle<T> delegate;

        public HandleImpl(BeanManager beanManager, jakarta.enterprise.inject.Instance.Handle<T> delegate) {
            this.beanManager = beanManager;
            this.delegate = delegate;
        }

        public T get() {
            return delegate.get();
        }

        public Bean<T> getBean() {
            return delegate.getBean();
        }

        public void destroy() {
            delegate.destroy();
        }

        public void close() {
            if (!beanManager.isNormalScope(delegate.getBean().getScope())) {
                delegate.destroy();
            }
        }
    }
}
