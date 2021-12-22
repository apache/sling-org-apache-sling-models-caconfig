/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.models.caconfig.impl.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.caconfig.ConfigurationResolveException;
import org.apache.sling.caconfig.ConfigurationResolver;
import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationInjectResourceDetectionStrategyMultiplexer;
import org.apache.sling.models.caconfig.annotations.ContextAwareConfiguration;
import org.apache.sling.models.spi.AcceptsNullName;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.StaticInjectAnnotationProcessorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = { Injector.class, StaticInjectAnnotationProcessorFactory.class, AcceptsNullName.class })
@ServiceRanking(6000)
public class ContextAwareConfigurationInjector implements Injector, StaticInjectAnnotationProcessorFactory, AcceptsNullName {

    private static final Logger log = LoggerFactory.getLogger(ContextAwareConfigurationInjector.class);

    @Reference
    private ConfigurationResolver configurationResolver;
    @Reference
    private ConfigurationInjectResourceDetectionStrategyMultiplexer configurationInjectResourceDetectionStrategyMultiplexer;

    @Override
    public @NotNull String getName() {
        return "caconfig";
    }

    @Override
    public InjectAnnotationProcessor2 createAnnotationProcessor(AnnotatedElement element) {
        // check if the element has the expected annotation
        ContextAwareConfiguration annotation = getAnnotation(element);
        if (annotation != null) {
            return new ContextAwareConfigurationProcessor(annotation);
        }
        return null;
    }

    @Override
    public Object getValue(@NotNull Object adaptable, String name,
            @NotNull Type declaredType, @NotNull AnnotatedElement element,
            @NotNull DisposalCallbackRegistry callbackRegistry) {

        ContextAwareConfiguration annotation = getAnnotation(element);
        if (annotation == null) {
            // support injections only with explicit @ContextAwareConfiguration annotation
            log.warn("Injection only supported using @ContextAwareConfiguration annotation.");
            return null;
        }

        // get resource
        Resource resource = getResource(adaptable);
        if (resource == null) {
            log.warn("Unable to get resource from {}", adaptable);
            return null;
        }

        // initialize configuration builder
        ConfigurationBuilder configurationBuilder = configurationResolver.get(resource);
        if (StringUtils.isNotBlank(annotation.name())) {
            configurationBuilder = configurationBuilder.name(annotation.name());
        }

        // detect from declared type if a single configuration or configuration collection is requested and return the configuration
        if (declaredType instanceof Class) {
            Class<?> clazz = (Class<?>)declaredType;
            if (clazz.isArray()) {
                Collection<?> result = getConfigurationCollection(configurationBuilder, clazz.getComponentType());
                Object array = Array.newInstance(clazz.getComponentType(), result.size());
                Iterator<?> resultIterator = result.iterator();
                int i = 0;
                while (resultIterator.hasNext()) {
                    Array.set(array, i++, resultIterator.next());
                }
                return array;
            }
            else {
                return getConfiguration(configurationBuilder, clazz);
            }
        }
        else if (declaredType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)declaredType;
            if (parameterizedType.getActualTypeArguments().length != 1) {
                return null;
            }
            Class<?> collectionType = (Class<?>) parameterizedType.getRawType();
            if (!(collectionType.equals(Collection.class) || collectionType.equals(List.class))) {
                return null;
            }
            Class<?> clazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            Collection<?> result = getConfigurationCollection(configurationBuilder, clazz);
            if (collectionType.equals(List.class)) {
                return new ArrayList<>(result);
            }
            else {
                return result;
            }
        }
        else {
            log.warn("Cannot handle type {}", declaredType);
            return null;
        }
    }

    private @Nullable ContextAwareConfiguration getAnnotation(AnnotatedElement element) {
        return element.getAnnotation(ContextAwareConfiguration.class);
    }

    private @Nullable Resource getResource(@NotNull Object adaptable) {
        if (adaptable instanceof Resource) {
            return (Resource)adaptable;
        }
        if (adaptable instanceof SlingHttpServletRequest) {
            SlingHttpServletRequest request = (SlingHttpServletRequest)adaptable;
            Resource resource = configurationInjectResourceDetectionStrategyMultiplexer.detectResource(request);
            if (resource == null) {
                resource = request.getResource();
            }
            return resource;
        }
        return null;
    }

    private @Nullable Object getConfiguration(@NotNull ConfigurationBuilder configurationBuilder, @NotNull Class<?> clazz) {
        try {
            if (clazz.equals(ValueMap.class)) {
                return configurationBuilder.asValueMap();
            }
            if (isContextAwareConfigAnnotationClass(clazz)) {
                return configurationBuilder.as(clazz);
            }
            return configurationBuilder.asAdaptable(clazz);
        }
        catch (ConfigurationResolveException ex) {
            throw new ConfigurationResolveException("Class " + clazz.getName() + ": " + ex.getMessage(), ex);
        }
    }

    private @NotNull Collection<?> getConfigurationCollection(@NotNull ConfigurationBuilder configurationBuilder, @NotNull Class<?> clazz) {
        try {
            if (clazz.equals(ValueMap.class)) {
                return configurationBuilder.asValueMapCollection();
            }
            if (isContextAwareConfigAnnotationClass(clazz)) {
                return configurationBuilder.asCollection(clazz);
            }
            return configurationBuilder.asAdaptableCollection(clazz);
        }
        catch (ConfigurationResolveException ex) {
            throw new ConfigurationResolveException("Class " + clazz.getName() + ": " + ex.getMessage(), ex);
        }
    }

    private boolean isContextAwareConfigAnnotationClass(Class<?> clazz) {
        return clazz.isAnnotation() && clazz.isAnnotationPresent(Configuration.class);
    }

}
