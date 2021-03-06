/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.util.reflection.instantiation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;

import org.jboss.weld.logging.ReflectionLogger;
import org.jboss.weld.security.GetDeclaredFieldAction;
import org.jboss.weld.security.GetDeclaredMethodAction;
import org.jboss.weld.security.SetAccessibleAction;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;


/**
 * An instantiator for sun.misc.Unsafe
 *
 * @author Nicklas Karlsson
 * @author Ales Justin
 */
public class UnsafeInstantiator implements Instantiator {
    private static final String REFLECTION_CLASS_NAME = "sun.misc.Unsafe";

    private Method allocateInstanceMethod = null;
    private Object unsafeInstance = null;

    @SuppressWarnings(value = "DE_MIGHT_IGNORE", justification = "The exception is expected to be ignored.")
    private void init() {
        try {
            Class<?> unsafe = Class.forName(REFLECTION_CLASS_NAME);
            Field accessor = AccessController.doPrivileged(new GetDeclaredFieldAction(unsafe, "theUnsafe"));
            AccessController.doPrivileged(SetAccessibleAction.of(accessor));
            unsafeInstance = accessor.get(null);
            allocateInstanceMethod = AccessController.doPrivileged(new GetDeclaredMethodAction(unsafe, "allocateInstance", Class.class));
        } catch (Exception e) {
            // OK to fail
        }
    }

    public boolean isAvailable() {
        init();
        return allocateInstanceMethod != null && unsafeInstance != null;
    }

    @SuppressWarnings("unchecked")
    public <T> T instantiate(Class<T> clazz) {
        try {
            return (T) allocateInstanceMethod.invoke(unsafeInstance, clazz);
        } catch (Exception e) {
            throw ReflectionLogger.LOG.unsafeInstantiationFailed(clazz, e);
        }
    }
}
