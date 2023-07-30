package com.github.jonasrutishauser.jakarta.enterprise.inject.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import com.github.jonasrutishauser.jakarta.enterprise.inject.ExtendedInstance;
import com.github.jonasrutishauser.jakarta.enterprise.inject.ExtendedInstance.Handle;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@EnableAutoWeld
@AddExtensions(ExtendedInstanceExtension.class)
@AddBeanClasses({ExtendedInstanceIT.ApplicationScopedBean.class, ExtendedInstanceIT.DependentBean.class})
class ExtendedInstanceIT {
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

    @Test
    void testInjection() {
        assertNotNull(namedInstance);
        assertNotNull(testee);
        assertNotNull(applicationScopedBean);
    }

    @Test
    void testDependentScope() {
        ApplicationScopedBean bean = applicationScopedBean.getInstance().get();

        try (Handle<Marker<?>> handle = namedInstance.getPseudoScopeClosingHandle()) {
            assertNotNull(handle.get());
        }
        assertEquals("bean destroyed", bean.getValue());

        bean.setValue("some other value");
        try (Handle<DependentBean> handle = testee.select(new TypeLiteral<Marker<?>>() {}, NamedLiteral.of("foo"))
                .getPseudoScopeClosingHandle()) {
            assertNotNull(handle.get());
        }
        assertEquals("bean destroyed", bean.getValue());

        bean.setValue("some other value");
        try (Handle<DependentBean> handle = testee.select(DependentBean.class).getPseudoScopeClosingHandle()) {
            assertNotNull(handle.get());
        }
        assertEquals("bean destroyed", bean.getValue());

        bean.setValue("some other value");
        try (Handle<Marker<String>> handle = stringMarker.getPseudoScopeClosingHandle()) {
            assertNotNull(handle.get());
        }
        assertEquals("bean destroyed", bean.getValue());

        bean.setValue("some other value");
        try (Handle<Marker<String>> handle = stringMarker.select(NamedLiteral.of("foo"))
                .getPseudoScopeClosingHandle()) {
            assertNotNull(handle.get());
        }
        assertEquals("bean destroyed", bean.getValue());
    }

    @Test
    void testApplicationScope() {
        try (Handle<ApplicationScopedBean> handle = applicationScopedBean.getPseudoScopeClosingHandle()) {
            handle.get().setValue("test value");
        }
        assertEquals("test value", applicationScopedBean.getInstance().get().getValue());

        try (Handle<ApplicationScopedBean> handle = testee.select(ApplicationScopedBean.class)
                .getPseudoScopeClosingHandle()) {
            handle.get().setValue("test 2 value");
        }
        assertEquals("test 2 value", applicationScopedBean.getInstance().get().getValue());
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
