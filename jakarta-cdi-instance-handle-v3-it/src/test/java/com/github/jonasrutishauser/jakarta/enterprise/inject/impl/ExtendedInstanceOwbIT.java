package com.github.jonasrutishauser.jakarta.enterprise.inject.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;

import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.junit.jupiter.api.Test;

import com.github.jonasrutishauser.jakarta.enterprise.inject.ExtendedInstance.Handle;
import com.github.jonasrutishauser.jakarta.enterprise.inject.impl.TestBean.ApplicationScopedBean;
import com.github.jonasrutishauser.jakarta.enterprise.inject.impl.TestBean.DependentBean;
import com.github.jonasrutishauser.jakarta.enterprise.inject.impl.TestBean.Marker;

@RunWithApplicationComposer
@Classes(cdi = true, value = {TestBean.class, ApplicationScopedBean.class, DependentBean.class})
class ExtendedInstanceOwbIT {
    @Inject
    TestBean testBean;

    @Test
    void testInjection() {
        assertNotNull(testBean.getNamedInstance());
        assertNotNull(testBean.getTestee());
        assertNotNull(testBean.getApplicationScopedBean());
    }

    @Test
    void testDependentScope() {
        ApplicationScopedBean bean = testBean.getApplicationScopedBean().getInstance().get();

        try (Handle<Marker<?>> handle = testBean.getNamedInstance().getPseudoScopeClosingHandle()) {
            assertNotNull(handle.get());
        }
        assertEquals("bean destroyed", bean.getValue());

        bean.setValue("some other value");
        try (Handle<DependentBean> handle = testBean.getTestee().select(DependentBean.class)
                .getPseudoScopeClosingHandle()) {
            assertNotNull(handle.get());
        }
        assertEquals("bean destroyed", bean.getValue());

        bean.setValue("some other value");
        try (Handle<Marker<String>> handle = testBean.getStringMarker().getPseudoScopeClosingHandle()) {
            assertNotNull(handle.get());
        }
        assertEquals("bean destroyed", bean.getValue());

        bean.setValue("some other value");
        try (Handle<Marker<String>> handle = testBean.getStringMarker().select(NamedLiteral.of("foo"))
                .getPseudoScopeClosingHandle()) {
            assertNotNull(handle.get());
        }
        assertEquals("bean destroyed", bean.getValue());
    }

    @Test
    void testApplicationScope() {
        try (Handle<ApplicationScopedBean> handle = testBean.getApplicationScopedBean().getPseudoScopeClosingHandle()) {
            handle.get().setValue("test value");
        }
        assertEquals("test value", testBean.getApplicationScopedBean().getInstance().get().getValue());

        try (Handle<ApplicationScopedBean> handle = testBean.getTestee().select(ApplicationScopedBean.class)
                .getPseudoScopeClosingHandle()) {
            handle.get().setValue("test 2 value");
        }
        assertEquals("test 2 value", testBean.getApplicationScopedBean().getInstance().get().getValue());
    }
}
