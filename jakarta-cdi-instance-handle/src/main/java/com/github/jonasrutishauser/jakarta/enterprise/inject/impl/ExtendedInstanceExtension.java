package com.github.jonasrutishauser.jakarta.enterprise.inject.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.github.jonasrutishauser.jakarta.enterprise.inject.ExtendedInstance;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.ProcessProducer;

public class ExtendedInstanceExtension implements Extension {
    private Set<Annotation> qualifiers = new HashSet<>();

    @SuppressWarnings("rawtypes")
    private BeanAttributes<ExtendedInstance> producerAttributes;
    private Set<InjectionPoint> producerInjectionPoints;
    @SuppressWarnings("rawtypes")
    private Function<CreationalContext<ExtendedInstance>, ExtendedInstance> producerFunction;

    void addProducer(@Observes BeforeBeanDiscovery event) {
        event.addAnnotatedType(ExtendedInstanceProducer.class, ExtendedInstanceProducer.class.getName())
                .add(Dependent.Literal.INSTANCE);
    }

    void addQualifiers(@Observes ProcessInjectionPoint<?, ExtendedInstance<?>> event) {
        qualifiers.addAll(event.getInjectionPoint().getQualifiers());
    }

    void addAllQualifiers(@SuppressWarnings("rawtypes") @Observes ProcessBeanAttributes<ExtendedInstance> event) {
        producerAttributes = event.getBeanAttributes();
        event.configureBeanAttributes().qualifiers(new HashSet<>(qualifiers));
        qualifiers.clear();
    }

    void setExtendedInstanceProducer(@SuppressWarnings("rawtypes") @Observes ProcessProducer<?, ExtendedInstance> event,
            BeanManager beanManager) {
        producerInjectionPoints = event.getProducer().getInjectionPoints();
        producerFunction = creationalContext -> {
            InjectionPoint targetInjectionPoint = (InjectionPoint) beanManager.getInjectableReference(
                    producerInjectionPoints.stream().filter(ip -> InjectionPoint.class.equals(ip.getType())).findAny()
                            .orElseThrow(IllegalStateException::new),
                    creationalContext);
            InjectionPoint instanceInjectionPoint = producerInjectionPoints.stream()
                    .filter(ip -> ip.getType() instanceof ParameterizedType).findAny()
                    .orElseThrow(IllegalStateException::new);
            Instance<?> instance = (Instance<?>) beanManager.getInjectableReference(new InjectionPoint() {
                @Override
                public Type getType() {
                    if (targetInjectionPoint.getType() instanceof Class) {
                        return new InstanceType(Object.class);
                    }
                    return new InstanceType(
                            ((ParameterizedType) targetInjectionPoint.getType()).getActualTypeArguments()[0]);
                }

                @Override
                public Set<Annotation> getQualifiers() {
                    return targetInjectionPoint.getQualifiers();
                }

                @Override
                public Bean<?> getBean() {
                    return instanceInjectionPoint.getBean();
                }

                @Override
                public Member getMember() {
                    return instanceInjectionPoint.getMember();
                }

                @Override
                public Annotated getAnnotated() {
                    return instanceInjectionPoint.getAnnotated();
                }

                @Override
                public boolean isDelegate() {
                    return false;
                }

                @Override
                public boolean isTransient() {
                    return false;
                }
            }, creationalContext);
            return ExtendedInstanceProducer.create(beanManager, targetInjectionPoint, instance);
        };
        event.configureProducer().produceWith(producerFunction);
    }

    void addAdditionalProducer(@Observes AfterBeanDiscovery event) {
        if (!qualifiers.isEmpty()) {
            // Add additional producer for missed qualifiers with OpenWebBeans
            event.addBean().read(producerAttributes).qualifiers(qualifiers).injectionPoints(producerInjectionPoints)
                    .beanClass(ExtendedInstanceProducer.class).alternative(true).createWith(producerFunction);
        }
    }
}
