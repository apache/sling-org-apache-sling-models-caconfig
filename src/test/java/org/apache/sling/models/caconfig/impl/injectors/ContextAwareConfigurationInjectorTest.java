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
package org.apache.sling.models.caconfig.impl.injectors;

import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.function.Function;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.models.caconfig.example.caconfig.ListConfig;
import org.apache.sling.models.caconfig.example.caconfig.SingleConfig;
import org.apache.sling.models.caconfig.example.caconfig.model.ConfigurationValuesModel;
import org.apache.sling.models.caconfig.example.model.InvalidAnnotationListModel;
import org.apache.sling.models.caconfig.example.model.InvalidAnnotationModel;
import org.apache.sling.models.caconfig.example.model.InvalidInjectModel;
import org.apache.sling.models.caconfig.example.model.InvalidSetModel;
import org.apache.sling.models.caconfig.example.model.ListConfigAdaptModel;
import org.apache.sling.models.caconfig.example.model.ListConfigGetter;
import org.apache.sling.models.caconfig.example.model.ListConfigModel;
import org.apache.sling.models.caconfig.example.model.ListConfigValueMapModel;
import org.apache.sling.models.caconfig.example.model.SingleConfigAdaptModel;
import org.apache.sling.models.caconfig.example.model.SingleConfigGetter;
import org.apache.sling.models.caconfig.example.model.SingleConfigModel;
import org.apache.sling.models.caconfig.example.model.SingleConfigValueMapModel;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextBuilder;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@ExtendWith(SlingContextExtension.class)
class ContextAwareConfigurationInjectorTest {

    private final SlingContext context = new SlingContextBuilder()
            .plugin(CACONFIG)
            .build();

    @BeforeEach
    void setUp() {
        MockContextAwareConfig.registerAnnotationPackages(context, "org.apache.sling.models.caconfig.example.caconfig");
        context.addModelsForPackage("org.apache.sling.models.caconfig.example.model");

        context.create().resource("/content/region/site",
                "sling:configRef", "/conf/region/site");

        context.currentResource(context.create().resource("/content/region/site/en"));

        MockContextAwareConfig.writeConfiguration(context, "/content/region/site", SingleConfig.class,
                "stringParam", "value1");

        MockContextAwareConfig.writeConfigurationCollection(context, "/content/region/site", ListConfig.class, ImmutableList.of(
                ImmutableMap.<String,Object>of("stringParam", "item1"),
                ImmutableMap.<String,Object>of("stringParam", "item2")));

        context.registerInjectActivateService(ContextAwareConfigurationInjector.class);
    }

    @Test
    void testSingleConfigModel_Request() {
        assertSingleConfig(SingleConfigModel.class, context.request(), SingleConfig::stringParam);
    }

    @Test
    void testSingleConfigModel_Resource() {
        assertSingleConfig(SingleConfigModel.class, context.currentResource(), SingleConfig::stringParam);
    }

    @Test
    void testSingleConfigValueMapModel_Request() {
        assertSingleConfig(SingleConfigValueMapModel.class, context.request(), map -> map.get("stringParam", String.class));
    }

    @Test
    void testSingleConfigValueMapModel_Resource() {
        assertSingleConfig(SingleConfigValueMapModel.class, context.currentResource(), map -> map.get("stringParam", String.class));
    }

    @Test
    void testSingleConfigAdaptModel_Request() {
        assertSingleConfig(SingleConfigAdaptModel.class, context.request(), ConfigurationValuesModel::getStringParam);
    }

    @Test
    void testSingleConfigAdaptModel_Resource() {
        assertSingleConfig(SingleConfigAdaptModel.class, context.currentResource(), ConfigurationValuesModel::getStringParam);
    }

    @Test
    void testListConfigModel_Request() {
        assertListConfig(ListConfigModel.class, context.request(), ListConfig::stringParam);
    }

    @Test
    void testListConfigModel_Resource() {
        assertListConfig(ListConfigModel.class, context.currentResource(), ListConfig::stringParam);
    }

    @Test
    void testListConfigValueMapModel_Request() {
        assertListConfig(ListConfigValueMapModel.class, context.request(), map -> map.get("stringParam", String.class));
    }

    @Test
    void testListConfigValueMapModel_Resource() {
        assertListConfig(ListConfigValueMapModel.class, context.currentResource(), map -> map.get("stringParam", String.class));
    }

    @Test
    void testListConfigAdaptModel_Request() {
        assertListConfig(ListConfigAdaptModel.class, context.request(), ConfigurationValuesModel::getStringParam);
    }

    @Test
    void testListConfigAdaptModel_Resource() {
        assertListConfig(ListConfigAdaptModel.class, context.currentResource(), ConfigurationValuesModel::getStringParam);
    }

    private <T> void assertSingleConfig(Class<? extends SingleConfigGetter<T>> modelClass, Adaptable adaptable, Function<T,String> extractor) {
        SingleConfigGetter<T> model = adaptable.adaptTo(modelClass);
        assertNotNull(model);
        T config = model.getConfig();
        assertEquals("value1", extractor.apply(config));
    }

    private <T> void assertListConfig(Class<? extends ListConfigGetter<T>> modelClass, Adaptable adaptable, Function<T,String> extractor) {
        ListConfigGetter<T> model = adaptable.adaptTo(modelClass);
        assertNotNull(model);
        assertListValues(model.getConfigList(), extractor);
        assertListValues(ImmutableList.copyOf(model.getConfigCollection()), extractor);
        assertListValues(ImmutableList.copyOf(model.getConfigArray()), extractor);
    }

    private <T> void assertListValues(List<T> configList, Function<T,String> extractor) {
        assertNotNull(configList);
        assertEquals(2, configList.size());
        assertEquals("item1", extractor.apply(configList.get(0)));
        assertEquals("item2", extractor.apply(configList.get(1)));
    }

    @Test
    void testInvalid_SingleConfigModel_ResourceResolver() {
        SingleConfigModel model = context.resourceResolver().adaptTo(SingleConfigModel.class);
        assertNull(model);
    }

    @Test
    @SuppressWarnings("null")
    void testInvalidInjectModel() {
        InvalidInjectModel model = context.request().adaptTo(InvalidInjectModel.class);
        assertNull(model);
    }

    @Test
    @SuppressWarnings("null")
    void testInvalidAnnotationModel() {
        InvalidAnnotationModel model = context.request().adaptTo(InvalidAnnotationModel.class);
        assertNull(model);
    }

    @Test
    @SuppressWarnings("null")
    void testInvalidAnnotationListModel() {
        InvalidAnnotationListModel model = context.request().adaptTo(InvalidAnnotationListModel.class);
        assertNull(model);
    }

    @Test
    @SuppressWarnings("null")
    void testInvalidSetModel() {
        InvalidSetModel model = context.request().adaptTo(InvalidSetModel.class);
        assertNull(model);
    }

}
