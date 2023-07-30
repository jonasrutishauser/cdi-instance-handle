package com.github.jonasrutishauser.javax.enterprise.inject.impl;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;

import com.github.jonasrutishauser.javax.enterprise.inject.ExtendedInstance;

@Dependent
class TestBean {
    @Inject
    @Named("foo")
    private ExtendedInstance<Marker<?>> namedInstance;
    @Any
    @Inject
    @SuppressWarnings("rawtypes")
    private ExtendedInstance testee;
    @Any
    @Inject
    private ExtendedInstance<Marker<String>> stringMarker;
    @Any
    @Inject
    private ExtendedInstance<ApplicationScopedBean> applicationScopedBean;

    public ExtendedInstance<Marker<?>> getNamedInstance() {
        return namedInstance;
    }

    public ExtendedInstance getTestee() {
        return testee;
    }

    public ExtendedInstance<Marker<String>> getStringMarker() {
        return stringMarker;
    }

    public ExtendedInstance<ApplicationScopedBean> getApplicationScopedBean() {
        return applicationScopedBean;
    }

    interface Marker<T> {}

    @ApplicationScoped
    static class ApplicationScopedBean implements Marker<Object> {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Dependent
    @Named("foo")
    static class DependentBean implements Marker<String> {
        private final ApplicationScopedBean bean;

        @Inject
        private DependentBean(ApplicationScopedBean bean) {
            this.bean = bean;
        }

        @PreDestroy
        void destroy() {
            bean.setValue("bean destroyed");
        }
    }
}
