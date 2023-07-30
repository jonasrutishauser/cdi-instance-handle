package com.github.jonasrutishauser.jakarta.enterprise.inject.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import jakarta.enterprise.inject.Instance;

class InstanceType implements ParameterizedType {

    private final Type[] types;

    public InstanceType(Type typeArgument) {
        this.types = new Type[] {typeArgument};
    }

    @Override
    public Type[] getActualTypeArguments() {
        return types;
    }

    @Override
    public Type getRawType() {
        return Instance.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(types) ^ 0 ^ Instance.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ParameterizedType) {
            ParameterizedType other = (ParameterizedType) obj;
            return other.getOwnerType() == null && Instance.class.equals(other.getRawType())
                    && Arrays.equals(types, other.getActualTypeArguments());
        }
        return false;
    }

}
