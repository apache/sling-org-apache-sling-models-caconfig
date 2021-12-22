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
package org.apache.sling.models.caconfig.example.model;

import java.util.Collection;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.caconfig.annotations.ContextAwareConfiguration;
import org.apache.sling.models.caconfig.example.caconfig.model.ConfigurationValuesModel;

@Model(adaptables = { SlingHttpServletRequest.class, Resource.class })
public class ListConfigAdaptModel implements ListConfigGetter<ConfigurationValuesModel> {

    @ContextAwareConfiguration(name = "org.apache.sling.models.caconfig.example.caconfig.ListConfig")
    private List<ConfigurationValuesModel> configList;

    @ContextAwareConfiguration(name = "org.apache.sling.models.caconfig.example.caconfig.ListConfig")
    private Collection<ConfigurationValuesModel> configCollection;

    @ContextAwareConfiguration(name = "org.apache.sling.models.caconfig.example.caconfig.ListConfig")
    private ConfigurationValuesModel[] configArray;

    @Override
    public List<ConfigurationValuesModel> getConfigList() {
        return configList;
    }

    @Override
    public Collection<ConfigurationValuesModel> getConfigCollection() {
        return configCollection;
    }

    @Override
    public ConfigurationValuesModel[] getConfigArray() {
        return configArray;
    }

}
