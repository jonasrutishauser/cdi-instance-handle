package com.github.jonasrutishauser.javax.enterprise.inject;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.TypeLiteral;

public interface ExtendedInstance<T> {
    /**
     * <p>
     * Obtains a child <code>Instance</code> for the given additional required
     * qualifiers.
     * </p>
     * 
     * @param qualifiers
     *            the additional required qualifiers
     * @return the child <code>Instance</code>
     * @throws IllegalArgumentException
     *             if passed two instances of the same non repeating qualifier type,
     *             or an instance of an annotation that is not a qualifier type
     * @throws IllegalStateException
     *             if the container is already shutdown
     */
    ExtendedInstance<T> select(Annotation... qualifiers);

    /**
     * <p>
     * Obtains a child <code>Instance</code> for the given required type and
     * additional required qualifiers.
     * </p>
     * 
     * @param <U>
     *            the required type
     * @param subtype
     *            a {@link java.lang.Class} representing the required type
     * @param qualifiers
     *            the additional required qualifiers
     * @return the child <code>Instance</code>
     * @throws IllegalArgumentException
     *             if passed two instances of the same non repeating qualifier type,
     *             or an instance of an annotation that is not a qualifier type
     * @throws IllegalStateException
     *             if the container is already shutdown
     */
    <U extends T> ExtendedInstance<U> select(Class<U> subtype, Annotation... qualifiers);

    /**
     * <p>
     * Obtains a child <code>Instance</code> for the given required type and additional required qualifiers.
     * </p>
     * 
     * @param <U> the required type
     * @param subtype a {@link TypeLiteral} representing the required type
     * @param qualifiers the additional required qualifiers
     * @return the child <code>Instance</code>
     * @throws IllegalArgumentException if passed two instances of the same non repeating qualifier type, or an instance of an annotation that
     *         is not a qualifier type
     * @throws IllegalStateException if the container is already shutdown
     */
    <U extends T> ExtendedInstance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers);

    Handle<T> getPseudoScopeClosingHandle();

    Instance<T> getInstance();

    /**
     * This interface represents a contextual reference handle.
     * <p>
     * Allows to inspect the metadata of the relevant bean before resolving its
     * contextual reference and also to destroy the underlying contextual instance.
     * </p>
     *
     * @param <T>
     *            the required bean type
     */
    interface Handle<T> extends AutoCloseable {
        /**
         * The contextual reference is obtained lazily, i.e. when first needed.
         *
         * @return the contextual reference
         * @see Instance#get()
         * @throws IllegalStateException
         *             If the producing {@link Instance} does not exist
         * @throws IllegalStateException
         *             If invoked on {@link Handle} that previously successfully
         *             destroyed its underlying contextual reference
         */
        T get();

        /**
         * @return the bean metadata
         */
        Bean<T> getBean();

        /**
         * Destroy the contextual instance. It's a no-op if:
         * <ul>
         * <li>called multiple times</li>
         * <li>if the producing {@link Instance} does not exist</li>
         * <li>if the handle does not hold a contextual reference, i.e. {@link #get()}
         * was never called</li>
         * </ul>
         *
         * @see Instance#destroy(Object)
         */
        void destroy();

        /**
         * Delegates to {@link #destroy()}, if the contextual reference is not a normal
         * scoped bean.
         */
        @Override
        void close();
    }
}
