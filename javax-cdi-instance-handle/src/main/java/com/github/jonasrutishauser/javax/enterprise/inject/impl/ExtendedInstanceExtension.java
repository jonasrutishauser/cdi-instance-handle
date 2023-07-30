package com.github.jonasrutishauser.javax.enterprise.inject.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.util.AnnotationLiteral;

import com.github.jonasrutishauser.javax.enterprise.inject.ExtendedInstance;

public class ExtendedInstanceExtension implements Extension {
    private final Set<Annotation> qualifiers = new HashSet<>();

    void addProducer(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        AnnotatedType<ExtendedInstanceProducer> annotatedType = beanManager
                .createAnnotatedType(ExtendedInstanceProducer.class);
        event.addAnnotatedType(new AnnotatedType<ExtendedInstanceProducer>() {
            @Override
            public Type getBaseType() {
                return annotatedType.getBaseType();
            }

            @Override
            public Set<Type> getTypeClosure() {
                return annotatedType.getTypeClosure();
            }

            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                if (Dependent.class.equals(annotationType)) {
                    return (T) DependentLiteral.INSTANCE;
                }
                return annotatedType.getAnnotation(annotationType);
            }

            @Override
            public Set<Annotation> getAnnotations() {
                Set<Annotation> annotations = new HashSet<>(annotatedType.getAnnotations());
                annotations.add(getAnnotation(Dependent.class));
                return annotations;
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                return Dependent.class.equals(annotationType) || annotatedType.isAnnotationPresent(annotationType);
            }

            @Override
            public Class<ExtendedInstanceProducer> getJavaClass() {
                return annotatedType.getJavaClass();
            }

            @Override
            public Set<AnnotatedConstructor<ExtendedInstanceProducer>> getConstructors() {
                return annotatedType.getConstructors();
            }

            @Override
            public Set<AnnotatedMethod<? super ExtendedInstanceProducer>> getMethods() {
                return annotatedType.getMethods();
            }

            @Override
            public Set<AnnotatedField<? super ExtendedInstanceProducer>> getFields() {
                return annotatedType.getFields();
            }
        }, ExtendedInstanceProducer.class.getName());
    }

    void addQualifiers(@Observes ProcessInjectionPoint<?, ExtendedInstance<?>> event) {
        qualifiers.addAll(event.getInjectionPoint().getQualifiers());
    }

    @SuppressWarnings("rawtypes")
    void addAllQualifiers(@Observes ProcessBeanAttributes<ExtendedInstance> event) {
        if (!qualifiers.isEmpty()) {
            BeanAttributes<ExtendedInstance> beanAttributes = event.getBeanAttributes();
            event.setBeanAttributes(new BeanAttributes<ExtendedInstance>() {
                @Override
                public boolean isAlternative() {
                    return beanAttributes.isAlternative();
                }
    
                @Override
                public Set<Type> getTypes() {
                    return beanAttributes.getTypes();
                }
    
                @Override
                public Set<Class<? extends Annotation>> getStereotypes() {
                    return beanAttributes.getStereotypes();
                }
    
                @Override
                public Class<? extends Annotation> getScope() {
                    return beanAttributes.getScope();
                }
    
                @Override
                public Set<Annotation> getQualifiers() {
                    return qualifiers;
                }
    
                @Override
                public String getName() {
                    return beanAttributes.getName();
                }
            });
        }
    }

    @SuppressWarnings("rawtypes")
    void setExtendedInstanceProducer(@Observes ProcessProducer<?, ExtendedInstance> event, BeanManager beanManager) {
        Producer<ExtendedInstance> extendedInstanceProducer = event.getProducer();
        event.setProducer(new Producer<ExtendedInstance>() {
            @Override
            public ExtendedInstance<?> produce(CreationalContext<ExtendedInstance> ctx) {
                InjectionPoint targetInjectionPoint = (InjectionPoint) beanManager
                        .getInjectableReference(extendedInstanceProducer.getInjectionPoints().stream()
                                .filter(ip -> InjectionPoint.class.equals(ip.getType())).findAny()
                                .orElseThrow(IllegalStateException::new), ctx);
                InjectionPoint instanceInjectionPoint = extendedInstanceProducer.getInjectionPoints().stream()
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
                }, ctx);
                return ExtendedInstanceProducer.create(beanManager, targetInjectionPoint, instance);
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return extendedInstanceProducer.getInjectionPoints();
            }

            @Override
            public void dispose(ExtendedInstance instance) {
                extendedInstanceProducer.dispose(instance);
            }
        });
    }

    private static class DependentLiteral extends AnnotationLiteral<Dependent> implements Dependent {
        private static final Dependent INSTANCE = new DependentLiteral();
    }
}
