/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.models.caconfig.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to be used on either methods, fields or constructor parameters to let Sling Models inject a context-aware configuration.
 * The annotation supports the same features and semantics as {@link org.apache.sling.caconfig.ConfigurationBuilder}.
 * For configuration collections, you can use arrays, {@link java.util.List} or {@link java.util.Collection} variables.
 */
@Target({METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
@InjectAnnotation
@Source("caconfig")
public @interface ContextAwareConfiguration {

    /**
     * Define configuration name.
     * Optional if used together with a context-aware configuration annotation class, which implictely defines a configuration name.
     * @return Configuration name
     */
    public String name() default "";

    /**
     * if set to REQUIRED injection is mandatory, if set to OPTIONAL injection is optional, in case of DEFAULT
     * the standard annotations ({@link org.apache.sling.models.annotations.Optional}, {@link org.apache.sling.models.annotations.Required}) are used.
     * If even those are not available the default injection strategy defined on the {@link org.apache.sling.models.annotations.Model} applies.
     * Default value = DEFAULT.
     */
    public InjectionStrategy injectionStrategy() default InjectionStrategy.DEFAULT;
}
